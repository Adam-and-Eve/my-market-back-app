package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

/**
 * <summary>
 * Сервис управления заказами пользователей
 **/
@Service
public class OrderServiceImpl implements OrderService {

    // region Fields

    private final ItemRepository itemRepository;

    /**
     * Репозиторий для управления персистентным состоянием элементов корзины покупателя.
     **/
    private final CartItemRepository cartItemRepository;

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

    // endregion

    // region Constructors

    public OrderServiceImpl(
            final ItemRepository itemRepository,
            final CartItemRepository cartItemRepository,
            final OrderRepository orderRepository,
            final OrderItemRepository orderItemRepository,
            final OrderMapper orderMapper) {

        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает список всех оформленных заказов, отсортированных по возрастанию идентификатора, с маппингом во View Model.
     * Работает в режиме оптимизации транзакции "только для чтения".
     * </summary>
     * <return>
     * @return Список моделей представления заказов List.
     * </return>
     **/
    @Transactional(readOnly = true)
    public Flux<OrderViewModel> findAll() {
        return orderRepository.findAllByOrderByIdAsc()
                .flatMap(orderMapper::toViewModel);
    }

    /**
     * <summary>
     * Выполняет поиск оформленного заказа по его уникальному идентификатору с последующим маппингом во View Model.
     * </summary>
     * @param id Уникальный идентификатор искомого заказа.
     * <return>
     * @return Сконвертированная модель представления заказа OrderViewModel.
     * </return>
     * @throws ResponseStatusException Со статусом HTTP 404 Not Found, если заказ с указанным идентификатором не найден.
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<OrderViewModel> findById(final long id) {
        return orderRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found.")))
                .flatMap(orderMapper::toViewModel);
    }


    /**
     * <summary>
     * Оформляет транзакцию покупки: выгружает все элементы из текущей корзины покупателя, переносит их
     * в историческую структуру нового заказа с фиксацией цен, сохраняет заказ в БД и полностью очищает корзину.
     * </summary>
     * <return>
     * @return Уникальный идентификатор созданного заказа, либо -1, если корзина покупателя оказалась пуста.
     * </return>
     **/
    @Transactional()
    @Override
    public Mono<Long> buy() {
        return cartItemRepository.findAllByOrderByItemIdAsc()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.just(-1L);
                    }

                    return saveOrder(cartItems);
                });
    }

    /**
     * <summary>
     * Преобразует плоский список элементов корзины в реактивный поток исторических позиций создаваемого заказа.
     * </summary>
     * @param orderId Уникальный идентификатор созданного родительского заказа.
     * @param cartItems Список элементов корзины, подлежащих переносу в заказ.
     * <return>
     * @return Реактивный поток созданных исторических позиций заказа Flux.
     * </return>
     **/
    private Flux<OrderItemModel> createOrderItems(
            final long orderId,
            final List<CartItemModel> cartItems) {

        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> new OrderItemModel(
                                orderId,
                                item.getTitle(),
                                item.getPrice(),
                                cartItem.getQuantity()
                        )));
    }

    /**
     * <summary>
     * Атомарно сохраняет шапку нового заказа, генерирует и записывает его позиции,
     * после чего производит полную очистку текущей корзины покупателя.
     * </summary>
     * @param cartItems Список элементов корзины для сохранения в составе заказа.
     * <return>
     * @return Моно-контейнер с уникальным идентификатором успешно сохраненного заказа.
     * </return>
     **/
    private Mono<Long> saveOrder(final List<CartItemModel> cartItems) {
        return orderRepository.save(OrderModel.create())
                .flatMap(savedOrder -> createOrderItems(savedOrder.getId(), cartItems)
                        .as(orderItemRepository::saveAll)
                        .then(cartItemRepository.deleteAll(cartItems))
                        .thenReturn(savedOrder.getId()));
    }

    // endregion
}