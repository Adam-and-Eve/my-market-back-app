package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.interfaces.PaymentClientService;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.CheckoutResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;

import java.util.Collections;
import java.util.List;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики PurchaseServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class PurchaseServiceImplTest {

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
    private PaymentClientService paymentClientService;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    // endregion

    // region Tests for buy

    /**
     * <summary>
     * Проверяет, что покупка возвращает CheckoutResultViewModel.empty(), если корзина пользователя пуста.
     * </summary>
     **/
    @Test
    void buyShouldReturnEmptyWhenCartIsEmpty() {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.empty());

        StepVerifier.create(purchaseService.buy())
                .expectNext(CheckoutResultViewModel.empty())
                .verifyComplete();

        Mockito.verifyNoInteractions(itemRepository, orderRepository, orderItemRepository, paymentClientService);
    }

    /**
     * <summary>
     * Проверяет успешный цикл покупки: расчет стоимости, успешную оплату через платежный шлюз,
     * создание шапки и позиций заказа, очистку корзины и возврат статуса PAID с ID заказа.
     * </summary>
     **/
    @Test
    void buyShouldCreateOrderAndClearCartWhenCartHasItemsAndPaymentSucceeds() {
        var itemId = 100L;

        var cartQuantity = 3;

        var itemPrice = 45000L;

        var totalAmount = itemPrice * cartQuantity;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", itemPrice);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(itemId, cartQuantity);

        var cartItemsList = List.of(cartItem);

        var expectedOrderId = 42L;

        var savedOrderStub = OrderModel.create();

        ReflectionTestUtils.setField(savedOrderStub, "id", expectedOrderId);

        var paymentResult = OrderPaymentResultViewModel.success(1000000L);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(paymentClientService.pay(totalAmount)).thenReturn(Mono.just(paymentResult));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenReturn(Mono.just(savedOrderStub));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItemModel>> listCaptor = ArgumentCaptor.forClass(List.class);

        Mockito.doReturn(Flux.empty()).when(orderItemRepository).saveAll(listCaptor.capture());

        Mockito.when(cartItemRepository.deleteAll(cartItemsList)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy())
                .expectNext(CheckoutResultViewModel.paid(expectedOrderId))
                .verifyComplete();

        List<OrderItemModel> savedOrderItems = listCaptor.getValue();

        Assertions.assertNotNull(savedOrderItems);

        Assertions.assertEquals(1, savedOrderItems.size());

        var savedOrderItem = savedOrderItems.getFirst();

        Assertions.assertEquals(expectedOrderId, savedOrderItem.getOrderId());

        Assertions.assertEquals("Novation Launchkey", savedOrderItem.getTitle());

        Assertions.assertEquals(itemPrice, savedOrderItem.getPrice());

        Assertions.assertEquals(cartQuantity, savedOrderItem.getQuantity());

        Mockito.verify(itemRepository, Mockito.times(2)).findById(itemId);

        Mockito.verify(paymentClientService, Mockito.times(1)).pay(totalAmount);

        Mockito.verify(orderItemRepository, Mockito.times(1)).saveAll(Mockito.any(Iterable.class));

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(cartItemsList);
    }

    /**
     * <summary>
     * Проверяет отклонение покупки при отказе платежного шлюза без создания заказа и без очистки корзины.
     * </summary>
     **/
    @Test
    void buyShouldReturnRejectedWhenPaymentFails() {
        var itemId = 100L;

        var cartQuantity = 1;

        var itemPrice = 45000L;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", itemPrice);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(itemId, cartQuantity);

        var paymentResult = OrderPaymentResultViewModel.rejected(0L, "Insufficient funds");

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(paymentClientService.pay(itemPrice)).thenReturn(Mono.just(paymentResult));

        StepVerifier.create(purchaseService.buy())
                .expectNext(CheckoutResultViewModel.rejected("Insufficient funds"))
                .verifyComplete();

        Mockito.verifyNoInteractions(orderRepository, orderItemRepository);

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

        var cartItem = new CartItemModel(itemId, 1);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy())
                .expectErrorMatches(throwable ->
                        {
                            if (!(throwable instanceof ResponseStatusException) ||
                                    !((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND))
                                return false;
                            assert ((ResponseStatusException) throwable).getReason() != null;
                            return ((ResponseStatusException) throwable).getReason().equals("Item not found in catalog");
                        }
                )
                .verify();

        Mockito.verify(orderItemRepository, Mockito.never()).saveAll(Mockito.anyList());

        Mockito.verify(cartItemRepository, Mockito.never()).deleteAll(Mockito.anyList());
    }

    // endregion
}