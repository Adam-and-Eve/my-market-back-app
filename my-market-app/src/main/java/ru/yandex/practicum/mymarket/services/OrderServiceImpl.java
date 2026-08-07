package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.repositories.UserRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

/**
 * <summary>
 * Сервис управления заказами пользователей
 **/
@Service
public class OrderServiceImpl implements OrderService {

    // region Fields

    /**
     * Репозиторий для выполнения операций над доменными моделями заказов.
     **/
    private final OrderRepository orderRepository;

    /**
     * <summary>
     * Репозиторий для управления персистентным состоянием элементов корзины покупателя.
     * </summary>
     **/
    private final OrderItemRepository orderItemRepository;

    /**
     * Компонент-маппер для трансформации доменных моделей заказов в их UI-представления.
     **/
    private final OrderMapper orderMapper;

    /**
     * Репозиторий для управления персистентным состоянием учетных записей пользователей.
     **/
    private final UserRepository userRepository;

    // endregion

    // region Constructors

    public OrderServiceImpl(
            final OrderRepository orderRepository,
            final OrderItemRepository orderItemRepository,
            final OrderMapper orderMapper,
            final UserRepository userRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает список всех оформленных заказов пользователя по его имени, отсортированных по возрастанию идентификатора.
     * </summary>
     * @param username Имя пользователя для поиска заказов.
     * <return>
     * @return Реактивный поток Flux с моделями представления заказов OrderViewModel.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Flux<OrderViewModel> findAll(final String username) {
        return userRepository.findByUsername(username)
                .flatMapMany(user -> orderRepository.findAllByUserIdAndStatusOrderByIdAsc(user.getId(), OrderModel.STATUS_PAID))
                .flatMapSequential(this::buildOrderViewModel);
    }

    /**
     * <summary>
     * Выполняет поиск оформленного заказа по его уникальному идентификатору и имени пользователя.
     * </summary>
     * @param username Имя покупателя.
     * @param id Уникальный идентификатор искомого заказа.
     * <return>
     * @return Сконвертированная модель представления заказа OrderViewModel.
     * </return>
     * @throws ResponseStatusException Со статусом HTTP 404 Not Found, если заказ с указанным идентификатором не найден.
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<OrderViewModel> findById(final String username, final long id) {
        return userRepository.findByUsername(username)
                .flatMap(user -> orderRepository.findByIdAndUserId(id, user.getId()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.")))
                .flatMap(this::buildOrderViewModel);
    }

    /**
     * <summary>
     * Вспомогательный метод для асинхронной загрузки позиций заказа из репозитория и формирования итоговой View Model через маппер.
     * </summary>
     * @param order Исходная доменная модель заказа.
     * <return>
     * @return Реактивный контейнер Mono с собранной моделью представления OrderViewModel.
     * </return>
     **/
    private Mono<OrderViewModel> buildOrderViewModel(final OrderModel order) {
        return orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId())
                .collectList()
                .map(orderItems -> orderMapper.toViewModel(order, orderItems));
    }

    // endregion
}