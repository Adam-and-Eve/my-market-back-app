package ru.yandex.practicum.mymarket.models;

/**
 * <summary>
 * Перечисление, представляющее стратегии сортировки товаров.
 * Определяет поддерживаемые варианты упорядочивания элементов при просмотре каталога.
 * </summary>
 **/
public enum ItemSortEnumModel {
    /**
     * Сортировка отсутствует (порядок по умолчанию).
     **/
    NO,

    /**
     * Сортировка по алфавиту (в порядке возрастания наименования).
     **/
    ALPHA,

    /**
     * Сортировка по стоимости (в порядке возрастания цены).
     **/
    PRICE;

    /**
     * <summary>
     * Безопасно преобразует строковое представление сортировки в элемент перечисления.
     * В случае передачи null, пустой строки или неизвестного значения возвращает вариант по умолчанию (NO).
     * </summary>
     * @param value Строковое наименование стратегии сортировки.
     * <return>
     * @return Соответствующий элемент перечисления ItemSortEnumModel.
     * </return>
     **/
    public static ItemSortEnumModel from(final String value) {

        if (value == null || value.isBlank()) {
            return NO;
        }

        try {
            return ItemSortEnumModel.valueOf(value.toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            return NO;
        }
    }
}