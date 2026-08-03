package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogCellViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <summary>
 * Компонент-маппер для преобразования моделей данных товаров в модели представления.
 * Отвечает за трансформацию объектов ItemModel в ItemViewModel и их структурную группировку для UI-слоя.
 * </summary>
 **/
@Component
public class ItemMapper {

    // region Constants

    /**
     * Количество элементов (товаров), отображаемых в одной строке сеточной разметки каталога.
     **/
    private static final int ITEMS_PER_ROW = 3;

    // endregion

    // region Methods

    /**
     * <summary>
     * Преобразует плоский список моделей товаров в двумерную матрицу (список строк) моделей представления.
     * Группирует элементы по строкам фиксированной длины и дополняет последнюю строку заглушками, если она заполнена не полностью.
     * </summary>
     * @param items Список исходных моделей товаров из репозитория.
     * <return>
     * @return Двумерный список моделей представления, распределенных по строкам для отображения в сетке.
     * </return>
     **/
    public List<List<CatalogCellViewModel>> toRows(
            final List<ItemModel> items,
            final Map<Long, Integer> counts) {

        var rows = new ArrayList<List<CatalogCellViewModel>>();

        for (var i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            var row = new ArrayList<CatalogCellViewModel>();

            items.subList(i, Math.min(i + ITEMS_PER_ROW, items.size()))
                    .stream()
                    .map(item -> toViewModel(item, counts.getOrDefault(item.getId(), 0)))
                    .map(CatalogCellViewModel::of)
                    .forEach(row::add);

            while (row.size() < ITEMS_PER_ROW) {
                row.add(CatalogCellViewModel.placeholder());
            }

            rows.add(row);
        }

        return rows;
    }

    /**
     * <summary>
     * Преобразует одиночную модель товара в объект модели представления (View Model).
     * </summary>
     * @param item Исходная модель данных товара.
     * <return>
     * @return Сконвертированная модель представления товара.
     * </return>
     **/
    public ItemViewModel toViewModel(
            final ItemModel item,
            final int count) {

        return new ItemViewModel(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                count
        );
    }

    /**
     * <summary>
     * Преобразует одиночную модель товара в объект модели представления (View Model).
     * </summary>
     * @param cartItem Исходная модель данных товара.
     * <return>
     * @return Сконвертированная модель представления товара.
     * </return>
     **/
    public ItemViewModel toViewModel(
            final CartItemModel cartItem,
            final ItemModel item) {

        return new ItemViewModel(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                cartItem.getQuantity()
        );
    }

    /**
     * <summary>
     * Преобразует одиночную модель товара в объект модели представления (View Model).
     * </summary>
     * @param orderItem Исходная модель данных товара.
     * <return>
     * @return Сконвертированная модель представления товара.
     * </return>
     **/
    public ItemViewModel toViewModel(
            final OrderItemModel orderItem) {

        return new ItemViewModel(
                null,
                orderItem.getTitle(),
                "",
                "",
                orderItem.getPrice(),
                orderItem.getQuantity()
        );
    }

    // endregion
}