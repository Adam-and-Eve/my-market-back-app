package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.viewmodels.CheckoutResultViewModel;

public interface PurchaseService {

    // region Methods

    /**
     * <summary>
     * Выполняет полный транзакционный процесс покупки товаров из корзины пользователя: фиксацию заказа,
     * взаимодействия с платежным сервисом и очистку корзины при успешном проведении операции.
     * </summary>
     * @param username Имя пользователя, оформляющего покупку.
     * <return>
     * @return Реактивный контейнер Mono с результатом проведения операции покупки CheckoutResultViewModel.
     * </return>
     **/
    public Mono<CheckoutResultViewModel> buy(final String username);

    // endregion
}