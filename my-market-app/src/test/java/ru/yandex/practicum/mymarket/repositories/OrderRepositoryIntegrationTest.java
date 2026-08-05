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

    // region Setup

    /**
     * <summary>
     * Очищает состояние базы данных перед каждым тестом для обеспечения их независимости.
     * </summary>
     **/
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
    }

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

    /**
     * <summary>
     * Проверяет, что запрос по статусу возвращает пустой список, если заказов с таким статусом в БД нет.
     * </summary>
     **/
    @Test
    void findAllByStatusOrderByIdAscShouldReturnEmptyListWhenNoMatchingOrdersExist()
    {
        var pendingOrder = OrderModel.create();

        orderRepository.save(pendingOrder).block();

        var orders = orderRepository.findAllByStatusOrderByIdAsc(OrderModel.STATUS_PAID).collectList().block();

        Assertions.assertNotNull(orders);

        Assertions.assertTrue(orders.isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает только те заказы, которые соответствуют переданному статусу.
     * </summary>
     **/
    @Test
    void findAllByStatusOrderByIdAscShouldReturnOnlyMatchingOrders()
    {
        var pendingOrder = OrderModel.create();

        var paidOrder1 = OrderModel.create();

        paidOrder1.markAsPaid();

        var failedOrder = OrderModel.create();

        failedOrder.markAsPaymentFailed();

        var paidOrder2 = OrderModel.create();

        paidOrder2.markAsPaid();

        orderRepository.save(pendingOrder).block();

        var savedPaid1 = orderRepository.save(paidOrder1).block();

        orderRepository.save(failedOrder).block();

        var savedPaid2 = orderRepository.save(paidOrder2).block();

        var paidOrders = orderRepository.findAllByStatusOrderByIdAsc(OrderModel.STATUS_PAID).collectList().block();

        Assertions.assertNotNull(paidOrders);

        Assertions.assertEquals(2, paidOrders.size());

        Assertions.assertTrue(paidOrders.get(0).getId() < paidOrders.get(1).getId());

        assert savedPaid1 != null;

        Assertions.assertEquals(savedPaid1.getId(), paidOrders.get(0).getId());

        assert savedPaid2 != null;

        Assertions.assertEquals(savedPaid2.getId(), paidOrders.get(1).getId());

        for (var order : paidOrders) {
            Assertions.assertEquals(OrderModel.STATUS_PAID, order.getStatus());
        }
    }

    // endregion
}