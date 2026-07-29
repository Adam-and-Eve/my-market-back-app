package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления (View Model) для отображения отдельного товара в интерфейсе.
 * Содержит очищенные и подготовленные для UI-слоя данные о конкретной позиции каталога.
 * </summary>
 **/
public record ItemViewModel(
        Long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {

    // region Methods

    /**
     * <summary>
     * Создает пустой объект-заглушку (placeholder) модели представления товара.
     * Используется для заполнения пустых ячеек в сеточной разметке шаблона или инициализации дефолтных состояний.
     * </summary>
     * <return>
     * @return Экземпляр ItemViewModel с инициализированными дефолтными значениями и отрицательным ID.
     * </return>
     **/
    public static ItemViewModel placeholder(){
        return new ItemViewModel(-1L, "", "", "", 0, 0);
    }

    // endregion
}