package ru.yandex.practicum.mymarket.viewmodels;

import java.util.List;

/**
 * <summary>
 * Модель представления для отображения детальной информации об оформленном заказе.
 * Используется на финальном экране успешной покупки и в истории заказов для передачи скомпонованных данных в UI.
 * </summary>
 * @param id Уникальный идентификатор сохраненного заказа.
 * @param items Список позиций (товаров), входящих в состав данного заказа.
 * @param totalSum Агрегированная итоговая стоимость всего заказа с учетом количества и цен на момент покупки.
 **/
public record OrderViewModel (
        long id,
        List<ItemViewModel> items,
        long totalSum
) {
}