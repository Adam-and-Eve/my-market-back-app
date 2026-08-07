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
     * Возвращает полную коллекцию оформленных заказов пользователя, отсортированных по возрастанию их идентификатора.
     * </summary>
     * @param username Имя пользователя, для которого запрашивается история заказов.
     * <return>
     * @return Реактивный поток Flux с моделями представления заказов OrderViewModel.
     * </return>
     **/
    public Flux<OrderViewModel> findAll(final String username);

    /**
     * <summary>
     * Выполняет поиск оформленного заказа по его уникальному идентификатору и преобразует его в модель представления.
     * </summary>
     * @param username Имя пользователя для проверки прав доступа к запрашиваемому заказу.
     * @param id Уникальный идентификатор искомого заказа.
     * <return>
     * @return Реактивный контейнер Mono со сконвертированной моделью представления заказа OrderViewModel.
     * </return>
     **/
    public Mono<OrderViewModel> findById(final String username, final long id);

    // endregion
}