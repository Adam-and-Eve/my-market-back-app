package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.interfaces.PaymentClientService;
import ru.yandex.practicum.mymarket.models.*;
import ru.yandex.practicum.mymarket.repositories.*;
import ru.yandex.practicum.mymarket.viewmodels.CheckoutResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики PurchaseServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTest {

    // region Constants

    private static final String TEST_USERNAME = "user";

    private static final long TEST_USER_ID = 1L;

    // endregion

    // region Fields

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentClientService paymentClientService;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private UserModel testUser;

    // endregion

    // region Setup

    /**
     * <summary>
     * Настраивает поведение транзакционного оператора и корректную модель пользователя.
     * </summary>
     **/
    @BeforeEach
    void setUp() {
        Mockito.lenient().when(transactionalOperator.transactional(Mockito.any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.lenient().when(transactionalOperator.transactional(Mockito.any(Flux.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        testUser = new UserModel(TEST_USER_ID, TEST_USERNAME, true, LocalDateTime.now());
    }

    // endregion

    // region Tests for buy

    /**
     * <summary>
     * Проверяет выброс ошибки 404 Not Found, если пользователь с указанным именем не найден.
     * </summary>
     **/
    @Test
    void buyShouldThrowNotFoundWhenUserDoesNotExist() {
        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException rse &&
                                rse.getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                                "User not found".equals(rse.getReason())
                )
                .verify();

        Mockito.verifyNoInteractions(cartItemRepository, itemRepository, orderRepository, orderItemRepository, paymentClientService);
    }

    /**
     * <summary>
     * Проверяет, что покупка возвращает CheckoutResultViewModel.empty(), если корзина пользователя пуста.
     * </summary>
     **/
    @Test
    void buyShouldReturnEmptyWhenCartIsEmpty() {
        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.empty());

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectNext(CheckoutResultViewModel.empty())
                .verifyComplete();

        Mockito.verifyNoInteractions(itemRepository, orderRepository, orderItemRepository, paymentClientService);
    }

    /**
     * <summary>
     * Проверяет успешный цикл покупки: сохранение заказа в статусе PENDING, успешную оплату,
     * обновление статуса до PAID и очистку корзины.
     * </summary>
     **/
    @Test
    void buyShouldCreateOrderAndClearCartWhenCartHasItemsAndPaymentSucceeds() {
        var itemId = 100L;

        var cartQuantity = 3;

        var itemPrice = 45000L;

        var totalAmount = itemPrice * cartQuantity;

        var expectedOrderId = 42L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", itemPrice);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, cartQuantity);

        var cartItemsList = List.of(cartItem);

        var paymentResult = OrderPaymentResultViewModel.success(1000000L);

        List<String> capturedStatuses = new ArrayList<>();

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenAnswer(invocation -> {
            OrderModel order = invocation.getArgument(0);
            capturedStatuses.add(order.getStatus());

            if (order.getId() == null) {
                ReflectionTestUtils.setField(order, "id", expectedOrderId);
            }

            return Mono.just(order);
        });

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItemModel>> listCaptor = ArgumentCaptor.forClass(List.class);

        Mockito.doReturn(Flux.empty()).when(orderItemRepository).saveAll(listCaptor.capture());

        Mockito.when(paymentClientService.pay(totalAmount)).thenReturn(Mono.just(paymentResult));

        Mockito.when(cartItemRepository.deleteAll(cartItemsList)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectNext(CheckoutResultViewModel.paid(expectedOrderId))
                .verifyComplete();

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any(OrderModel.class));

        Assertions.assertEquals(2, capturedStatuses.size());

        Assertions.assertEquals(OrderModel.STATUS_PENDING, capturedStatuses.get(0));

        Assertions.assertEquals(OrderModel.STATUS_PAID, capturedStatuses.get(1));

        List<OrderItemModel> savedOrderItems = listCaptor.getValue();

        Assertions.assertNotNull(savedOrderItems);

        Assertions.assertEquals(1, savedOrderItems.size());

        var savedOrderItem = savedOrderItems.getFirst();

        Assertions.assertEquals(expectedOrderId, savedOrderItem.getOrderId());

        Assertions.assertEquals("Novation Launchkey", savedOrderItem.getTitle());

        Assertions.assertEquals(itemPrice, savedOrderItem.getPrice());

        Assertions.assertEquals(cartQuantity, savedOrderItem.getQuantity());

        Mockito.verify(paymentClientService, Mockito.times(1)).pay(totalAmount);

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(cartItemsList);
    }

    /**
     * <summary>
     * Проверяет перевод заказа в статус PAYMENT_FAILED и сохранение содержимого корзины при отказе платежного шлюза.
     * </summary>
     **/
    @Test
    void buyShouldMarkOrderAsPaymentFailedAndNotClearCartWhenPaymentFails() {
        var itemId = 100L;

        var cartQuantity = 1;

        var itemPrice = 45000L;

        var expectedOrderId = 10L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", itemPrice);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, cartQuantity);

        var paymentResult = OrderPaymentResultViewModel.rejected(0L, "Insufficient funds");

        List<String> capturedStatuses = new ArrayList<>();

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenAnswer(invocation -> {
            OrderModel order = invocation.getArgument(0);
            capturedStatuses.add(order.getStatus());

            if (order.getId() == null) {
                ReflectionTestUtils.setField(order, "id", expectedOrderId);
            }

            return Mono.just(order);
        });

        Mockito.doReturn(Flux.empty()).when(orderItemRepository).saveAll(Mockito.anyList());

        Mockito.when(paymentClientService.pay(itemPrice)).thenReturn(Mono.just(paymentResult));

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectNext(CheckoutResultViewModel.rejected("Insufficient funds"))
                .verifyComplete();

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any(OrderModel.class));

        Assertions.assertEquals(2, capturedStatuses.size());

        Assertions.assertEquals(OrderModel.STATUS_PENDING, capturedStatuses.get(0));

        Assertions.assertEquals(OrderModel.STATUS_PAYMENT_FAILED, capturedStatuses.get(1));

        Mockito.verify(cartItemRepository, Mockito.never()).deleteAll(Mockito.anyList());
    }

    /**
     * <summary>
     * Платеж прошел успешно, но последующее сохранение заказа
     * со статусом PAID завершилось ошибкой базы данных.
     * </summary>
     **/
    @Test
    void buyShouldPropagateErrorWhenFinalizingPaidOrderFailsAfterSuccessfulPayment() {
        var itemId = 100L;

        var itemPrice = 45000L;

        var expectedOrderId = 77L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", itemPrice);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, 1);

        var paymentResult = OrderPaymentResultViewModel.success(500000L);

        List<String> capturedStatuses = new ArrayList<>();

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenAnswer(invocation -> {
            OrderModel order = invocation.getArgument(0);

            capturedStatuses.add(order.getStatus());

            if (OrderModel.STATUS_PENDING.equals(order.getStatus())) {
                if (order.getId() == null) {
                    ReflectionTestUtils.setField(order, "id", expectedOrderId);
                }

                return Mono.just(order);
            }

            return Mono.error(new RuntimeException("Database error during status update"));
        });

        Mockito.doReturn(Flux.empty()).when(orderItemRepository).saveAll(Mockito.anyList());

        Mockito.when(paymentClientService.pay(itemPrice)).thenReturn(Mono.just(paymentResult));

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectError(RuntimeException.class)
                .verify();

        Assertions.assertEquals(2, capturedStatuses.size());

        Assertions.assertEquals(OrderModel.STATUS_PENDING, capturedStatuses.get(0));

        Assertions.assertEquals(OrderModel.STATUS_PAID, capturedStatuses.get(1));

        Mockito.verify(paymentClientService, Mockito.times(1)).pay(itemPrice);

        Mockito.verify(cartItemRepository, Mockito.never()).deleteAll(Mockito.anyList());

        Mockito.verify(orderRepository, Mockito.times(2)).save(Mockito.any(OrderModel.class));
    }

    /**
     * <summary>
     * Проверяет, что при сбое сохранения шапки заказа paymentClientService.pay() не вызывается.
     * </summary>
     **/
    @Test
    void buyShouldNotCallPaymentWhenOrderSaveFails() {
        var itemId = 100L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, 1);

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class)))
                .thenReturn(Mono.error(new RuntimeException("Database connection failure")));

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectError(RuntimeException.class)
                .verify();

        Mockito.verifyNoInteractions(paymentClientService);

        Mockito.verifyNoInteractions(orderItemRepository);
    }

    /**
     * <summary>
     * Проверяет, что при сбое сохранения позиций заказа (orderItemRepository.saveAll) paymentClientService.pay() не вызывается.
     * </summary>
     **/
    @Test
    void buyShouldNotCallPaymentWhenOrderItemSaveFails() {
        var itemId = 100L;

        var expectedOrderId = 50L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, 1);

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenAnswer(invocation -> {
            OrderModel order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", expectedOrderId);
            return Mono.just(order);
        });

        Mockito.when(orderItemRepository.saveAll(Mockito.anyList()))
                .thenReturn(Flux.error(new RuntimeException("Error saving order items")));

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectError(RuntimeException.class)
                .verify();

        Mockito.verifyNoInteractions(paymentClientService);

        Mockito.verify(cartItemRepository, Mockito.never()).deleteAll(Mockito.anyList());
    }

    /**
     * <summary>
     * Проверяет выброс исключения 404 Not Found при сохранении позиций заказа, если товар из корзины отсутствует в каталоге.
     * </summary>
     **/
    @Test
    void buyShouldThrowNotFoundWhenItemMissingInCatalog() {
        var itemId = 999L;

        var cartItem = new CartItemModel(TEST_USER_ID, itemId, 1);

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(testUser));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(TEST_USER_ID)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy(TEST_USERNAME))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException rse &&
                                rse.getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                                "Item not found in catalog".equals(rse.getReason())
                )
                .verify();

        Mockito.verifyNoInteractions(orderRepository, orderItemRepository, paymentClientService);

        Mockito.verify(cartItemRepository, Mockito.never()).deleteAll(Mockito.anyList());
    }

    // endregion
}