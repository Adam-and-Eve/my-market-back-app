package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
     * Извлекает все заказы пользователя из базы данных, сортируя их по возрастанию идентификатора.
     * </summary>
     * @param userId Идентификатор покупателя.
     * <return>
     * @return Реактивный поток Flux с доменными сущностями заказов OrderModel, отсортированными по возрастанию ID.
     * </return>
     **/
    public Flux<OrderModel> findAllByUserIdOrderByIdAsc(final long userId);

    /**
     * <summary>
     * Извлекает заказы пользователя из базы данных по указанному статусу, сортируя их по возрастанию идентификатора.
     * </summary>
     * @param userId Идентификатор покупателя.
     * @param status Строковое представление статуса для фильтрации.
     * <return>
     * @return Реактивный поток Flux с доменными сущностями заказов OrderModel, отсортированными по возрастанию ID.
     * </return>
     **/
    public Flux<OrderModel> findAllByUserIdAndStatusOrderByIdAsc(final long userId, final String status);

    /**
     * <summary>
     * Извлекает заказ из базы данных по указанному идентификатору заказа и покупателя.
     * </summary>
     * @param id Идентификатор заказа.
     * @param userId Идентификатор покупателя.
     * <return>
     * @return Реактивный контейнер Mono, содержащий найденную доменную сущность заказа OrderModel, или Mono.empty(), если заказ не найден.
     * </return>
     **/
    public Mono<OrderModel> findByIdAndUserId(final long id, final long userId);

    // endregion
}