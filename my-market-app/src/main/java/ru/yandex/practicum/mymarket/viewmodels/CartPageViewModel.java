package ru.yandex.practicum.mymarket.viewmodels;

import java.util.List;

/**
 * <summary>
 * Модель представления страницы корзины покупателя.
 * </summary>
 **/
public record CartPageViewModel (
        List<ItemViewModel> items,
        long total,
        boolean paymentAvailable,
        long balance,
        boolean purchaseAvailable,
        String paymentMessage
) {

    /**
     * <summary>
     * Инициализирует сокращенную версию модели представления корзины с базовым набором данных,
     * устанавливая доступность оплаты по умолчанию и нулевой баланс.
     * </summary>
     * @param items Список моделей представления товаров, добавленных в корзину покупателя.
     * @param total Вычисленная общая стоимость всех позиций в корзине.
     **/
    public CartPageViewModel(List<ItemViewModel> items, long total) {
        this(items, total, true, 0, false, null);
    }
}