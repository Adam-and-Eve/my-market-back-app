package ru.yandex.practicum.mymarket.viewmodels;

public record CachedItemViewModel(
        Long id,
        String title,
        String description,
        String imgPath,
        long price
) {
}