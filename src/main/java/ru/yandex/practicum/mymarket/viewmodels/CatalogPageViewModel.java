package ru.yandex.practicum.mymarket.viewmodels;

import java.util.List;

/**
 * <summary>
 * Модель представления (View Model) страницы каталога товаров.
 * Аккумулирует в себе сетку отфильтрованных товаров, параметры текущего поиска,
 * примененную сортировку и состояние постраничной навигации для передачи в UI-слой.
 * </summary>
 **/
public record CatalogPageViewModel(
        List<List<CatalogCellViewModel>> items,
        String search,
        String sort,
        PagingViewModel paging
) {
}