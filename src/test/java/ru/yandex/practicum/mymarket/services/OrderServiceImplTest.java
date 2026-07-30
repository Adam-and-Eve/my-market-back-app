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
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
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
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет получение списка всех заказов, когда они присутствуют в базе данных.
     * </summary>
     **/
    @Test
    void findAllShouldReturnMappedViewModelsWhenOrdersExist()
    {
        var order = OrderModel.create();

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(List.of(order));

        Mockito.when(orderMapper.toViewModel(order)).thenReturn(mockViewModel);

        var result = orderService.findAll();

        Assertions.assertNotNull(result);

        Assertions.assertEquals(1, result.size());

        Assertions.assertSame(mockViewModel, result.getFirst());
    }

    /**
     * <summary>
     * Проверяет, что метод findAll возвращает пустой список, если заказов еще нет.
     * </summary>
     **/
    @Test
    void findAllShouldReturnEmptyListWhenNoOrdersExist()
    {
        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(Collections.emptyList());

        var result = orderService.findAll();

        Assertions.assertNotNull(result);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verifyNoInteractions(orderMapper);
    }

    /**
     * <summary>
     * Проверяет успешный поиск существующего заказа по его идентификатору.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnViewModelWhenOrderExists()
    {
        var orderId = 10L;

        var order = OrderModel.create();

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        Mockito.when(orderMapper.toViewModel(order)).thenReturn(mockViewModel);

        var result = orderService.findById(orderId);

        Assertions.assertNotNull(result);

        Assertions.assertSame(mockViewModel, result);
    }

    /**
     * <summary>
     * Проверяет выбрасывание ResponseStatusException со статусом 404, если заказ не найден.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowNotFoundWhenOrderDoesNotExist()
    {
        var orderId = 999L;

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(ResponseStatusException.class, () ->
        {
            orderService.findById(orderId);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        Assertions.assertEquals("Order not found.", exception.getReason());
    }

    /**
     * <summary>
     * Проверяет, что покупка прерывается и возвращает -1, если текущая корзина пользователя пуста.
     * </summary>
     **/
    @Test
    void buyShouldReturnMinusOneWhenCartIsEmpty()
    {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Collections.emptyList());

        var orderId = orderService.buy();

        Assertions.assertEquals(-1L, orderId);

        Mockito.verifyNoInteractions(orderRepository);
    }

    /**
     * <summary>
     * Проверяет успешное оформление заказа: создание исторической структуры,
     * перенос позиций корзины, очистку корзины и возврат сгенерированного ID.
     * </summary>
     **/
    @Test
    void buyShouldCreateOrderAndClearCartWhenCartHasItems()
    {
        var item = new ItemModel("Тестовый товар", "Описание", "/img.png", 500L);

        var cartItem = new CartItemModel(item, 3);

        var cartItemsList = List.of(cartItem);

        var expectedOrderId = 42L;

        var savedOrderStub = OrderModel.create();

        ReflectionTestUtils.setField(savedOrderStub, "id", expectedOrderId);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(cartItemsList);

        Mockito.when(orderRepository.save(Mockito.any(OrderModel.class))).thenReturn(savedOrderStub);

        var actualOrderId = orderService.buy();

        Assertions.assertEquals(expectedOrderId, actualOrderId);

        Mockito.verify(cartItemRepository, Mockito.times(1)).deleteAll(cartItemsList);
    }

    // endregion
}