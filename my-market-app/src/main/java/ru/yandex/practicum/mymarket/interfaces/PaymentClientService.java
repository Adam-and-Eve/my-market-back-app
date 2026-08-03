package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;

/**
 * <summary>
 * Контракт сервиса для взаимодействия с удаленным платежным шлюзом.
 * </summary>
 **/
public interface PaymentClientService {

    // region Methods

    /**
     * <summary>
     * Запрашивает текущий доступный баланс пользователя в платежной системе.
     * </summary>
     * <return>
     * @return Реактивный контейнер Mono с информацией о доступности сервиса и балансе.
     * </return>
     **/
    public Mono<PaymentAvailabilityViewModel> getBalance();

    /**
     * <summary>
     * Инициирует операцию списания денежных средств в счет оплаты заказа.
     * </summary>
     * @param amount Сумма списания.
     * <return>
     * @return Реактивный контейнер Mono с детализированным результатом проведения платежа.
     * </return>
     **/
    public Mono<OrderPaymentResultViewModel> pay(final long amount);

    // endregion
}