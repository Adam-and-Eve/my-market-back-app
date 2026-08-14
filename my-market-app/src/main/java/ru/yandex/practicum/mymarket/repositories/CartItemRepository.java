package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.CartItemModel;

import java.util.Collection;

/**
 * <summary>
 * Интерфейс репозитория для управления доменными моделями элементов корзины в базе данных.
 * Расширяет JpaRepository для реализации базовых CRUD-операций над сущностями CartItemModel.
 * </summary>
 **/
public interface CartItemRepository extends ReactiveCrudRepository<CartItemModel, Long> {

    // region Methods

    /**
     * <summary>
     * Выполняет пакетную выборку элементов корзины пользователя для переданной коллекции идентификаторов товаров.
     * Используется для оптимизации запросов и предотвращения проблемы N+1 при пагинации каталога.
     * </summary>
     * @param userId Идентификатор покупателя.
     * @param itemIds Коллекция идентификаторов интересующих товаров.
     * <return>
     * @return Реактивный поток Flux с доменными моделями элементов корзины, соответствующих переданным идентификаторам.
     * </return>
     **/
    Flux<CartItemModel> findAllByUserIdAndItemIdIn(final long userId, final Collection<Long> itemIds);

    /**
     * <summary>
     * Выполняет выборку всех элементов корзины пользователя с сортировкой по возрастанию идентификатора товара.
     * Используется для обеспечения стабильного порядка отображения позиций в UI при изменении их количества.
     * </summary>
     * @param userId Идентификатор покупателя.
     * <return>
     * @return Реактивный поток Flux с доменными моделями всех элементов корзины пользователя, упорядоченных по ID товара.
     * </return>
     **/
    Flux<CartItemModel> findAllByUserIdOrderByItemIdAsc(final long userId);

    Mono<CartItemModel> findByUserIdAndItemId(
            long userId,
            long itemId);

    // endregion
}