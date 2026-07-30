package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <summary>
 * Юнит-тесты для проверки безопасного фабричного метода преобразования строк в перечисление ItemSortEnumModel.
 * </summary>
 **/
public class ItemSortEnumModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет, что метод возвращает значение по умолчанию (NO), если переданная строка равна null.
     * </summary>
     **/
    @Test
    void fromShouldReturnNoWhenValueIsNull()
    {
        String value = null;

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.NO, result);
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает значение по умолчанию (NO), если переданная строка пустая или состоит из пробелов.
     * </summary>
     **/
    @Test
    void fromShouldReturnNoWhenValueIsBlank()
    {
        var value = "   ";

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.NO, result);
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает значение по умолчанию (NO), если передано невалидное или неизвестное наименование стратегии.
     * </summary>
     **/
    @Test
    void fromShouldReturnNoWhenValueIsUnknown()
    {
        var value = "BY_RATING_DESC";

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.NO, result);
    }

    /**
     * <summary>
     * Проверяет успешное преобразование валидной строки в значение ALPHA без учета регистра символов.
     * </summary>
     **/
    @Test
    void fromShouldReturnAlphaWhenValueIsValidCaseInsensitive()
    {
        var value = "aLpHa";

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.ALPHA, result);
    }

    /**
     * <summary>
     * Проверяет успешное преобразование валидной строки в значение PRICE без учета регистра символов.
     * </summary>
     **/
    @Test
    void fromShouldReturnPriceWhenValueIsValidCaseInsensitive()
    {
        var value = "price";

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.PRICE, result);
    }

    /**
     * <summary>
     * Проверяет успешное преобразование явной строки со значением NO в соответствующий элемент перечисления.
     * </summary>
     **/
    @Test
    void fromShouldReturnNoWhenValueIsValidNo()
    {
        var value = "no";

        var result = ItemSortEnumModel.from(value);

        Assertions.assertEquals(ItemSortEnumModel.NO, result);
    }

    // endregion
}