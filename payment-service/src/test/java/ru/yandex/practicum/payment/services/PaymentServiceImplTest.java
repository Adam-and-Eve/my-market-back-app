package ru.yandex.practicum.payment.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import ru.yandex.practicum.payment.configurations.properties.PaymentProperties;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики PaymentServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    // region Fields

    @Mock
    private PaymentProperties paymentProperties;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет получение начального баланса счета после инициализации сервиса.
     * </summary>
     **/
    @Test
    void getBalanceShouldReturnInitialBalance() {
        var initialBalance = 1000L;

        Mockito.when(paymentProperties.initialBalance()).thenReturn(initialBalance);

        var paymentService = new PaymentServiceImpl(paymentProperties);

        StepVerifier.create(paymentService.getBalance())
                .expectNext(initialBalance)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет успешную оплату при достаточном количестве средств на счете и обновление текущего баланса.
     * </summary>
     **/
    @Test
    void payShouldDeductBalanceAndReturnSuccessWhenSufficientFunds() {
        var initialBalance = 1000L;

        var amountToPay = 400L;

        var expectedBalance = 600L;

        Mockito.when(paymentProperties.initialBalance()).thenReturn(initialBalance);

        var paymentService = new PaymentServiceImpl(paymentProperties);

        StepVerifier.create(paymentService.pay(amountToPay))
                .expectNextMatches(result ->
                        result.success() &&
                                result.balance() == expectedBalance &&
                                result.message() == null
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance())
                .expectNext(expectedBalance)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет списывание всей суммы до нулевого остатка, если сумма покупки равна текущему балансу.
     * </summary>
     **/
    @Test
    void payShouldReduceBalanceToZeroWhenAmountEqualsBalance() {
        var initialBalance = 500L;

        var amountToPay = 500L;

        Mockito.when(paymentProperties.initialBalance()).thenReturn(initialBalance);

        var paymentService = new PaymentServiceImpl(paymentProperties);

        StepVerifier.create(paymentService.pay(amountToPay))
                .expectNextMatches(result ->
                        result.success() &&
                                result.balance() == 0L
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance())
                .expectNext(0L)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет отклонение платежа при недостаточном количестве средств и неизменность баланса.
     * </summary>
     **/
    @Test
    void payShouldReturnFailedWhenInsufficientFunds() {
        var initialBalance = 300L;

        var amountToPay = 500L;

        Mockito.when(paymentProperties.initialBalance()).thenReturn(initialBalance);

        var paymentService = new PaymentServiceImpl(paymentProperties);

        StepVerifier.create(paymentService.pay(amountToPay))
                .expectNextMatches(result ->
                        !result.success() &&
                                result.balance() == initialBalance &&
                                "Недостаточно средств.".equals(result.message())
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance())
                .expectNext(initialBalance)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет корректность проведения нескольких последовательных операций списания и отказа.
     * </summary>
     **/
    @Test
    void payShouldProcessMultipleConsecutiveTransactionsCorrectly() {
        var initialBalance = 1000L;

        Mockito.when(paymentProperties.initialBalance()).thenReturn(initialBalance);

        var paymentService = new PaymentServiceImpl(paymentProperties);

        StepVerifier.create(paymentService.pay(300L))
                .expectNextMatches(result -> result.success() && result.balance() == 700L)
                .verifyComplete();

        StepVerifier.create(paymentService.pay(500L))
                .expectNextMatches(result -> result.success() && result.balance() == 200L)
                .verifyComplete();

        StepVerifier.create(paymentService.pay(300L))
                .expectNextMatches(result ->
                        !result.success() &&
                                result.balance() == 200L &&
                                "Недостаточно средств.".equals(result.message())
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance())
                .expectNext(200L)
                .verifyComplete();
    }

    // endregion
}