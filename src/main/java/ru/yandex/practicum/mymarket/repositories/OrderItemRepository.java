package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.models.OrderItemModel;

/**
 * <summary>
 * Интерфейс реактивного репозитория для выполнения CRUD-операций и управления персистентным состоянием исторических позиций заказов в базе данных.
 * </summary>
 **/
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemModel, Long> {

    // region Methods

    /**
     * <summary>
     * Выполняет выборку всех зафиксированных товарных позиций, принадлежащих конкретному оформленному заказу.
     * </summary>
     * @param orderId Уникальный идентификатор заказа, для которого извлекаются позиции.
     * <return>
     * @return Реактивный поток Flux, эмитирующий доменные модели выкупленных товаров OrderItemModel.
     * </return>
     **/
    Flux<OrderItemModel> findAllByOrderIdOrderByIdAsc(final long orderId);

    // endregion
}