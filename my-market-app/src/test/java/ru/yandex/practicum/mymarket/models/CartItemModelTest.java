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
     * Проверяет успешное создание нового элемента корзины через конструктор со связанной доменной моделью товара.
     * Идентификатор itemId должен автоматически синхронизироваться с идентификатором переданного товара.
     * </summary>
     **/
    @Test
    public void constructorWithItemShouldCreateNewInstanceWithValidArguments() {
        var item = new ItemModel("Клавиатура Novation", "MIDI-контроллер", "/images/novation.png", 45000L);

        var quantity = 3;

        var cartItem = new CartItemModel(item, quantity);

        Assertions.assertNotNull(cartItem);

        Assertions.assertNull(cartItem.getId());

        Assertions.assertEquals(item, cartItem.getItem());

        Assertions.assertEquals(item.getId(), cartItem.getItemId());

        Assertions.assertEquals(quantity, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-модели товара в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullItemShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel((ItemModel) null, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче отрицательного количества товара в конструктор с моделью.
     * </summary>
     **/
    @Test
    public void constructorWithNegativeQuantityAndItemShouldThrowException() {
        var item = new ItemModel("Товар", "Описание", "/path.png", 100L);

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(item, -1);
        });
    }

    /**
     * <summary>
     * Проверяет успешное создание нового элемента корзины через конструктор с прямым указанием идентификатора товара.
     * При этом транзиентная ссылка на доменную модель товара должна оставаться null.
     * </summary>
     **/
    @Test
    public void constructorWithItemIdShouldCreateNewInstanceWithValidArguments() {
        var itemId = 42L;

        var quantity = 2;

        var cartItem = new CartItemModel(itemId, quantity);

        Assertions.assertNotNull(cartItem);

        Assertions.assertNull(cartItem.getId());

        Assertions.assertEquals(itemId, cartItem.getItemId());

        Assertions.assertNull(cartItem.getItem());

        Assertions.assertEquals(quantity, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора товара в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullItemIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel((Long) null, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче отрицательного количества товара в конструктор с идентификатором.
     * </summary>
     **/
    @Test
    public void constructorWithNegativeQuantityAndItemIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(42L, -1);
        });
    }

    /**
     * <summary>
     * Проверяет работу защищенного конструктора по умолчанию, необходимого для маппинга данных репозиторием.
     * Сущность должна собираться с дефолтными значениями полей, null-идентификаторами и нулевым количеством.
     * </summary>
     **/
    @Test
    public void defaultConstructorShouldCreateInstanceWithDefaultState() {
        var cartItem = new CartItemModel();

        Assertions.assertNotNull(cartItem);

        Assertions.assertNull(cartItem.getId());

        Assertions.assertNull(cartItem.getItemId());

        Assertions.assertNull(cartItem.getItem());

        Assertions.assertEquals(0, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет успешное увеличение количества единиц товара в корзине на одну единицу.
     * </summary>
     **/
    @Test
    public void increaseShouldIncrementQuantity() {
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
    public void decreaseShouldDecrementQuantityWhenQuantityIsGreaterThanZero() {
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
    public void decreaseShouldNotDecrementQuantityWhenQuantityIsZero() {
        var item = new ItemModel("Товар", "Описание", "/path.png", 100L);

        var cartItem = new CartItemModel(item, 0);

        cartItem.decrease();

        Assertions.assertEquals(0, cartItem.getQuantity());
    }

    // endregion
}