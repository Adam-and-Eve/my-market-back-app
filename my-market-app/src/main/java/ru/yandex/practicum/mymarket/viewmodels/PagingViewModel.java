package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления (View Model) постраничной навигации.
 * Хранит параметры текущей страницы каталога товаров и флаги доступности переходов для UI-слоя.
 * </summary>
 **/
public record PagingViewModel(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {
}