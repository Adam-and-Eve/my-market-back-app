package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
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

    private final OrderItemRepository orderItemRepository;
    private final ItemMapper itemMapper;

    // endregion

    // region Constructors

    public OrderMapper(
            final OrderItemRepository orderItemRepository,
            final ItemMapper itemMapper) {

        this.orderItemRepository = orderItemRepository;
        this.itemMapper = itemMapper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Асинхронно преобразует доменную модель заказа в объект модели представления (View Model).
     * Извлекает из репозитория все позиции, привязанные к заказу, и калькулирует общую стоимость.
     * </summary>
     * @param order Исходная доменная модель данных заказа.
     * <return>
     * @return Реактивный контейнер Mono со сконвертированной моделью представления заказа.
     * </return>
     **/
    public Mono<OrderViewModel> toViewModel(final OrderModel order)
    {
        return orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId())
                .map(itemMapper::toViewModel)
                .collectList()
                .map(items -> toOrderViewModel(order, items));
    }

    /**
     * <summary>
     * Синхронно агрегирует модель заказа и готовый список позиций, вычисляя финальную сумму.
     * </summary>
     **/
    private OrderViewModel toOrderViewModel(
            final OrderModel order,
            final List<ItemViewModel> items)
    {
        var totalSum = items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        return new OrderViewModel(order.getId(), items, totalSum);
    }

    // endregion
}