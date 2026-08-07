package ru.yandex.practicum.payment.interfaces;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.viewmodels.PaymentResultViewModel;

/**
 * <summary>
 * Контракт сервиса управления балансом и проведения платежных операций пользователя.
 * </summary>
 **/
public interface PaymentService {

    // region Methods

    /**
     * <summary>
     * Возвращает текущий остаток денежных средств на счете пользователя.
     * </summary>
     * @param username Имя пользователя для получения баланса.
     * <return>
     * @return Реактивный контейнер Mono с текущим значением баланса.
     * </return>
     **/
    public Mono<Long> getBalance(final String username);

    /**
     * <summary>
     * Производит атомарное списание средств со счета пользователя.
     * </summary>
     * @param username Имя пользователя, со счета которого списываются средства.
     * @param amount Сумма, подлежащая списанию.
     * <return>
     * @return Реактивный контейнер Mono с результатом транзакции, содержащим статус операции, актуальный баланс и описание ошибки при наличии.
     * </return>
     **/
    public Mono<PaymentResultViewModel> pay(final String username, long amount);

    // endregion
}