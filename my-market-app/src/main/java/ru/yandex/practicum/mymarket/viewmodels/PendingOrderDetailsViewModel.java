package ru.yandex.practicum.mymarket.viewmodels;

import ru.yandex.practicum.mymarket.models.OrderModel;

public record PendingOrderDetailsViewModel(
        OrderModel order, long totalAmount
) {
}