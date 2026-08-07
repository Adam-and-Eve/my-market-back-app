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

    // region Constants

    private static final long USER_ID_1 = 1L;

    private static final long USER_ID_2 = 2L;

    // endregion

    // region Fields

    @Autowired
    private OrderRepository orderRepository;

    // endregion

    // region Setup

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет, что при отсутствии заказов для конкретного пользователя реактивный запрос возвращает пустой список.
     * </summary>
     **/
    @Test
    void findAllByUserIdOrderByIdAscShouldReturnEmptyListWhenNoOrdersExist() {
        var orders = orderRepository.findAllByUserIdOrderByIdAsc(USER_ID_1).collectList().block();

        Assertions.assertNotNull(orders);

        Assertions.assertTrue(orders.isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что заказы конкретного пользователя возвращаются строго отсортированными по возрастанию ID,
     * а заказы других пользователей не попадают в выборку.
     * </summary>
     **/
    @Test
    void findAllByUserIdOrderByIdAscShouldReturnOrdersSortedByIdAscendingForSpecificUser() {
        var firstOrderUser1 = OrderModel.create(USER_ID_1);

        var secondOrderUser1 = OrderModel.create(USER_ID_1);

        var orderUser2 = OrderModel.create(USER_ID_2);

        var thirdOrderUser1 = OrderModel.create(USER_ID_1);

        var savedFirst = orderRepository.save(firstOrderUser1).block();

        var savedSecond = orderRepository.save(secondOrderUser1).block();

        orderRepository.save(orderUser2).block();

        var savedThird = orderRepository.save(thirdOrderUser1).block();

        Assertions.assertNotNull(savedFirst);

        Assertions.assertNotNull(savedSecond);

        Assertions.assertNotNull(savedThird);

        var sortedOrders = orderRepository.findAllByUserIdOrderByIdAsc(USER_ID_1).collectList().block();

        Assertions.assertNotNull(sortedOrders);

        Assertions.assertEquals(3, sortedOrders.size());

        var allBelongToUser1 = sortedOrders.stream().allMatch(o -> o.getUserId().equals(USER_ID_1));

        Assertions.assertTrue(allBelongToUser1);

        Assertions.assertTrue(sortedOrders.get(0).getId() < sortedOrders.get(1).getId());

        Assertions.assertTrue(sortedOrders.get(1).getId() < sortedOrders.get(2).getId());

        Assertions.assertEquals(savedFirst.getId(), sortedOrders.get(0).getId());

        Assertions.assertEquals(savedSecond.getId(), sortedOrders.get(1).getId());

        Assertions.assertEquals(savedThird.getId(), sortedOrders.get(2).getId());
    }

    /**
     * <summary>
     * Проверяет, что запрос по пользователю и статусу возвращает пустой список, если подпадающих заказов нет в БД.
     * </summary>
     **/
    @Test
    void findAllByUserIdAndStatusOrderByIdAscShouldReturnEmptyListWhenNoMatchingOrdersExist() {
        var pendingOrder = OrderModel.create(USER_ID_1);

        orderRepository.save(pendingOrder).block();

        var orders = orderRepository.findAllByUserIdAndStatusOrderByIdAsc(USER_ID_1, OrderModel.STATUS_PAID).collectList().block();

        Assertions.assertNotNull(orders);

        Assertions.assertTrue(orders.isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает только те заказы, которые принадлежат указанному пользователю и соответствуют фильтру по статусу.
     * </summary>
     **/
    @Test
    void findAllByUserIdAndStatusOrderByIdAscShouldReturnOnlyMatchingOrdersForSpecificUser() {
        var pendingOrder = OrderModel.create(USER_ID_1);

        var paidOrder1User1 = OrderModel.create(USER_ID_1);

        paidOrder1User1.markAsPaid();

        var failedOrder = OrderModel.create(USER_ID_1);

        failedOrder.markAsPaymentFailed();

        var paidOrder2User1 = OrderModel.create(USER_ID_1);
        paidOrder2User1.markAsPaid();

        var paidOrderUser2 = OrderModel.create(USER_ID_2);

        paidOrderUser2.markAsPaid();

        orderRepository.save(pendingOrder).block();

        var savedPaid1 = orderRepository.save(paidOrder1User1).block();
        orderRepository.save(failedOrder).block();

        var savedPaid2 = orderRepository.save(paidOrder2User1).block();

        orderRepository.save(paidOrderUser2).block();

        var paidOrders = orderRepository.findAllByUserIdAndStatusOrderByIdAsc(USER_ID_1, OrderModel.STATUS_PAID).collectList().block();

        Assertions.assertNotNull(paidOrders);

        Assertions.assertEquals(2, paidOrders.size());

        var allBelongToUser1 = paidOrders.stream().allMatch(o -> o.getUserId().equals(USER_ID_1));

        Assertions.assertTrue(allBelongToUser1);

        Assertions.assertTrue(paidOrders.get(0).getId() < paidOrders.get(1).getId());

        Assertions.assertNotNull(savedPaid1);

        Assertions.assertEquals(savedPaid1.getId(), paidOrders.get(0).getId());

        Assertions.assertNotNull(savedPaid2);

        Assertions.assertEquals(savedPaid2.getId(), paidOrders.get(1).getId());

        for (var order : paidOrders) {
            Assertions.assertEquals(OrderModel.STATUS_PAID, order.getStatus());
        }
    }

    /**
     * <summary>
     * Проверяет поиск заказа по его идентификатору и идентификатору владельца, когда заказ принадлежит данному пользователю.
     * </summary>
     **/
    @Test
    void findByIdAndUserIdShouldReturnOrderWhenOrderBelongsToUser() {
        var order = OrderModel.create(USER_ID_1);

        var savedOrder = orderRepository.save(order).block();

        Assertions.assertNotNull(savedOrder);

        var result = orderRepository.findByIdAndUserId(savedOrder.getId(), USER_ID_1).blockOptional();

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(savedOrder.getId(), result.get().getId());

        Assertions.assertEquals(USER_ID_1, result.get().getUserId());
    }

    /**
     * <summary>
     * Проверяет, что поиск заказа возвращает empty, если заказ принадлежит другому пользователю или не существует.
     * </summary>
     **/
    @Test
    void findByIdAndUserIdShouldReturnEmptyWhenOrderBelongsToAnotherUserOrDoesNotExist() {
        var order = OrderModel.create(USER_ID_1);

        var savedOrder = orderRepository.save(order).block();

        Assertions.assertNotNull(savedOrder);

        var resultOtherUser = orderRepository.findByIdAndUserId(savedOrder.getId(), USER_ID_2).blockOptional();

        Assertions.assertTrue(resultOtherUser.isEmpty());

        var resultNonExistent = orderRepository.findByIdAndUserId(999L, USER_ID_1).blockOptional();

        Assertions.assertTrue(resultNonExistent.isEmpty());
    }

    // endregion
}