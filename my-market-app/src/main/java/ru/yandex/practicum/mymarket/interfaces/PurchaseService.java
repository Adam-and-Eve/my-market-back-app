package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Mono;

public interface PurchaseService {

    // region Methods

    /**
     * <summary>
     * Оформляет транзакцию покупки: выгружает все элементы из текущей корзины покупателя, переносит их
     * в историческую структуру нового заказа с фиксацией цен, сохраняет заказ в БД и полностью очищает корзину.
     * </summary>
     * <return>
     * @return Уникальный идентификатор созданного заказа, либо Mono.empty(), если корзина покупателя оказалась пуста.
     * </return>
     **/
    public Mono<Long> buy();

    // endregion
}