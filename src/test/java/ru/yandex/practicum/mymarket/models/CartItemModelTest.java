package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <summary>
 * Юнит-тесты для проверки работы свойств, управления количеством и инициализации состояния сущности CartItemModel.
 * </summary>
 **/
public class CartItemModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет успешное создание нового элемента корзины со всеми переданными параметрами через публичный конструктор.
     * </summary>
     **/
    @Test
    void constructorShouldCreateNewInstanceWithValidArguments()
    {
        var item = new ItemModel("Клавиатура Novation", "MIDI-контроллер", "/images/novation.png", 45000L);

        var quantity = 3;

        var cartItem = new CartItemModel(item, quantity);

        Assertions.assertNotNull(cartItem);

        Assertions.assertEquals(item, cartItem.getItem());

        Assertions.assertEquals(quantity, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет работу защищенного конструктора по умолчанию, необходимого для JPA-провайдера.
     * Сущность должна собираться с дефолтными значениями полей и null-идентификатором.
     * </summary>
     **/
    @Test
    void defaultConstructorShouldCreateInstanceWithDefaultState()
    {
        var cartItem = new CartItemModel();

        Assertions.assertNotNull(cartItem);

        Assertions.assertNull(cartItem.getItem());

        Assertions.assertEquals(0, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет успешное увеличение количества единиц товара в корзине на одну единицу.
     * </summary>
     **/
    @Test
    void increaseShouldIncrementQuantity()
    {
        var item = new ItemModel("Товар", "Описание", "/path.png", 100L);

        var cartItem = new CartItemModel(item, 1);

        cartItem.increase();

        Assertions.assertEquals(2, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет успешное уменьшение количества единиц товара в корзине на одну единицу, если текущее количество больше нуля.
     * </summary>
     **/
    @Test
    void decreaseShouldDecrementQuantityWhenQuantityIsGreaterThanZero()
    {
        var item = new ItemModel("Товар", "Описание", "/path.png", 100L);

        var cartItem = new CartItemModel(item, 5);

        cartItem.decrease();

        Assertions.assertEquals(4, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет защиту от ухода количества в отрицательный диапазон при вызове уменьшения, если текущее количество равно нулю.
     * </summary>
     **/
    @Test
    void decreaseShouldNotDecrementQuantityWhenQuantityIsZero()
    {
        var item = new ItemModel("Товар", "Описание", "/path.png", 100L);

        var cartItem = new CartItemModel(item, 0);

        cartItem.decrease();

        Assertions.assertEquals(0, cartItem.getQuantity());
    }

    // endregion
}