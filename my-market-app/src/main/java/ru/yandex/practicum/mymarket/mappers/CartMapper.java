package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;

import java.util.List;

@Component
public class CartMapper {

    // region Methods

    public CartPageViewModel toViewModel(final List<ItemViewModel> items) {
        var total = items.stream()
                .mapToLong(item -> item.price() * item.count())
                .sum();

        return new CartPageViewModel(items, total);
    }

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