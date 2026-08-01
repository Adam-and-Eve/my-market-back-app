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
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;

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

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    // endregion

    // region Tests for buy

    /**
     * <summary>
     * Проверяет, что покупка возвращает пустой Mono.empty(), если корзина пользователя пуста.
     * </summary>
     **/
    @Test
    void buyShouldReturnEmptyWhenCartIsEmpty() {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.empty());

        StepVerifier.create(purchaseService.buy())
                .verifyComplete();

        Mockito.verifyNoInteractions(itemRepository);

        Mockito.verifyNoInteractions(orderRepository);

        Mockito.verifyNoInteractions(orderItemRepository);
    }

    /**
     * <summary>
     * Проверяет успешное оформление заказа: загрузку элементов корзины, проверку/получение товаров из каталога,
     * создание шапки заказа, перенос позиций с валидацией их содержимого (title, price, quantity),
     * очистку корзины и возврат ID созданного заказа.
     * </summary>
     **/
    @Test
    void buyShouldCreateOrderAndClearCartWhenCartHasItems() {
        var itemId = 100L;
        var cartQuantity = 3;

        var item = new ItemModel("Novation Launchkey", "Studio MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(itemId, cartQuantity);

        var cartItemsList = List.of(cartItem);

        var expectedOrderId = 42L;

        var savedOrderStub = OrderModel.create();

        ReflectionTestUtils.setField(savedOrderStub, "id", expectedOrderId);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(item));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenReturn(Mono.just(savedOrderStub));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItemModel>> listCaptor = ArgumentCaptor.forClass(List.class);

        Mockito.doReturn(Flux.empty()).when(orderItemRepository).saveAll(listCaptor.capture());

        Mockito.when(cartItemRepository.deleteAll(cartItemsList)).thenReturn(Mono.empty());

        StepVerifier.create(purchaseService.buy())
                .expectNext(expectedOrderId)
                .verifyComplete();

        List<OrderItemModel> savedOrderItems = listCaptor.getValue();

        Assertions.assertNotNull(savedOrderItems);

        Assertions.assertEquals(1, savedOrderItems.size());

        var savedOrderItem = savedOrderItems.getFirst();

        Assertions.assertEquals(expectedOrderId, savedOrderItem.getOrderId());

        Assertions.assertEquals("Novation Launchkey", savedOrderItem.getTitle());

        Assertions.assertEquals(45000L, savedOrderItem.getPrice());

        Assertions.assertEquals(cartQuantity, savedOrderItem.getQuantity());

        Mockito.verify(itemRepository, Mockito.times(1)).findById(itemId);

        Mockito.verify(orderItemRepository, Mockito.times(1)).saveAll(Mockito.any(Iterable.class));

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(cartItemsList);
    }

    /**
     * <summary>
     * Проверяет выброс исключения 404 Not Found, если товар из корзины отсутствует в каталоге.
     * </summary>
     **/
    @Test
    void buyShouldThrowNotFoundWhenItemMissingInCatalog() {
        var itemId = 999L;

        var cartItem = new CartItemModel(itemId, 1);

        var savedOrderStub = OrderModel.create();

        ReflectionTestUtils.setField(savedOrderStub, "id", 1L);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenReturn(Mono.just(savedOrderStub));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.empty());

        Mockito.when(cartItemRepository.deleteAll(Mockito.anyList())).thenReturn(Mono.empty());

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

        Mockito.verify(orderItemRepository, Mockito.never()).saveAll(Mockito.any(Flux.class));

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(Mockito.anyList());
    }

    // endregion
}