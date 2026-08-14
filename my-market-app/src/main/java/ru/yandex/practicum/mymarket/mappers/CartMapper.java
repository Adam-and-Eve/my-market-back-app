package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;

import java.util.List;

/**
 * <summary>
 * Компонент-маппер для преобразования списков товаров и данных платежной системы в модели представления страницы корзины.
 * </summary>
 **/
@Component
public class CartMapper {

    // region Methods

    /**
     * <summary>
     * Преобразует список элементов корзины в базовую модель представления с автоматическим расчетом общей стоимости.
     * </summary>
     * @param items Список позиций товаров, находящихся в корзине.
     * <return>
     * @return Модель представления CartPageViewModel с посчитанной суммой.
     * </return>
     **/
    public CartPageViewModel toViewModel(final List<ItemViewModel> items) {
        var total = items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        return new CartPageViewModel(items, total);
    }

    /**
     * <summary>
     * Собрает модель представления страницы корзины на основе списка товаров, рассчитанной стоимости и статуса платежного платежного сервиса.
     * </summary>
     * @param items Список позиций товаров, находящихся в корзине.
     * @param total Рассчитанная итоговая стоимость всех позиций.
     * @param payment Информация о доступности платежной системы и балансе пользователя.
     * <return>
     * @return Расширенная модель представления CartPageViewModel с данными о доступности покупки и балансе.
     * </return>
     **/
    public CartPageViewModel toViewModel(
            final List<ItemViewModel> items,
            final long total,
            final PaymentAvailabilityViewModel payment) {
        if (!payment.available()) {
            return new CartPageViewModel(items, total, false, payment.balance(), false, payment.message());
        }

        var purchaseAvailable = payment.balance() >= total;

        var message = purchaseAvailable ? null : "Недостаточно средств для оформления заказа";

        return new CartPageViewModel(items, total, true, payment.balance(), purchaseAvailable, message);
    }

    /**
     * <summary>
     * Преобразует список элементов и информацию о балансе пользователя в полную модель представления страницы корзины с расчетом итоговой суммы.
     * </summary>
     * @param items Список позиций товаров, находящихся в корзине.
     * @param payment Информация о доступности платежной системы и балансе пользователя.
     * <return>
     * @return Расширенная модель представления CartPageViewModel с рассчитанной суммой и статусом возможности оплаты.
     * </return>
     **/
    public CartPageViewModel toViewModel(
            final List<ItemViewModel> items,
            final PaymentAvailabilityViewModel payment) {

        var total = items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        if (!payment.available()) {
            return new CartPageViewModel(items, total, false, payment.balance(), false, payment.message());
        }

        var purchaseAvailable = payment.balance() >= total;

        var message = purchaseAvailable ? null : "Недостаточно средств для оформления заказа";

        return new CartPageViewModel(items, total, true, payment.balance(), purchaseAvailable, message);
    }

    // endregion
}