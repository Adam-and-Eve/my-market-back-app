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

        Assertions.assertNull(cartItem.getUserId());

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
     * Проверяет успешное создание нового элемента корзины через конструктор с указанием идентификаторов пользователя и товара.
     * При этом транзиентная ссылка на доменную модель товара должна оставаться null.
     * </summary>
     **/
    @Test
    public void constructorWithUserIdAndItemIdShouldCreateNewInstanceWithValidArguments() {
        var userId = 10L;

        var itemId = 42L;

        var quantity = 2;

        var cartItem = new CartItemModel(userId, itemId, quantity);

        Assertions.assertNotNull(cartItem);

        Assertions.assertNull(cartItem.getId());

        Assertions.assertEquals(userId, cartItem.getUserId());

        Assertions.assertEquals(itemId, cartItem.getItemId());

        Assertions.assertNull(cartItem.getItem());

        Assertions.assertEquals(quantity, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора покупателя в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullUserIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(null, 42L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора товара в конструктор с идентификатором пользователя.
     * </summary>
     **/
    @Test
    public void constructorWithNullItemIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(10L, null, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче отрицательного количества товара в конструктор с идентификаторами пользователя и товара.
     * </summary>
     **/
    @Test
    public void constructorWithNegativeQuantityAndUserIdAndItemIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(10L, 42L, -1);
        });
    }

    /**
     * <summary>
     * Проверяет успешное создание сущности через полный конструктор (PersistenceCreator), используемый при маппинге из БД.
     * </summary>
     **/
    @Test
    public void constructorWithAllArgumentsShouldCreateInstanceWithValidState() {
        var id = 1L;

        var userId = 10L;

        var itemId = 42L;

        var quantity = 5;

        var cartItem = new CartItemModel(id, userId, itemId, quantity);

        Assertions.assertNotNull(cartItem);

        Assertions.assertEquals(id, cartItem.getId());

        Assertions.assertEquals(userId, cartItem.getUserId());

        Assertions.assertEquals(itemId, cartItem.getItemId());

        Assertions.assertNull(cartItem.getItem());

        Assertions.assertEquals(quantity, cartItem.getQuantity());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора записи в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(null, 10L, 42L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора покупателя в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullUserIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(1L, null, 42L, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null-идентификатора товара в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullItemIdShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(1L, 10L, null, 1);
        });
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче отрицательного количества товара в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNegativeQuantityShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CartItemModel(1L, 10L, 42L, -1);
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

        Assertions.assertNull(cartItem.getUserId());

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

    // endregion
}