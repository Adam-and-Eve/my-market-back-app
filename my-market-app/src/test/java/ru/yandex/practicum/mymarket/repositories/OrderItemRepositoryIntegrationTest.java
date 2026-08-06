package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.OrderItemModel;
import ru.yandex.practicum.mymarket.models.OrderModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения реактивных запросов в OrderItemRepository.
 * </summary>
 **/
public class OrderItemRepositoryIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет успешную выборку всех товарных позиций конкретного заказа с гарантированной сортировкой по возрастанию ID записи.
     * </summary>
     **/
    @Test
    void findAllByOrderIdOrderByIdAscShouldReturnItemsOrderedByIdWhenOrderExists()
    {
        var order = OrderModel.create();

        var savedOrder = orderRepository.save(order).block();

        Assertions.assertNotNull(savedOrder);

        var orderId = savedOrder.getId();

        var item1 = new OrderItemModel(orderId, "Клавиатура Novation Launchkey 88", 45000L, 1);

        var item2 = new OrderItemModel(orderId, "Кабель USB-B 3.0", 1500L, 2);

        var item3 = new OrderItemModel(orderId, "Студийные мониторы", 70000L, 1);

        var savedItems = orderItemRepository.saveAll(List.of(item1, item2, item3)).collectList().block();

        Assertions.assertNotNull(savedItems);

        var resultList = orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertEquals(3, resultList.size());

        Assertions.assertEquals(orderId, resultList.get(0).getOrderId());

        Assertions.assertEquals(orderId, resultList.get(1).getOrderId());

        Assertions.assertEquals(orderId, resultList.get(2).getOrderId());

        Assertions.assertTrue(resultList.get(0).getId() < resultList.get(1).getId());

        Assertions.assertTrue(resultList.get(1).getId() < resultList.get(2).getId());

        Assertions.assertEquals("Клавиатура Novation Launchkey 88", resultList.get(0).getTitle());

        Assertions.assertEquals("Кабель USB-B 3.0", resultList.get(1).getTitle());

        Assertions.assertEquals("Студийные мониторы", resultList.get(2).getTitle());
    }

    /**
     * <summary>
     * Проверяет, что при поиске позиций для несуществующего идентификатора заказа возвращается пустой реактивный поток Flux.
     * </summary>
     **/
    @Test
    void findAllByOrderIdOrderByIdAscShouldReturnEmptyFluxWhenNoItemsExistForOrder()
    {
        var nonExistentOrderId = 999L;

        var resultList = orderItemRepository.findAllByOrderIdOrderByIdAsc(nonExistentOrderId).collectList().block();

        Assertions.assertNotNull(resultList);

        Assertions.assertTrue(resultList.isEmpty());
    }

    // endregion
}