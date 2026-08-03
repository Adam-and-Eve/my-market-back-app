package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;

/**
 * <summary>
 * Веб-контроллер для обработки запросов к каталогу товаров.
 * </summary>
 **/
@Controller
public class CatalogController {

    // region Fields

    /**
     * Сервис для управления бизнес-логикой каталога товаров.
     **/
    private final ItemService itemService;

    /**
     * Сервис для управления состоянием корзины покупателя.
     **/
    private final CartService cartService;

    // endregion

    public CatalogController(
            final ItemService itemService,
            final CartService cartService) {

        this.itemService = itemService;
        this.cartService = cartService;
    }

    /**
     * <summary>
     * Обрабатывает GET-запросы к корневому адресу "/" и эндпоинту "/items" для формирования страницы каталога.
     * Считывает параметры фильтрации, сортировки и постраничного вывода, запрашивает скомпонованную страницу
     * у сервиса и пробрасывает все необходимые атрибуты в контекст представления.
     * </summary>
     * @param search Строка поискового запроса для фильтрации позиций (может быть null).
     * @param sort Имя стратегии сортировки элементов каталога (может быть null).
     * @param pageNumber Порядковый номер запрашиваемой страницы (может быть null).
     * @param pageSize Желаемый размер выводимой страницы (может быть null).
     * @param model Контекст модели Spring MVC для передачи данных в UI-шаблон.
     * <return>
     * @return Логическое имя HTML-шаблона ("items") для отображения интерфейса пользователю.
     * </return>
     **/
    @GetMapping({"/", "/items"})
    public Mono<String> getCatalog(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final Integer pageSize,
            final Model model
    ){
        return itemService.findCatalog(search, sort, pageNumber, pageSize)
                        .doOnNext(catalogPage -> fillModel(model, catalogPage))
                        .thenReturn("items");
    }

    /**
     * <summary>
     * Обрабатывает GET-запросы для отображения детальной карточки конкретного товара по его идентификатору.
     * </summary>
     * @param id Уникальный идентификатор запрашиваемого товара.
     * @param model Контекст модели Spring MVC для передачи данных в UI-шаблон.
     * <return>
     * @return Логическое имя HTML-шаблона ("item").
     * </return>
     **/
    @GetMapping("/items/{id}")
    public Mono<String> getItem(
            @PathVariable final long id,
            final Model model) {

        return itemService.findById(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы с витрины товаров для изменения количества выбранной позиции в корзине.
     * После изменения состояния выполняет редирект обратно на каталог с сохранением всех фильтров и пагинации.
     * </summary>
     * @param id Идентификатор изменяемого товара.
     * @param search Текущее состояние строки поиска (для сохранения контекста при редиректе).
     * @param sort Текущее состояние сортировки (для сохранения контекста при редиректе).
     * @param pageNumber Текущий номер страницы (для сохранения контекста при редиректе).
     * @param pageSize Текущий размер страницы (для сохранения контекста при редиректе).
     * @param action Действие над товаром.
     * <return>
     * @return Строка перенаправления (redirect) на эндпоинт каталога с query-параметрами.
     * </return>
     **/
    @PostMapping("/items")
    public Mono<String> updateCatalogItem(
            @RequestParam final long id,
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final Integer pageSize,
            @RequestParam CartActionEnumModel action
    ){
        return cartService.updateItemCount(id, action)
                .thenReturn(redirectToCatalog(search, sort, pageNumber, pageSize));
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы со страницы отдельного товара для изменения его количества в корзине.
     * </summary>
     * @param id Идентификатор товара, переданный в пути запроса.
     * @param action Действие над товаром.
     * <return>
     * @return Строка перенаправления (redirect) на GET-эндпоинт карточки текущего товара.
     * </return>
     **/
    @PostMapping("/items/{id}")
    public Mono<String> updateItem(
            @PathVariable final long id,
            @RequestParam CartActionEnumModel action,
            Model model
    ){
        return cartService.updateItemCount(id, action)
                .thenReturn("redirect:/items/" + id);
    }

    private void fillModel(final Model model, CatalogPageViewModel viewModel) {
        model.addAttribute("items", viewModel.items());
        model.addAttribute("search", viewModel.search());
        model.addAttribute("sort", viewModel.sort());
        model.addAttribute("paging", viewModel.paging());
    }

    /**
     * <summary>
     * Вспомогательный метод для формирования строки безопасного перенаправления на страницу каталога.
     * Сохраняет состояние установленных пользователем фильтров, сортировок и пагинации.
     * </summary>
     * @param search Текущее значение фильтра поиска.
     * @param sort Текущее значение правила сортировки.
     * @param pageNumber Номер текущей страницы каталога.
     * @param pageSize Вместимость текущей страницы каталога.
     * <return>
     * @return Строка редиректа со всеми необходимыми query-параметрами.
     * </return>
     **/
    private String redirectToCatalog(
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize
    ){
        var builder = UriComponentsBuilder.fromPath("/items");

        addQueryParam(builder, "search", search);
        addQueryParam(builder, "sort", sort);
        addQueryParam(builder, "pageNumber", pageNumber);
        addQueryParam(builder, "pageSize", pageSize);

        return "redirect:" + builder.toUriString();
    }

    /**
     * <summary>
     * Вспомогательный метод для условного добавления параметров запроса в UriComponentsBuilder.
     * Игнорирует параметры со значением null.
     * </summary>
     * @param builder Конструктор URI-компонентов.
     * @param name Название query-параметра.
     * @param value Значение query-параметра.
     **/
    private void addQueryParam(UriComponentsBuilder builder, String name, Object value){
        if (value != null){
            builder.queryParam(name, value);
        }
    }
}