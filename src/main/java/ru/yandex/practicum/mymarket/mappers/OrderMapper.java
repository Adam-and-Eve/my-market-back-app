package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

/**
 * <summary>
 * Компонент-маппер для преобразования моделей данных заказов в модели представления.
 * Отвечает за трансформацию объектов OrderModel в OrderViewModel и их структурную группировку для UI-слоя.
 * </summary>
 **/
@Component
public class OrderMapper {

    // region Fields

    private final ItemMapper itemMapper;

    // endregion

    // region Constructors

    public OrderMapper(final ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Преобразует одиночную модель заказа в объект модели представления (View Model).
     * </summary>
     * @param order Исходная модель данных заказа.
     * <return>
     * @return Сконвертированная модель представления заказа.
     * </return>
     **/
    public OrderViewModel toViewModel(
            final OrderModel order) {

        var items = order.getItems()
                .stream()
                .map(itemMapper::toViewModel)
                .toList();

        var totalSum = items.
                stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        return new OrderViewModel(order.getId(), items, totalSum);
    }

    // endregion
}