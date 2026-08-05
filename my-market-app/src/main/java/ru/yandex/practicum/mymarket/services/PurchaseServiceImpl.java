package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
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
    private final TransactionalOperator transactionalOperator;

    // endregion

    // region Constructors

    public PurchaseServiceImpl(
            final ItemRepository itemRepository,
            final CartItemRepository cartItemRepository,
            final OrderRepository orderRepository,
            final OrderItemRepository orderItemRepository,
            final PaymentClientService paymentClientService,
            final TransactionalOperator transactionalOperator) {

        this.itemRepository = itemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentClientService = paymentClientService;
        this.transactionalOperator = transactionalOperator;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Оформляет покупку в 3 этапа:
     * 1. (Транзакция) Создает заказ PENDING и сохраняет позиции.
     * 2. (Без транзакции) Проводит списание средств.
     * 3. (Транзакция) Обновляет статус до PAID/PAYMENT_FAILED и очищает корзину.
     * </summary>
     * <return>
     * @return Модель представления с результатом оформления заказа.
     * </return>
     **/
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
     * Валидирует товары, создает заказ в статусе PENDING, сохраняет его позиции.
     * Выполняется в рамках независимой транзакции.
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
                })
                .as(transactionalOperator::transactional);
    }

    /**
     * <summary>
     * Обрабатывает результат проведения платежа и обновляет статус заказа.
     * Выполняется в рамках независимой транзакции.
     * </summary>
     **/
    private Mono<CheckoutResultViewModel> processPaymentResult(
            final OrderModel order,
            final List<CartItemModel> cartItems,
            final OrderPaymentResultViewModel payment) {

        Mono<CheckoutResultViewModel> resultMono;

        if (!payment.success()) {
            order.markAsPaymentFailed();
            resultMono = orderRepository.save(order)
                    .thenReturn(CheckoutResultViewModel.rejected(payment.message()));
        } else {
            order.markAsPaid();
            resultMono = orderRepository.save(order)
                    .then(Mono.defer(() -> cartItemRepository.deleteAll(cartItems)))
                    .thenReturn(CheckoutResultViewModel.paid(order.getId()));
        }

        return resultMono.as(transactionalOperator::transactional);
    }

    // endregion
}