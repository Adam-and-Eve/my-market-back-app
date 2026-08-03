package ru.yandex.practicum.mymarket.viewmodels;

import ru.yandex.practicum.mymarket.models.CartActionEnumModel;

/**
 * <summary>
 * Модель формы передачи данных для выполнения операций с позициями корзины.
 * Передает идентификатор товара, тип выполняемого действия и сохраняет состояние пагинации и поиска в каталоге.
 * </summary>
 **/
public class CartItemFormViewModel {

    // region Fields

    /**
     * <summary>
     * Уникальный идентификатор товара.
     * </summary>
     **/
    private Long id;

    /**
     * <summary>
     * Поисковый запрос для сохранения состояния фильтрации каталога при перенаправлении.
     * </summary>
     **/
    private String search;

    /**
     * <summary>
     * Поле и направление сортировки для сохранения состояния каталога при перенаправлении.
     * </summary>
     **/
    private String sort;

    /**
     * <summary>
     * Номер текущей страницы каталога.
     * </summary>
     **/
    private Integer pageNumber;

    /**
     * <summary>
     * Размер страницы (количество элементов) в каталоге.
     * </summary>
     **/
    private Integer pageSize;

    /**
     * <summary>
     * Тип действия, выполняемого над элементом корзины.
     * </summary>
     **/
    private CartActionEnumModel action;

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает уникальный идентификатор товара.
     * </summary>
     * <return>
     * @return Уникальный идентификатор товара.
     * </return>
     **/
    public Long getId() {
        return id;
    }

    /**
     * <summary>
     * Возвращает поисковый запрос каталога.
     * </summary>
     * <return>
     * @return Строка поискового запроса.
     * </return>
     **/
    public String getSearch() {
        return search;
    }

    /**
     * <summary>
     * Возвращает параметр сортировки каталога.
     * </summary>
     * <return>
     * @return Параметр сортировки.
     * </return>
     **/
    public String getSort() {
        return sort;
    }

    /**
     * <summary>
     * Возвращает номер текущей страницы пагинации.
     * </summary>
     * <return>
     * @return Номер страницы.
     * </return>
     **/
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * <summary>
     * Возвращает количество элементов на странице.
     * </summary>
     * <return>
     * @return Размер страницы.
     * </return>
     **/
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * <summary>
     * Возвращает целевое действие над товаром в корзине.
     * </summary>
     * <return>
     * @return Модель типа действия с корзиной.
     * </return>
     **/
    public CartActionEnumModel getAction() {
        return action;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Устанавливает уникальный идентификатор товара.
     * </summary>
     * @param id Уникальный идентификатор товара.
     **/
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <summary>
     * Устанавливает поисковый запрос каталога.
     * </summary>
     * @param search Поисковый запрос.
     **/
    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * <summary>
     * Устанавливает параметр сортировки каталога.
     * </summary>
     * @param sort Параметр сортировки.
     **/
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * <summary>
     * Устанавливает номер текущей страницы.
     * </summary>
     * @param pageNumber Номер страницы.
     **/
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * <summary>
     * Устанавливает размер страницы пагинации.
     * </summary>
     * @param pageSize Размер страницы.
     **/
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * <summary>
     * Устанавливает целевое действие над товаром в корзине.
     * </summary>
     * @param action Вид действия над корзиной.
     **/
    public void setAction(CartActionEnumModel action) {
        this.action = action;
    }

    // endregion
}