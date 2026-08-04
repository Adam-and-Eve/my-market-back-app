package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.PaymentClientService;
import ru.yandex.practicum.mymarket.interfaces.PurchaseService;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.CheckoutResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PendingOrderDetailsViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PreparedCartItemViewModel;

import java.util.List;

/**
 * <summary>
 * Сервис для координации процесса покупки.
 * Отвечает за транзакционное преобразование текущей корзины пользователя в оформленный заказ.
 * </summary>
 **/
@Service
public class PurchaseServiceImpl implements PurchaseService {

    // region Fields

    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentClientService paymentClientService;

    // endregion

    // region Constructors

    public PurchaseServiceImpl(
            final ItemRepository itemRepository,
            final CartItemRepository cartItemRepository,
            final OrderRepository orderRepository,
            final OrderItemRepository orderItemRepository,
            final PaymentClientService paymentClientService) {

        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentClientService = paymentClientService;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Оформляет транзакцию покупки: предварительно создает заказ в статусе PENDING, сохраняет его позиции,
     * проводит списание средств через внешнюю платежную систему и обновляет статус заказа на PAID или PAYMENT_FAILED.
     * </summary>
     * <return>
     * @return Модель представления с результатом оформления заказа.
     * </return>
     **/
    @Transactional
    public Mono<CheckoutResultViewModel> buy() {
        return cartItemRepository.findAllByOrderByItemIdAsc()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.just(CheckoutResultViewModel.empty());
                    }

                    return prepareAndSavePendingOrder(cartItems)
                            .flatMap(details -> paymentClientService.pay(details.totalAmount())
                                    .flatMap(payment -> processPaymentResult(details.order(), cartItems, payment)));
                });
    }

    /**
     * <summary>
     * Валидирует товары из каталога, создает заказ в статусе PENDING, сохраняет его позиции в БД и высчитывает итоговую стоимость.
     * </summary>
     **/
    private Mono<PendingOrderDetailsViewModel> prepareAndSavePendingOrder(final List<CartItemModel> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .switchIfEmpty(Mono.error(
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in catalog")
                        ))
                        .map(item -> new PreparedCartItemViewModel(item, cartItem.getQuantity())))
                .collectList()
                .flatMap(preparedItems -> {
                    long totalAmount = preparedItems.stream()
                            .mapToLong(p -> p.item().getPrice() * p.quantity())
                            .sum();

                    OrderModel pendingOrder = OrderModel.create();

                    return orderRepository.save(pendingOrder)
                            .flatMap(savedOrder -> {
                                List<OrderItemModel> orderItems = preparedItems.stream()
                                        .map(p -> new OrderItemModel(
                                                savedOrder.getId(),
                                                p.item().getTitle(),
                                                p.item().getPrice(),
                                                p.quantity()
                                        ))
                                        .toList();

                                return orderItemRepository.saveAll(orderItems)
                                        .then()
                                        .thenReturn(new PendingOrderDetailsViewModel(savedOrder, totalAmount));
                            });
                });
    }

    /**
     * <summary>
     * Обрабатывает результат проведения платежа во внешнем сервисе и обновляет статус заказа в БД.
     * </summary>
     **/
    private Mono<CheckoutResultViewModel> processPaymentResult(
            final OrderModel order,

            final List<CartItemModel> cartItems,

            final OrderPaymentResultViewModel payment) {

        if (!payment.success()) {
            order.markAsPaymentFailed();

            return orderRepository.save(order)
                    .thenReturn(CheckoutResultViewModel.rejected(payment.message()));
        }

        order.markAsPaid();

        return orderRepository.save(order)
                .then(cartItemRepository.deleteAll(cartItems))
                .thenReturn(CheckoutResultViewModel.paid(order.getId()));
    }

    // endregion
}