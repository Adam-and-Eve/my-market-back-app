package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CartItemFormViewModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;

import java.security.Principal;

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
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * @param model Контекст модели Spring MVC для передачи данных в UI-шаблон.
     * <return>
     * @return Реактивный контейнер Mono с логическим именем HTML-шаблона ("items").
     * </return>
     **/
    @GetMapping({"/", "/items"})
    public Mono<String> getCatalog(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final Integer pageSize,
            final Principal principal,
            final Model model
    ){

        return itemService.findCatalog(username(principal), search, sort, pageNumber, pageSize)
                        .doOnNext(catalogPage -> fillModel(model, catalogPage))
                        .thenReturn("items");
    }

    /**
     * <summary>
     * Обрабатывает GET-запросы для отображения детальной карточки конкретного товара по его идентификатору.
     * </summary>
     * @param id Уникальный идентификатор запрашиваемого товара.
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * @param model Контекст модели Spring MVC для передачи данных в UI-шаблон.
     * <return>
     * @return Реактивный контейнер Mono с логическим именем HTML-шаблона ("item").
     * </return>
     **/
    @GetMapping("/items/{id}")
    public Mono<String> getItem(
            @PathVariable final long id,
            final Principal principal,
            final Model model) {

        return itemService.findById(username(principal), id)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы с витрины товаров для изменения количества выбранной позиции в корзине.
     * После изменения состояния выполняет редирект обратно на каталог с сохранением всех фильтров и пагинации.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * @param form Валидированная модель представления формы изменения состава корзины.
     * <return>
     * @return Реактивный контейнер Mono со строкой перенаправления (redirect) на эндпоинт каталога с query-параметрами.
     * </return>
     **/
    @PostMapping("/items")
    public Mono<String> updateCatalogItem(
                final Principal principal,
                @Valid @ModelAttribute final CartItemFormViewModel form
            ){
        return cartService.updateItemCount(username(principal), form.getId(), form.getAction())
                .thenReturn(redirectToCatalog(
                        form.getSearch(),
                        form.getSort(),
                        form.getPageNumber(),
                        form.getPageSize()));
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы со страницы отдельного товара для изменения его количества в корзине.
     * </summary>
     * @param id Идентификатор товара, переданный в пути запроса.
     * @param form Валидированная модель представления формы изменения состава корзины.
     * @param model Контекст модели Spring MVC для передачи данных в UI-шаблон.
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * <return>
     * @return Реактивный контейнер Mono со строкой перенаправления (redirect) на GET-эндпоинт карточки текущего товара.
     * </return>
     **/
    @PostMapping("/items/{id}")
    public Mono<String> updateItem(
            @PathVariable final long id,
            @Valid @ModelAttribute final CartItemFormViewModel form,
            final Model model,
            final Principal principal
    ){
        return cartService.updateItemCount(username(principal), id, form.getAction())
                .thenReturn("redirect:/items/" + id);
    }

    /**
     * <summary>
     * Наполняет контекст модели Spring MVC данными из сформированной модели представления каталога.
     * </summary>
     * @param model Контекст модели Spring MVC.
     * @param viewModel Модель представления страницы каталога с товарами и параметрами пагинации.
     **/
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

    /**
     * <summary>
     * Извлекает имя пользователя из объекта аутентификации Principal.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя.
     * <return>
     * @return Имя пользователя или null, если объект Principal отсутствует.
     * </return>
     **/
    private String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}