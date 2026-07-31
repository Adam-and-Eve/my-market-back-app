package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики OrderServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

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
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // endregion

    // region Tests for findAll

    /**
     * <summary>
     * Проверяет получение списка всех заказов из Flux, когда они присутствуют в базе данных.
     * </summary>
     **/
    @Test
    void findAllShouldReturnMappedViewModelsWhenOrdersExist()
    {
        var order = OrderModel.create();

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(Flux.just(order));

        Mockito.when(orderMapper.toViewModel(order)).thenReturn(Mono.just(mockViewModel));

        var result = orderService.findAll().collectList().block();

        Assertions.assertNotNull(result);

        Assertions.assertEquals(1, result.size());

        Assertions.assertSame(mockViewModel, result.getFirst());
    }

    /**
     * <summary>
     * Проверяет, что метод findAll возвращает пустой поток, если заказов еще нет.
     * </summary>
     **/
    @Test
    void findAllShouldReturnEmptyListWhenNoOrdersExist()
    {
        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(Flux.empty());

        var result = orderService.findAll().collectList().block();

        Assertions.assertNotNull(result);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verifyNoInteractions(orderMapper);
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет успешный поиск существующего заказа по его идентификатору через Mono.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnViewModelWhenOrderExists()
    {
        var orderId = 10L;

        var order = OrderModel.create();

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));

        Mockito.when(orderMapper.toViewModel(order)).thenReturn(Mono.just(mockViewModel));

        var result = orderService.findById(orderId).block();

        Assertions.assertNotNull(result);

        Assertions.assertSame(mockViewModel, result);
    }

    /**
     * <summary>
     * Проверяет выбрасывание ResponseStatusException со статусом 404 из Mono.error(), если заказ не найден.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowNotFoundWhenOrderDoesNotExist()
    {
        var orderId = 999L;

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        var exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            orderService.findById(orderId).block();
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        Assertions.assertEquals("Order not found.", exception.getReason());
    }

    // endregion

    // region Tests for buy

    /**
     * <summary>
     * Проверяет, что покупка прерывается и возвращает -1, если текущая корзина пользователя пуста.
     * </summary>
     **/
    @Test
    void buyShouldReturnMinusOneWhenCartIsEmpty()
    {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.empty());

        var orderId = orderService.buy().block();

        Assertions.assertEquals(-1L, orderId);

        Mockito.verifyNoInteractions(orderRepository);

        Mockito.verifyNoInteractions(orderItemRepository);
    }

    /**
     * <summary>
     * Проверяет успешное оформление заказа: создание исторической структуры,
     * перенос позиций корзины через промежуточное обогащение данными товаров,
     * очистку корзины и возврат сгенерированного ID из реактивной цепочки.
     * </summary>
     **/
    @Test
    void buyShouldCreateOrderAndClearCartWhenCartHasItems()
    {
        var itemId = 100L;

        var item = new ItemModel("Тестовый товар", "Описание", "/img.png", 500L);

        ReflectionTestUtils.setField(item, "id", itemId);

        var cartItem = new CartItemModel(item, 3);

        var cartItemsList = List.of(cartItem);

        var expectedOrderId = 42L;

        var savedOrderStub = OrderModel.create();

        ReflectionTestUtils.setField(savedOrderStub, "id", expectedOrderId);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenReturn(Mono.just(savedOrderStub));

        Mockito.when(orderItemRepository.saveAll(Mockito.any(Flux.class))).thenReturn(Flux.empty());

        Mockito.when(cartItemRepository.deleteAll(cartItemsList)).thenReturn(Mono.empty());

        var actualOrderId = orderService.buy().block();

        Assertions.assertEquals(expectedOrderId, actualOrderId);

        Mockito.verify(orderItemRepository, Mockito.times(1)).saveAll(Mockito.any(Flux.class));

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(cartItemsList);
    }

    // endregion
}