package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

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

    // endregion
}