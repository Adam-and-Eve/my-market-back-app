package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

/**
 * <summary>
 * Юнит-тесты для проверки фабричных методов, инициализации состояния и управления позициями в сущности OrderModel.
 * </summary>
 **/
public class OrderModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет работу защищенного конструктора по умолчанию, необходимого для корректного восстановления сущности.
     * Сущность должна собираться с дефолтными значениями полей, текущей датой создания, статусом, пустым списком позиций и null-идентификаторами.
     * </summary>
     **/
    @Test
    public void defaultConstructorShouldCreateInstanceWithDefaultState() {
        var order = new OrderModel();

        Assertions.assertNotNull(order);

        Assertions.assertNull(order.getId());

        Assertions.assertNull(order.getUserId());

        Assertions.assertNotNull(order.getCreatedAt());

        Assertions.assertEquals(OrderModel.STATUS_PENDING, order.getStatus());

        Assertions.assertNotNull(order.getItems());

        Assertions.assertTrue(order.getItems().isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что статический фабричный метод успешно создает новый пустой экземпляр заказа с привязанным идентификатором пользователя.
     * </summary>
     **/
    @Test
    public void createShouldReturnNewInstanceWithValidUserId() {
        var userId = 10L;

        var order = OrderModel.create(userId);

        Assertions.assertNotNull(order);

        Assertions.assertNull(order.getId());

        Assertions.assertEquals(userId, order.getUserId());

        Assertions.assertNotNull(order.getCreatedAt());

        Assertions.assertEquals(OrderModel.STATUS_PENDING, order.getStatus());

        Assertions.assertNotNull(order.getItems());

        Assertions.assertTrue(order.getItems().isEmpty());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора пользователя в фабричный метод create.
     * </summary>
     **/
    @Test
    public void createWithNullUserIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            OrderModel.create(null);
        });
    }

    /**
     * <summary>
     * Проверяет успешное создание сущности через полный конструктор (PersistenceCreator), используемый при маппинге из БД.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithValidArgumentsShouldCreateInstance() {
        var id = 1L;

        var userId = 10L;

        var status = OrderModel.STATUS_PAID;

        var createdAt = LocalDateTime.now().minusDays(1);

        var order = new OrderModel(id, userId, status, createdAt);

        Assertions.assertNotNull(order);

        Assertions.assertEquals(id, order.getId());

        Assertions.assertEquals(userId, order.getUserId());

        Assertions.assertEquals(status, order.getStatus());

        Assertions.assertEquals(createdAt, order.getCreatedAt());

        Assertions.assertNotNull(order.getItems());

        Assertions.assertTrue(order.getItems().isEmpty());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора заказа в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderModel(null, 10L, OrderModel.STATUS_PENDING, LocalDateTime.now());
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора пользователя в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullUserIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderModel(1L, null, OrderModel.STATUS_PENDING, LocalDateTime.now());
        });
    }

    /**
     * <summary>
     * Проверяет корректность перевода заказа в статус успешной оплаты.
     * </summary>
     **/
    @Test
    public void markAsPaidShouldUpdateStatusToPaid() {
        var order = OrderModel.create(10L);

        order.markAsPaid();

        Assertions.assertEquals(OrderModel.STATUS_PAID, order.getStatus());
    }

    /**
     * <summary>
     * Проверяет корректность перевода заказа в статус ошибки оплаты.
     * </summary>
     **/
    @Test
    public void markAsPaymentFailedShouldUpdateStatusToPaymentFailed() {
        var order = OrderModel.create(10L);

        order.markAsPaymentFailed();

        Assertions.assertEquals(OrderModel.STATUS_PAYMENT_FAILED, order.getStatus());
    }

    /**
     * <summary>
     * Проверяет успешное создание и добавление исторической позиции в транзиентный список заказа на основе текущих метрик товара.
     * </summary>
     **/
    @Test
    public void addItemShouldCreateAndAddOrderItemWithHistoricalData() {
        var order = OrderModel.create(10L);

        ReflectionTestUtils.setField(order, "id", 1L);

        var item = new ItemModel("Клавиатура Novation", "MIDI-контроллер", "/images/novation.png", 45000L);

        var quantity = 2;

        order.addItem(item, quantity);

        var items = order.getItems();

        Assertions.assertEquals(1, items.size());

        var addedItem = items.getFirst();

        Assertions.assertEquals(order.getId(), addedItem.getOrderId());

        Assertions.assertEquals(item.getTitle(), addedItem.getTitle());

        Assertions.assertEquals(item.getPrice(), addedItem.getPrice());

        Assertions.assertEquals(quantity, addedItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при попытке добавить null-товар в заказ.
     * </summary>
     **/
    @Test
    public void addItemWithNullItemShouldThrowException() {
        var order = OrderModel.create(10L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            order.addItem(null, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при попытке добавить товар с неверным количеством (ноль или меньше).
     * </summary>
     **/
    @Test
    public void addItemWithInvalidQuantityShouldThrowException() {
        var order = OrderModel.create(10L);

        var item = new ItemModel("Клавиатура Novation", "MIDI-контроллер", "/images/novation.png", 45000L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            order.addItem(item, 0);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            order.addItem(item, -3);
        });
    }

    // endregion
}