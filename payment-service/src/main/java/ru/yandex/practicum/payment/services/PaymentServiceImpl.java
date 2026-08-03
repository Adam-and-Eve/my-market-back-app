package ru.yandex.practicum.payment.services;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.configurations.properties.PaymentProperties;
import ru.yandex.practicum.payment.interfaces.PaymentService;
import ru.yandex.practicum.payment.viewmodels.PaymentResultViewModel;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <summary>
 * Реализация сервиса управления платежами.
 * </summary>
 **/
@Service
public class PaymentServiceImpl implements PaymentService {

    // region Fields

    private final AtomicLong balance;

    // endregion

    // region Constructors

    public PaymentServiceImpl(final PaymentProperties paymentProperties) {
        this.balance = new AtomicLong(paymentProperties.initialBalance());
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает текущий остаток денежных средств на счете.
     * </summary>
     * <return>
     * @return Моно-контейнер с текущим значением баланса.
     * </return>
     **/
    public Mono<Long> getBalance() {
        return Mono.fromSupplier(balance::get);
    }

    /**
     * <summary>
     * Производит атомарное списание средств со счета пользователя.
     * </summary>
     * @param amount Сумма, подлежащая списанию.
     * <return>
     * @return Моно-контейнер с результатом транзакции, содержащим статус операции, актуальный баланс и описание ошибки при наличии.
     * </return>
     **/
    public Mono<PaymentResultViewModel> pay(long amount) {
        return Mono.fromSupplier(() -> {
            while (true) {
                long currentBalance = balance.get();

                if (currentBalance < amount) {
                    return PaymentResultViewModel.failed(currentBalance, "Недостаточно средств.");
                }

                long updatedBalance = currentBalance - amount;

                if (balance.compareAndSet(currentBalance, updatedBalance)) {
                    return PaymentResultViewModel.success(updatedBalance);
                }
            }
        });
    }

    // endregion
}