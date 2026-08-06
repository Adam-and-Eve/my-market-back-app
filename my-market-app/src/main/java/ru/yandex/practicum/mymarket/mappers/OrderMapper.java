package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

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

    public OrderMapper(
            final ItemMapper itemMapper) {

        this.itemMapper = itemMapper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Синхронно преобразует доменную модель заказа и список его позиций в объект модели представления (View Model).
     * Вычисляет общую стоимость заказа на основе переданных позиций.
     * </summary>
     * @param order Исходная доменная модель данных заказа.
     * @param orderItems Список доменных моделей позиций, входящих в данный заказ.
     * <return>
     * @return Сконвертированная модель представления заказа OrderViewModel.
     * </return>
     **/
    public OrderViewModel toViewModel(
            final OrderModel order,
            final List<OrderItemModel> orderItems
    ) {
        var itemsViewModels = orderItems.stream()
                .map(itemMapper::toViewModel)
                .toList();

        var totalSum = itemsViewModels.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        return new OrderViewModel(order.getId(), itemsViewModels, totalSum);
    }

    // endregion
}