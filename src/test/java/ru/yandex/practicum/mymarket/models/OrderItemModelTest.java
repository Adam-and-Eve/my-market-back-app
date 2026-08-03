package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * <summary>
 * Юнит-тесты для проверки корректности инициализации состояния и сохранения исторических данных в сущности OrderItemModel.
 * </summary>
 **/
public class OrderItemModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет успешное создание новой позиции заказа со всеми переданными параметрами через публичный конструктор.
     * </summary>
     **/
    @Test
    public void constructorShouldCreateNewInstanceWithValidArguments() {
        var orderId = 1L;

        var title = "Клавиатура Novation Launchkey 88";

        var price = 45000L;

        var quantity = 2;

        var orderItem = new OrderItemModel(orderId, title, price, quantity);

        Assertions.assertNotNull(orderItem);

        Assertions.assertNull(orderItem.getId());

        Assertions.assertEquals(orderId, orderItem.getOrderId());

        Assertions.assertEquals(title, orderItem.getTitle());

        Assertions.assertEquals(price, orderItem.getPrice());

        Assertions.assertEquals(quantity, orderItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет работу защищенного конструктора по умолчанию, необходимого для восстановления состояния сущности из БД.
     * Сущность должна собираться с дефолтными значениями полей и null-идентификаторами.
     * </summary>
     **/
    @Test
    public void defaultConstructorShouldCreateInstanceWithDefaultState() {
        var orderItem = new OrderItemModel();

        Assertions.assertNotNull(orderItem);

        Assertions.assertNull(orderItem.getId());

        Assertions.assertNull(orderItem.getOrderId());

        Assertions.assertNull(orderItem.getTitle());

        Assertions.assertEquals(0L, orderItem.getPrice());

        Assertions.assertEquals(0, orderItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора заказа в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullOrderIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(null, "Товар", 100L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче пустого или null названия позиции в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullOrBlankTitleShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, null, 100L, 1);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, "", 100L, 1);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, "   ", 100L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче отрицательной цены в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNegativePriceShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, "Товар", -1L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче некорректного (меньше или равного нулю) количества в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithInvalidQuantityShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, "Товар", 100L, 0);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new OrderItemModel(1L, "Товар", 100L, -5);
        });
    }

    // endregion
}