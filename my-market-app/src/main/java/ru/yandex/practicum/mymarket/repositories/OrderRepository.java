package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.models.OrderModel;

/**
 * <summary>
 * Интерфейс репозитория для выполнения операций CRUD и управления персистентным состоянием доменных моделей заказов OrderModel.
 * </summary>
 **/
public interface OrderRepository extends ReactiveCrudRepository<OrderModel, Long> {

    // region Methods

    /**
     * <summary>
     * Извлекает все заказы из базы данных, сортируя их по возрастанию идентификатора)
     * </summary>
     * <return>
     * @return Список доменных сущностей заказов OrderModel.
     * </return>
     **/
    public Flux<OrderModel> findAllByOrderByIdAsc();

    /**
     * <summary>
     * Извлекает заказы из базы данных по указанному статусу, сортируя их по возрастанию идентификатора.
     * </summary>
     * @param status Строковое представление статуса для фильтрации.
     * <return>
     * @return Список доменных сущностей заказов OrderModel.
     * </return>
     **/
    public Flux<OrderModel> findAllByStatusOrderByIdAsc(String status);

    // endregion
}