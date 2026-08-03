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
     * Оформляет транзакцию покупки: выгружает все элементы из текущей корзины покупателя, переносит их
     * в историческую структуру нового заказа с фиксацией цен, сохраняет заказ в БД и полностью очищает корзину.
     * </summary>
     * <return>
     * @return Уникальный идентификатор созданного заказа, либо Mono.empty(), если корзина покупателя оказалась пуста.
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

                    return calculateTotal(cartItems)
                            .flatMap(paymentClientService::pay)
                            .flatMap(payment -> finishCheckout(cartItems, payment));
                });
    }

    /**
     * <summary>
     * Завершает процесс оформления заказа по результатам транзакции оплаты: при успешном ответе
     * сохраняет заказ в БД с очисткой корзины, а при отказе формирует отклоненный результат покупки.
     * </summary>
     * @param cartItems Список элементов корзины, подлежащих переносу в заказ.
     * @param payment Результат проведения транзакции во внешнем платежном сервисе.
     * <return>
     * @return Моно-контейнер с итоговой моделью представления результата оформления покупки.
     * </return>
     **/
    private Mono<CheckoutResultViewModel> finishCheckout(List<CartItemModel> cartItems, OrderPaymentResultViewModel payment) {
        if (!payment.success()) {
            return Mono.just(CheckoutResultViewModel.rejected(payment.message()));
        }
        return saveOrder(cartItems).map(CheckoutResultViewModel::paid);
    }

    /**
     * <summary>
     * Вычисляет суммарную стоимость всех элементов корзины на основе актуальных цен товаров из каталога.
     * </summary>
     * @param cartItems Список элементов корзины для расчета итоговой суммы.
     * <return>
     * @return Моно-контейнер с рассчитанной общей стоимостью всех позиций в корзине.
     * </return>
     **/
    private Mono<Long> calculateTotal(List<CartItemModel> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> itemRepository.findById(cartItem.getItemId())
                        .map(item -> item.getPrice() * cartItem.getQuantity()))
                .reduce(0L, Long::sum);
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
                        .switchIfEmpty(Mono.error(
                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found in catalog")
                        ))
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
                        .collectList()
                        .flatMapMany(orderItemRepository::saveAll)
                        .then(cartItemRepository.deleteAll(cartItems))
                        .thenReturn(savedOrder.getId()));
    }

    // endregion
}