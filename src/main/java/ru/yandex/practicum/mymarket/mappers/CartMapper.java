package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

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

    // endregion
}