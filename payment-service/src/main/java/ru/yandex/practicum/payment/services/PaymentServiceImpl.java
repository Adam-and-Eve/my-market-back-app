package ru.yandex.practicum.payment.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.configurations.properties.PaymentProperties;
import ru.yandex.practicum.payment.interfaces.PaymentService;
import ru.yandex.practicum.payment.viewmodels.PaymentResultViewModel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <summary>
 * Реализация сервиса управления платежами.
 * </summary>
 **/
@Service
public class PaymentServiceImpl implements PaymentService {

    // region Constants

    /**
     * Сообщение об ошибке при недостаточном количестве средств на счете пользователя.
     **/
    private static final String NOT_ENOUGH_MONEY_MESSAGE = "Недостаточно средств";

    // endregion

    // region Fields

    /**
     * Начальный баланс, присваиваемый новым пользователям при первом обращении.
     **/
    private final long initialBalance;

    /**
     * Потокбезопасное хранилище балансов пользователей (имя пользователя -> атомарный счетчик баланса).
     **/
    private final ConcurrentMap<String, AtomicLong> balances = new ConcurrentHashMap<>();

    // endregion

    // region Constructors

    public PaymentServiceImpl(PaymentProperties properties) {
        this.initialBalance = properties.initialBalance();
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
    public Mono<Long> getBalance(final String username) {
        return Mono.fromSupplier(() -> balanceFor(username).get());
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
    public Mono<PaymentResultViewModel> pay(final String username, final long amount) {
        if (username == null ||
            username.isBlank()) {

            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Имя покупателя не должно отсутствовать"
            ));
        }

        if (amount <= 0) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Сумма платежа должна быть больше нуля"
            ));
        }

        return Mono.fromSupplier(() -> {
            var balance = balanceFor(username);

            while (true) {
                var currentBalance = balance.get();

                if (currentBalance < amount) {
                    return PaymentResultViewModel.failed(currentBalance, NOT_ENOUGH_MONEY_MESSAGE);
                }

                var updatedBalance = currentBalance - amount;

                if (balance.compareAndSet(currentBalance, updatedBalance)) {
                    return PaymentResultViewModel.success(updatedBalance);
                }
            }
        });
    }

    /**
     * <summary>
     * Возвращает атомарный счетчик баланса для указанного пользователя.
     * Если пользователь обращается впервые, инициализирует его счет стартовым балансом.
     * </summary>
     * @param username Имя пользователя.
     * <return>
     * @return Атомарный контейнер AtomicLong с текущим балансом пользователя.
     * </return>
     **/
    private AtomicLong balanceFor(String username) {
        return balances.computeIfAbsent(username, ignored -> new AtomicLong(initialBalance));
    }

    // endregion
}