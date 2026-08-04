package ru.yandex.practicum.mymarket.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * <summary>
 * Модульные тесты для проверки корректности работы CatalogHelper.
 * </summary>
 **/
public class CatalogHelperTest {

    // region Fields

    private CatalogHelper catalogHelper;

    // endregion

    // region Setup

    @BeforeEach
    public void setUp() {
        catalogHelper = new CatalogHelper();
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет сброс размера страницы на значение по умолчанию при значении null.
     * </summary>
     **/
    @Test
    public void normalizePageSizeShouldReturnDefaultWhenPageSizeIsNull() {
        var result = catalogHelper.normalizePageSize(null);

        Assertions.assertEquals(5, result);
    }

    /**
     * <summary>
     * Проверяет сброс размера страницы на значение по умолчанию при значениях меньше 1 (включая 0).
     * </summary>
     **/
    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    public void normalizePageSizeShouldReturnDefaultWhenPageSizeIsLessThanOne(final int invalidPageSize) {
        var result = catalogHelper.normalizePageSize(invalidPageSize);

        Assertions.assertEquals(5, result);
    }

    /**
     * <summary>
     * Проверяет обрезку размера страницы до максимального значения 100 при передаче 1 000 000.
     * </summary>
     **/
    @Test
    public void normalizePageSizeShouldCapAtMaxPageSizeWhenExceedsLimit() {
        var hugePageSize = 1_000_000;

        var result = catalogHelper.normalizePageSize(hugePageSize);

        Assertions.assertEquals(100, result);
    }

    /**
     * <summary>
     * Проверяет сохранение запрошенного значения, если оно находится в допустимом диапазоне.
     * </summary>
     **/
    @Test
    public void normalizePageSizeShouldReturnSameValueWhenWithinValidRange() {
        var validPageSize = 20;

        var result = catalogHelper.normalizePageSize(validPageSize);

        Assertions.assertEquals(validPageSize, result);
    }

    // endregion
}