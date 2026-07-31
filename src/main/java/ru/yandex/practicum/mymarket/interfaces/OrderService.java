package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

/**
 * <summary>
 * Контракт сервиса для управления бизнес-логикой создания, обработки и получения заказов пользователей.
 * </summary>
 **/
public interface OrderService {

    // region Methods

    /**
     * <summary>
     * Возвращает полную коллекцию оформленных заказов, отсортированных по возрастанию их идентификатора.
     * </summary>
     * <return>
     * @return Список моделей представления всех существующих заказов.
     * </return>
     **/
    public Flux<OrderViewModel> findAll();

    /**
     * <summary>
     * Выполняет поиск оформленного заказа по его уникальному идентификатору и преобразует его в модель представления.
     * </summary>
     * @param id Уникальный идентификатор искомого заказа.
     * <return>
     * @return Сконвертированная модель представления заказа OrderViewModel.
     * </return>
     **/
    public Mono<OrderViewModel> findById(final long id);

    /**
     * <summary>
     * Оформляет покупку на основе текущего содержимого корзины покупателя.
     * Переносит все активные элементы корзины в историческую структуру нового заказа, фиксируя цены,
     * после чего полностью очищает корзину.
     * </summary>
     * <return>
     * @return Уникальный идентификатор созданного заказа, либо -1, если корзина была пуста.
     * </return>
     **/
    public Mono<Long> buy();

    // endregion
}