package ru.yandex.practicum.mymarket.interfaces;

import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;

import java.util.List;

/**
 * <summary>
 * Контракт сервиса для управления каталогом товаров.
 * </summary>
 **/
public interface ItemService {

    // region Methods

    /**
     * <summary>
     * Извлекает полный нефильтрованный список всех товаров из базы данных.
     * </summary>
     * <return>
     * @return Список доменных моделей всех существующих товаров.
     * </return>
     **/
    public List<ItemModel> findAll();

    /**
     * <summary>
     * Выполняет построение страницы каталога товаров на основе переданных фильтров, правил сортировки и пагинации.
     * Обеспечивает безопасную нормализацию параметров перед отправкой запроса в слой хранения данных.
     * </summary>
     * @param search Необработанная поисковая строка для фильтрации по названию или описанию.
     * @param sort Строковое имя стратегии сортировки элементов.
     * @param pageNumber Запрашиваемый номер страницы каталога.
     * @param pageSize Желаемое количество элементов на одной странице.
     * <return>
     * @return Сформированная модель представления страницы каталога с сеткой товаров и метаданными навигации.
     * </return>
     **/
    public CatalogPageViewModel findCatalog(
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize);

    // endregion
}