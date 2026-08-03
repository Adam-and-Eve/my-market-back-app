package ru.yandex.practicum.mymarket.viewmodels;

import java.util.List;

/**
 * <summary>
 * Модель представления страницы корзины покупателя.
 * </summary>
 **/
public record CartPageViewModel (
        List<ItemViewModel> items,
        long total
) {
}