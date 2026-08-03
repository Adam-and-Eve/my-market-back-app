package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления для ячейки сетки каталога.
 * </summary>
 **/
public class CatalogCellViewModel {

    // region Fields

    private final ItemViewModel item;

    private final boolean placeholder;

    // endregion

    // region Constructors

    private CatalogCellViewModel(
            final ItemViewModel item,
            final boolean placeholder) {

        this.item = item;

        this.placeholder = placeholder;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Создает ячейку с реальным товаром.
     * </summary>
     * @param item Модель представления товара.
     * @return Экземпляр CatalogCellViewModel, содержащий товар.
     **/
    public static CatalogCellViewModel of(final ItemViewModel item) {
        return new CatalogCellViewModel(item, false);
    }

    /**
     * <summary>
     * Создает пустую ячейку-заглушку для выравнивания сеточной разметки.
     * </summary>
     * @return Экземпляр CatalogCellViewModel-заглушки.
     **/
    public static CatalogCellViewModel placeholder() {
        return new CatalogCellViewModel(null, true);
    }

    /**
     * <summary>
     * Возвращает модель представления товара.
     * </summary>
     **/
    public ItemViewModel getItem() {
        return item;
    }

    /**
     * <summary>
     * Проверяет, является ли ячейка заглушкой.
     * </summary>
     **/
    public boolean isPlaceholder() {
        return placeholder;
    }

    // endregion
}