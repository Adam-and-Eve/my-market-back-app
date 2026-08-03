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



    // endregion
}