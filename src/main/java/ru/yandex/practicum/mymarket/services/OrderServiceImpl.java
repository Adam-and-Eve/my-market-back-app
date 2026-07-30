package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

/**
 * <summary>
 * Сервис управления заказами пользователей
 **/
@Service
public class OrderServiceImpl implements OrderService {

    // region Fields

    /**
     * Репозиторий для управления персистентным состоянием элементов корзины покупателя.
     **/
    private final CartItemRepository cartItemRepository;

    /**
     * Репозиторий для выполнения операций над доменными моделями заказов.
     **/
    private final OrderRepository orderRepository;

    /**
     * Компонент-маппер для трансформации доменных моделей заказов в их UI-представления.
     **/
    private final OrderMapper orderMapper;

    // endregion

    // region Constructors

    public OrderServiceImpl(
            final CartItemRepository cartItemRepository,
            final OrderRepository orderRepository,
            final OrderMapper orderMapper) {

        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    // endregion

    // region Methods

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
    public OrderViewModel findById(final long id) {
        return orderRepository
                .findById(id)
                .map(orderMapper::toViewModel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found."));
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
    public long buy() {
        var cartItems = cartItemRepository.findAllByOrderByItemIdAsc();

        if (cartItems.isEmpty()) {
            return -1;
        }

        var order = OrderModel.create();

        cartItems.forEach(cartItem -> order.addItem(cartItem.getItem(), cartItem.getQuantity()));

        var savedOrder = orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);

        return savedOrder.getId();
    }

    // endregion
}