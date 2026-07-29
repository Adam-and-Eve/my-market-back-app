package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.interfaces.ItemService;

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

    // endregion

    public CatalogController(final ItemService itemService) {
        this.itemService = itemService;
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
    public String getCatalog(
            @RequestParam(required = false) final String search,
            @RequestParam(required = false) final String sort,
            @RequestParam(required = false) final Integer pageNumber,
            @RequestParam(required = false) final Integer pageSize,
            final Model model
    ){
        var catalogPage = itemService.findCatalog(search, sort, pageNumber, pageSize);

        model.addAttribute("items", catalogPage.items());

        model.addAttribute("search", catalogPage.search());

        model.addAttribute("sort", catalogPage.sort());

        model.addAttribute("paging", catalogPage.paging());

        return "items";
    }
}