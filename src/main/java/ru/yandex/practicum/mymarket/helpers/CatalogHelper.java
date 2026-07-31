package ru.yandex.practicum.mymarket.helpers;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;

import java.util.Comparator;
import java.util.function.Predicate;

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
        return pageSize;
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

    /**
     * <summary>
     * Создает предикат проверки соответствия товара поисковой подстроке без учета регистра букв.
     * </summary>
     * @param search Нормализованная поисковая строка для фильтрации.
     * <return>
     * @return Предикат для фильтрации реактивного или стандартного стрима товаров.
     * </return>
     **/
    public Predicate<ItemModel> matchesSearch(final String search)
    {
        if (search.isEmpty())
        {
            return item -> true;
        }

        var lowerCaseSearch = search.toLowerCase();

        return item -> item.getTitle().toLowerCase().contains(lowerCaseSearch) ||
                item.getDescription().toLowerCase().contains(lowerCaseSearch);
    }

    /**
     * <summary>
     * Разрешает нужный экземпляр компаратора для выполнения in-memory сортировки доменных моделей.
     * </summary>
     * @param sort Выбранный элемент перечисления стратегии сортировки элементов.
     * <return>
     * @return Компаратор для упорядочивания объектов ItemModel в реактивном потоке.
     * </return>
     **/
    public Comparator<ItemModel> resolveComparator(final ItemSortEnumModel sort)
    {
        return switch (sort)
        {
            case NO -> Comparator.comparing(ItemModel::getId);
            case ALPHA -> Comparator.comparing(ItemModel::getTitle, String.CASE_INSENSITIVE_ORDER);
            case PRICE -> Comparator.comparing(ItemModel::getPrice);
        };
    }

    // endregion
}