package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <summary>
 * Юнит-тесты для проверки фабричных методов, инициализации состояния и управления позициями в сущности OrderModel.
 * </summary>
 **/
public class OrderModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет работу защищенного конструктора по умолчанию, необходимого для JPA-провайдера.
     * Сущность должна собираться с дефолтными значениями полей, пустым списком позиций и null-идентификатором.
     * </summary>
     **/
    @Test
    void defaultConstructorShouldCreateInstanceWithDefaultState()
    {
        var order = new OrderModel();

        Assertions.assertNotNull(order);

        Assertions.assertNull(order.getId());

        Assertions.assertNotNull(order.getItems());

        Assertions.assertTrue(order.getItems().isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что статический фабричный метод успешно создает новый пустой экземпляр заказа.
     * </summary>
     **/
    @Test
    void createShouldReturnNewInstanceWithEmptyItems()
    {
        var order = OrderModel.create();

        Assertions.assertNotNull(order);

        Assertions.assertNull(order.getId());

        Assertions.assertNotNull(order.getItems());

        Assertions.assertTrue(order.getItems().isEmpty());
    }

    /**
     * <summary>
     * Проверяет успешное создание и добавление исторической позиции в список заказа на основе текущих метрик товара.
     * </summary>
     **/
    @Test
    void addItemShouldCreateAndAddOrderItemWithHistoricalData()
    {
        var order = OrderModel.create();

        var item = new ItemModel("Клавиатура Novation", "MIDI-контроллер", "/images/novation.png", 45000L);

        var quantity = 2;

        order.addItem(item, quantity);

        var items = order.getItems();

        Assertions.assertEquals(1, items.size());

        var addedItem = items.getFirst();

        Assertions.assertEquals(item.getTitle(), addedItem.getTitle());

        Assertions.assertEquals(item.getPrice(), addedItem.getPrice());

        Assertions.assertEquals(quantity, addedItem.getQuantity());
    }

    // endregion
}