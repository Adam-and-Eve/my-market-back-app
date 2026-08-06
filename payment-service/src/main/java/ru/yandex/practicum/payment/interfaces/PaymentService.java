package ru.yandex.practicum.payment.interfaces;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.viewmodels.PaymentResultViewModel;

public interface PaymentService {

    // region Methods

    /**
     * <summary>
     * Возвращает текущий остаток денежных средств на счете.
     * </summary>
     * <return>
     * @return Моно-контейнер с текущим значением баланса.
     * </return>
     **/
    public Mono<Long> getBalance();

    /**
     * <summary>
     * Производит атомарное списание средств со счета пользователя.
     * </summary>
     * @param amount Сумма, подлежащая списанию.
     * <return>
     * @return Моно-контейнер с результатом транзакции, содержащим статус операции, актуальный баланс и описание ошибки при наличии.
     * </return>
     **/
    public Mono<PaymentResultViewModel> pay(long amount);

    // endregion
}