package ru.yandex.practicum.mymarket.viewmodels;

import ru.yandex.practicum.mymarket.models.ItemModel;

public record PreparedCartItemViewModel(
        ItemModel item,
        int quantity
) {
}