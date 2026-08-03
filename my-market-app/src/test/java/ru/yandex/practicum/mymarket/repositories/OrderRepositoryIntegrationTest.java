package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.OrderModel;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения сортировки запросов в OrderRepository.
 * </summary>
 **/
public class OrderRepositoryIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @Autowired
    private OrderRepository orderRepository;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет, что при отсутствии заказов в базе данных реактивный запрос возвращает пустой список.
     * </summary>
     **/
    @Test
    void findAllByOrderByIdAscShouldReturnEmptyListWhenNoOrdersExist()
    {
        var orders = orderRepository.findAllByOrderByIdAsc().collectList().block();

        Assertions.assertNotNull(orders);

        Assertions.assertTrue(orders.isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что все извлеченные заказы возвращаются строго отсортированными по возрастанию их уникального идентификатора.
     * </summary>
     **/
    @Test
    void findAllByOrderByIdAscShouldReturnOrdersSortedByIdAscending()
    {
        var firstOrder = OrderModel.create();

        var secondOrder = OrderModel.create();

        var thirdOrder = OrderModel.create();

        var savedFirst = orderRepository.save(firstOrder).block();

        var savedSecond = orderRepository.save(secondOrder).block();

        var savedThird = orderRepository.save(thirdOrder).block();

        Assertions.assertNotNull(savedFirst);

        Assertions.assertNotNull(savedSecond);

        Assertions.assertNotNull(savedThird);

        var sortedOrders = orderRepository.findAllByOrderByIdAsc().collectList().block();

        Assertions.assertNotNull(sortedOrders);

        Assertions.assertEquals(3, sortedOrders.size());

        Assertions.assertTrue(sortedOrders.get(0).getId() < sortedOrders.get(1).getId());

        Assertions.assertTrue(sortedOrders.get(1).getId() < sortedOrders.get(2).getId());

        Assertions.assertEquals(savedFirst.getId(), sortedOrders.get(0).getId());

        Assertions.assertEquals(savedSecond.getId(), sortedOrders.get(1).getId());

        Assertions.assertEquals(savedThird.getId(), sortedOrders.get(2).getId());
    }

    // endregion
}