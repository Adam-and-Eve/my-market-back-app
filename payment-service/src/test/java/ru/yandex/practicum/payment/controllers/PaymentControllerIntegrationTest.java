package ru.yandex.practicum.payment.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.generated.model.PaymentRequest;
import ru.yandex.practicum.payment.PaymentServiceApplicationTests;
import ru.yandex.practicum.payment.interfaces.PaymentService;
import ru.yandex.practicum.payment.viewmodels.PaymentResultViewModel;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности обработки HTTP-запросов в PaymentController.
 * </summary>
 **/
public class PaymentControllerIntegrationTest extends PaymentServiceApplicationTests {

    // region Fields

    @MockitoBean
    private PaymentService paymentService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет успешное получение баланса счета (HTTP 200 OK) с корректной JSON-структурой ответа.
     * </summary>
     **/
    @Test
    public void getBalanceShouldReturn200OKAndCurrentBalance() {
        var currentBalance = 1500L;

        Mockito.when(paymentService.getBalance()).thenReturn(Mono.just(currentBalance));

        webTestClient.get().uri("/payments/balance")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.balance").isEqualTo(currentBalance);

        Mockito.verify(paymentService, Mockito.times(1)).getBalance();
    }

    /**
     * <summary>
     * Проверяет успешную проведение операции оплаты (HTTP 200 OK) при достаточном количестве средств.
     * </summary>
     **/
    @Test
    public void payShouldReturn200OKWhenPaymentIsSuccessful() {
        var payAmount = 500L;

        var remainingBalance = 1000L;

        var successResult = PaymentResultViewModel.success(remainingBalance);

        Mockito.when(paymentService.pay(payAmount)).thenReturn(Mono.just(successResult));

        var requestBody = new PaymentRequest(payAmount);

        webTestClient.post().uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.balance").isEqualTo(remainingBalance)
                .jsonPath("$.message").isEqualTo(null);

        Mockito.verify(paymentService, Mockito.times(1)).pay(payAmount);
    }

    /**
     * <summary>
     * Проверяет возврат статуса HTTP 409 Conflict и сообщения об ошибке при дефиците средств на счете.
     * </summary>
     **/
    @Test
    public void payShouldReturn409ConflictWhenInsufficientFunds() {
        var payAmount = 2000L;
        var currentBalance = 300L;
        var errorMessage = "Недостаточно средств.";

        var failedResult = PaymentResultViewModel.failed(currentBalance, errorMessage);

        Mockito.when(paymentService.pay(payAmount)).thenReturn(Mono.just(failedResult));

        var requestBody = new PaymentRequest(payAmount);

        webTestClient.post().uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.balance").isEqualTo(currentBalance)
                .jsonPath("$.message").isEqualTo(errorMessage);

        Mockito.verify(paymentService, Mockito.times(1)).pay(payAmount);
    }

    // endregion
}