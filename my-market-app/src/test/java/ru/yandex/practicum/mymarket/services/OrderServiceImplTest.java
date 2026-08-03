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
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderItemRepository;
import ru.yandex.practicum.mymarket.repositories.OrderRepository;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

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
    void findAllShouldReturnMappedViewModelsWhenOrdersExist() {
        var orderId = 1L;

        var order = OrderModel.create();

        ReflectionTestUtils.setField(order, "id", orderId);

        var orderItem = new OrderItemModel(orderId, "Товар", 100L, 1);

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(Flux.just(order));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId)).thenReturn(Flux.just(orderItem));

        Mockito.when(orderMapper.toViewModel(order, List.of(orderItem))).thenReturn(mockViewModel);

        StepVerifier.create(orderService.findAll().collectList())
                .expectNextMatches(result ->
                        result.size() == 1 && result.getFirst() == mockViewModel
                )
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет, что метод findAll возвращает пустой поток, если заказов еще нет.
     * </summary>
     **/
    @Test
    void findAllShouldReturnEmptyListWhenNoOrdersExist() {
        Mockito.when(orderRepository.findAllByOrderByIdAsc()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.findAll().collectList())
                .expectNextMatches(List::isEmpty)
                .verifyComplete();

        Mockito.verifyNoInteractions(orderMapper, orderItemRepository);
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет успешный поиск существующего заказа по его идентификатору через Mono.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnViewModelWhenOrderExists() {
        var orderId = 10L;

        var order = OrderModel.create();

        ReflectionTestUtils.setField(order, "id", orderId);

        var orderItem = new OrderItemModel(orderId, "Ноутбук", 75000L, 1);

        var mockViewModel = Mockito.mock(OrderViewModel.class);

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Mono.just(order));

        Mockito.when(orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId)).thenReturn(Flux.just(orderItem));

        Mockito.when(orderMapper.toViewModel(order, List.of(orderItem))).thenReturn(mockViewModel);

        StepVerifier.create(orderService.findById(orderId))
                .expectNext(mockViewModel)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет выбрасывание ResponseStatusException со статусом 404 из Mono.error(), если заказ не найден.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowNotFoundWhenOrderDoesNotExist() {
        var orderId = 999L;

        Mockito.when(orderRepository.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.findById(orderId))
                .expectErrorMatches(throwable ->
                        {
                            if (!(throwable instanceof ResponseStatusException) ||
                                    !((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND))
                                return false;
                            assert ((ResponseStatusException) throwable).getReason() != null;
                            return ((ResponseStatusException) throwable).getReason().equals("Order not found.");
                        }
                )
                .verify();

        Mockito.verifyNoInteractions(orderItemRepository, orderMapper);
    }

    // endregion
}