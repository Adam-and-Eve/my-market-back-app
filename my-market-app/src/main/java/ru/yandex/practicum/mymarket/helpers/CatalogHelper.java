package ru.yandex.practicum.mymarket.helpers;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;

/**
 * <summary>
 * Вспомогательный компонент (Helper) для обработки и валидации параметров каталога товаров.
 * Обеспечивает нормализацию входных поисковых запросов, параметров постраничной навигации,
 * а также отвечает за построение объектов сортировки для слоя данных.
 * </summary>
 **/
@Component
public class CatalogHelper {

    // region Constants

    /**
     * Номер страницы по умолчанию (индексация пользовательского интерфейса начинается с 1).
     **/
    private static final int DEFAULT_PAGE_NUMBER = 1;

    /**
     * Количество отображаемых товаров на странице по умолчанию.
     **/
    private static final int DEFAULT_PAGE_SIZE = 5;

    /**
     * Максимально допустимое количество товаров на одной странице.
     **/
    private static final int MAX_PAGE_SIZE = 100;

    // endregion

    // region Methods

    /**
     * <summary>
     * Нормализует поисковую строку, очищая её от начальных и конечных пробельных символов.
     * Если переданный аргумент равен null, возвращает пустую строку во избежание NullPointerException.
     * </summary>
     * @param search Исходная строка поискового запроса от пользователя.
     * <return>
     * @return Нормализованная поисковая строка, гарантированно отличная от null.
     * </return>
     **/
    public String normalizeSearch(final String search) {
        return search == null
                ? ""
                : search.trim();
    }

    /**
     * <summary>
     * Валидирует и нормализует номер запрашиваемой страницы каталога.
     * Если номер страницы равен null или меньше допустимого минимума, возвращает дефолтный индекс.
     * </summary>
     * @param pageNumber Запрашиваемый номер страницы.
     * <return>
     * @return Свалидированный и безопасный для использования номер страницы.
     * </return>
     **/
    public int normalizePageNumber(final Integer pageNumber) {
        if (pageNumber == null || pageNumber < DEFAULT_PAGE_NUMBER) {
            return DEFAULT_PAGE_NUMBER;
        }
        return pageNumber;
    }

    /**
     * <summary>
     * Валидирует и нормализует количество элементов, отображаемых на одной странице.
     * Если размер равен null или меньше единицы, сбрасывает значение на размер страницы по умолчанию.
     * </summary>
     * @param pageSize Запрашиваемый размер страницы.
     * <return>
     * @return Свалидированное и безопасное количество элементов для пагинации.
     * </return>
     **/
    public int normalizePageSize(final Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    /**
     * <summary>
     * Транслирует внутреннюю модель стратегии сортировки во вспомогательный объект конфигурации Spring Data Sort.
     * </summary>
     * @param sort Выбранный элемент перечисления стратегии сортировки.
     * <return>
     * @return Сконфигурированный объект Sort для выполнения запроса в репозитории.
     * </return>
     **/
    public Sort resolveSort(final ItemSortEnumModel sort) {
        return switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };
    }

    // endregion
}