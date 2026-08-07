package ru.yandex.practicum.mymarket.services;

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
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.mappers.OrderMapper;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.models.UserModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.repositories.UserRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.time.Duration;
import java.util.List;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики OrderServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    // region Fields

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    // endregion

    // region Tests for findAll

    /**
     * <summary>
     * Проверяет получение списка всех заказов пользователя из Flux, когда они присутствуют в базе данных.
     * </summary>
     **/
    @Test
    void findAllShouldReturnMappedViewModelsWhenOrdersExist() {
        var username = "user";

        var userId = 100L;

        var orderId = 1L;

        var user = Mockito.mock(UserModel.class);

        Mockito.when(user.getId()).thenReturn(userId);

        var order = OrderModel.create(userId);

        ReflectionTestUtils.setField(order, "id", orderId);

        var orderItem = new OrderItemModel(orderId, "Товар", 100L, 1);

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(orderRepository.findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID))
                .thenReturn(Flux.just(order));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId)).thenReturn(Flux.just(orderItem));

        Mockito.when(orderMapper.toViewModel(order, List.of(orderItem))).thenReturn(mockViewModel);

        StepVerifier.create(orderService.findAll(username).collectList())
                .expectNextMatches(result ->
                        result.size() == 1 && result.getFirst() == mockViewModel
                )
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);

        Mockito.verify(orderRepository, Mockito.times(1)).findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID);

        Mockito.verify(orderItemRepository, Mockito.times(1)).findAllByOrderIdOrderByIdAsc(orderId);
    }

    /**
     * <summary>
     * Проверяет, что метод findAll сохраняет строго исходный порядок следования заказов (1, 2, 3),
     * даже если асинхронная загрузка позиций для первого заказа происходит с задержкой.
     * </summary>
     **/
    @Test
    void findAllShouldPreserveOrderWhenPositionLoadingIsDelayedForFirstItem() {
        var username = "user";

        var userId = 100L;

        var orderId1 = 1L;

        var orderId2 = 2L;

        var orderId3 = 3L;

        var user = Mockito.mock(UserModel.class);

        Mockito.when(user.getId()).thenReturn(userId);

        var order1 = OrderModel.create(userId);

        ReflectionTestUtils.setField(order1, "id", orderId1);

        var order2 = OrderModel.create(userId);

        ReflectionTestUtils.setField(order2, "id", orderId2);

        var order3 = OrderModel.create(userId);

        ReflectionTestUtils.setField(order3, "id", orderId3);

        var orderItem1 = new OrderItemModel(orderId1, "Товар 1", 100L, 1);

        var orderItem2 = new OrderItemModel(orderId2, "Товар 2", 200L, 1);

        var orderItem3 = new OrderItemModel(orderId3, "Товар 3", 300L, 1);

        var mockViewModel1 = Mockito.mock(OrderViewModel.class);

        var mockViewModel2 = Mockito.mock(OrderViewModel.class);

        var mockViewModel3 = Mockito.mock(OrderViewModel.class);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(orderRepository.findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID))
                .thenReturn(Flux.just(order1, order2, order3));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId1))
                .thenReturn(Flux.just(orderItem1).delayElements(Duration.ofMillis(100)));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId2))
                .thenReturn(Flux.just(orderItem2));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId3))
                .thenReturn(Flux.just(orderItem3));

        Mockito.when(orderMapper.toViewModel(order1, List.of(orderItem1))).thenReturn(mockViewModel1);

        Mockito.when(orderMapper.toViewModel(order2, List.of(orderItem2))).thenReturn(mockViewModel2);

        Mockito.when(orderMapper.toViewModel(order3, List.of(orderItem3))).thenReturn(mockViewModel3);

        StepVerifier.create(orderService.findAll(username))
                .expectNext(mockViewModel1)
                .expectNext(mockViewModel2)
                .expectNext(mockViewModel3)
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);

        Mockito.verify(orderRepository, Mockito.times(1)).findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID);

        Mockito.verify(orderItemRepository, Mockito.times(1)).findAllByOrderIdOrderByIdAsc(orderId1);

        Mockito.verify(orderItemRepository, Mockito.times(1)).findAllByOrderIdOrderByIdAsc(orderId2);

        Mockito.verify(orderItemRepository, Mockito.times(1)).findAllByOrderIdOrderByIdAsc(orderId3);
    }

    /**
     * <summary>
     * Проверяет, что метод findAll возвращает пустой поток, если у пользователя еще нет заказов.
     * </summary>
     **/
    @Test
    void findAllShouldReturnEmptyListWhenNoOrdersExist() {
        var username = "user";

        var userId = 100L;

        var user = Mockito.mock(UserModel.class);

        Mockito.when(user.getId()).thenReturn(userId);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(orderRepository.findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID))
                .thenReturn(Flux.empty());

        StepVerifier.create(orderService.findAll(username).collectList())
                .expectNextMatches(List::isEmpty)
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);

        Mockito.verify(orderRepository, Mockito.times(1)).findAllByUserIdAndStatusOrderByIdAsc(userId, OrderModel.STATUS_PAID);

        Mockito.verifyNoInteractions(orderMapper, orderItemRepository);
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет успешный поиск существующего заказа конкретного пользователя по его идентификатору через Mono.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnViewModelWhenOrderExists() {
        var username = "user";

        var userId = 100L;

        var orderId = 10L;

        var user = Mockito.mock(UserModel.class);

        Mockito.when(user.getId()).thenReturn(userId);

        var order = OrderModel.create(userId);

        ReflectionTestUtils.setField(order, "id", orderId);

        var orderItem = new OrderItemModel(orderId, "Ноутбук", 75000L, 1);

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Mono.just(order));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId)).thenReturn(Flux.just(orderItem));

        Mockito.when(orderMapper.toViewModel(order, List.of(orderItem))).thenReturn(mockViewModel);

        StepVerifier.create(orderService.findById(username, orderId))
                .expectNext(mockViewModel)
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);

        Mockito.verify(orderRepository, Mockito.times(1)).findByIdAndUserId(orderId, userId);

        Mockito.verify(orderItemRepository, Mockito.times(1)).findAllByOrderIdOrderByIdAsc(orderId);
    }

    /**
     * <summary>
     * Проверяет выбрасывание ResponseStatusException со статусом 404 из Mono.error(), если заказ не найден у данного пользователя.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowNotFoundWhenOrderDoesNotExist() {
        var username = "user";

        var userId = 100L;

        var orderId = 999L;

        var user = Mockito.mock(UserModel.class);

        Mockito.when(user.getId()).thenReturn(userId);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.findById(username, orderId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException resEx &&
                                resEx.getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                                "Order not found.".equals(resEx.getReason())
                )
                .verify();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);

        Mockito.verify(orderRepository, Mockito.times(1)).findByIdAndUserId(orderId, userId);

        Mockito.verifyNoInteractions(orderItemRepository, orderMapper);
    }

    // endregion
}