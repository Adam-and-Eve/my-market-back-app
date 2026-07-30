package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <summary>
 * Модульные тесты для проверки корректности инициализации состояния и работы свойств сущности ItemModel.
 * </summary>
 **/
public class ItemModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет успешное создание нового товара со всеми переданными параметрами через публичный конструктор.
     * </summary>
     **/
    @Test
    void constructorShouldCreateNewInstanceWithValidArguments()
    {
        var title = "Клавиатура Novation Launchkey 88";

        var description = "MIDI-контроллер с полувзвешенной механикой.";

        var imgPath = "/images/items/novation_88.png";

        var price = 45000L;

        var item = new ItemModel(title, description, imgPath, price);

        Assertions.assertNotNull(item);

        Assertions.assertNull(item.getId());

        Assertions.assertEquals(title, item.getTitle());

        Assertions.assertEquals(description, item.getDescription());

        Assertions.assertEquals(imgPath, item.getImgPath());

        Assertions.assertEquals(price, item.getPrice());
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
        var item = new ItemModel();

        Assertions.assertNotNull(item);

        Assertions.assertNull(item.getId());

        Assertions.assertNull(item.getTitle());

        Assertions.assertNull(item.getDescription());

        Assertions.assertNull(item.getImgPath());

        Assertions.assertEquals(0L, item.getPrice());
    }

    /**
     * <summary>
     * Проверяет граничный сценарий инициализации товара с нулевой стоимостью для промо-акций.
     * </summary>
     **/
    @Test
    void constructorShouldAllowZeroPrice()
    {
        var title = "Подарочный стикер";

        var description = "Промо-наклейка к заказу.";

        var imgPath = "/images/items/sticker.png";

        var zeroPrice = 0L;

        var item = new ItemModel(title, description, imgPath, zeroPrice);

        Assertions.assertNotNull(item);

        Assertions.assertEquals(zeroPrice, item.getPrice());
    }

    // endregion
}