package ru.yandex.practicum.mymarket.viewmodels;

import java.util.List;

public record CachedItemsViewModel(
        List<CachedItemViewModel> items
) {
}