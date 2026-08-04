package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.helpers.PaymentHelper;
import ru.yandex.practicum.mymarket.mappers.PaymentMapper;
import ru.yandex.practicum.mymarket.payment.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payment.client.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentResponse;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;
import tools.jackson.databind.ObjectMapper;

/**
 * <summary>
 * Модульные тесты для проверки клиентского сервиса взаимодействия с платежным шлюзом PaymentClientServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class PaymentClientServiceImplTest {

    // region Fields

    @Mock
    private PaymentsApi paymentsApi;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentHelper paymentHelper;

    @InjectMocks
    private PaymentClientServiceImpl paymentClientService;

    // endregion

    // region Tests for getBalance

    /**
     * <summary>
     * Проверяет успешное получение баланса из удаленного сервиса.
     * </summary>
     **/
    @Test
    void getBalanceShouldReturnViewModelWhenRequestSucceeds() {
        var balanceResponse = new BalanceResponse().balance(150000L);

        var expectedViewModel = PaymentAvailabilityViewModel.available(150000L);

        Mockito.when(paymentsApi.getBalance()).thenReturn(Mono.just(balanceResponse));

        Mockito.when(paymentMapper.toAvailabilityViewModel(balanceResponse)).thenReturn(expectedViewModel);

        StepVerifier.create(paymentClientService.getBalance())
                .expectNext(expectedViewModel)
                .verifyComplete();

        Mockito.verify(paymentsApi, Mockito.times(1)).getBalance();

        Mockito.verify(paymentMapper, Mockito.times(1)).toAvailabilityViewModel(balanceResponse);
    }

    /**
     * <summary>
     * Проверяет обработку HTTP-ошибки (4xx/5xx) при запросе баланса и возврат статуса недоступности.
     * </summary>
     **/
    @Test
    void getBalanceShouldReturnUnavailableOnWebClientResponseException() {
        var responseException = Mockito.mock(WebClientResponseException.class);

        var unavailableMessage = "Платежный сервис временно недоступен";

        var expectedViewModel = PaymentAvailabilityViewModel.unavailable(unavailableMessage);

        Mockito.when(paymentsApi.getBalance()).thenReturn(Mono.error(responseException));

        Mockito.when(paymentHelper.resolveServiceUnavailableMessage()).thenReturn(unavailableMessage);

        StepVerifier.create(paymentClientService.getBalance())
                .expectNext(expectedViewModel)
                .verifyComplete();

        Mockito.verify(paymentHelper, Mockito.times(1)).resolveServiceUnavailableMessage();
    }

    /**
     * <summary>
     * Проверяет обработку сетевого сбоя (таймаут, сбой DNS) при запросе баланса.
     * </summary>
     **/
    @Test
    void getBalanceShouldReturnUnavailableOnWebClientRequestException() {
        var requestException = Mockito.mock(WebClientRequestException.class);

        var unavailableMessage = "Сетевая ошибка сервиса оплаты";

        var expectedViewModel = PaymentAvailabilityViewModel.unavailable(unavailableMessage);

        Mockito.when(paymentsApi.getBalance()).thenReturn(Mono.error(requestException));

        Mockito.when(paymentHelper.resolveServiceUnavailableMessage()).thenReturn(unavailableMessage);

        StepVerifier.create(paymentClientService.getBalance())
                .expectNext(expectedViewModel)
                .verifyComplete();

        Mockito.verify(paymentHelper, Mockito.times(1)).resolveServiceUnavailableMessage();
    }

    // endregion

    // region Tests for pay

    /**
     * <summary>
     * Проверяет выброс исключения BAD_REQUEST при передаче нулевой или отрицательной суммы списания.
     * </summary>
     **/
    @Test
    void payShouldThrowBadRequestWhenAmountIsZeroOrNegative() {
        StepVerifier.create(paymentClientService.pay(0L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException rse &&
                                rse.getStatusCode().equals(HttpStatus.BAD_REQUEST) &&
                                "Сумма платежа должна быть больше нуля".equals(rse.getReason())
                )
                .verify();

        StepVerifier.create(paymentClientService.pay(-500L))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException rse &&
                                rse.getStatusCode().equals(HttpStatus.BAD_REQUEST)
                )
                .verify();

        Mockito.verifyNoInteractions(paymentsApi);
    }

    /**
     * <summary>
     * Проверяет успешную транзакцию списания денежных средств.
     * </summary>
     **/
    @Test
    void payShouldReturnResultWhenPaymentSucceeds() {
        var amount = 10000L;

        var paymentRequest = new PaymentRequest().amount(amount);

        var paymentResponse = new PaymentResponse().success(true).balance(90000L);

        var expectedResult = OrderPaymentResultViewModel.success(90000L);

        Mockito.when(paymentsApi.pay(Mockito.refEq(paymentRequest))).thenReturn(Mono.just(paymentResponse));

        Mockito.when(paymentMapper.toOrderPaymentResultViewModel(paymentResponse)).thenReturn(expectedResult);

        StepVerifier.create(paymentClientService.pay(amount))
                .expectNext(expectedResult)
                .verifyComplete();

        Mockito.verify(paymentsApi, Mockito.times(1)).pay(Mockito.refEq(paymentRequest));
    }

    /**
     * <summary>
     * Проверяет обработку конфликта (HTTP 409) от платежного шлюза при недостаточном балансе и успешный парсинг тела ошибки.
     * </summary>
     **/
    @Test
    void payShouldParseErrorBodyWhenConflictExceptionOccurs() throws Exception {
        var amount = 50000L;

        var responseException = Mockito.mock(WebClientResponseException.class);

        var errorBytes = "{\"success\":false,\"balance\":1000,\"message\":\"Недостаточно средств\"}".getBytes();

        var parsedResponse = new PaymentResponse().success(false).balance(1000L).message("Недостаточно средств");

        var expectedResult = OrderPaymentResultViewModel.rejected(1000L, "Недостаточно средств");

        Mockito.when(paymentsApi.pay(Mockito.any(PaymentRequest.class))).thenReturn(Mono.error(responseException));

        Mockito.when(responseException.getStatusCode()).thenReturn(HttpStatus.CONFLICT);

        Mockito.when(responseException.getResponseBodyAsByteArray()).thenReturn(errorBytes);

        Mockito.when(objectMapper.readValue(errorBytes, PaymentResponse.class)).thenReturn(parsedResponse);

        Mockito.when(paymentMapper.toOrderPaymentResultViewModel(parsedResponse)).thenReturn(expectedResult);

        StepVerifier.create(paymentClientService.pay(amount))
                .expectNext(expectedResult)
                .verifyComplete();

        Mockito.verify(objectMapper, Mockito.times(1)).readValue(errorBytes, PaymentResponse.class);
    }

    /**
     * <summary>
     * Проверяет корректный фоллбэк при конфликте (HTTP 409), если тело ошибки не удалось десериализовать.
     * </summary>
     **/
    @Test
    void payShouldFallbackWhenConflictResponseBodyParsingFails() throws Exception {
        var amount = 50000L;

        var responseException = Mockito.mock(WebClientResponseException.class);

        var invalidBytes = "invalid json".getBytes();

        var expectedResult = OrderPaymentResultViewModel.rejected(0L, "Сбой обработки");

        Mockito.when(paymentsApi.pay(Mockito.any(PaymentRequest.class))).thenReturn(Mono.error(responseException));

        Mockito.when(responseException.getStatusCode()).thenReturn(HttpStatus.CONFLICT);

        Mockito.when(responseException.getResponseBodyAsByteArray()).thenReturn(invalidBytes);

        Mockito.when(objectMapper.readValue(invalidBytes, PaymentResponse.class)).thenThrow(new RuntimeException("JSON error"));

        Mockito.when(paymentMapper.toOrderPaymentResultViewModel(Mockito.argThat(res ->
                !res.getSuccess() && res.getBalance() == 0L && res.getMessage() == null
        ))).thenReturn(expectedResult);

        StepVerifier.create(paymentClientService.pay(amount))
                .expectNext(expectedResult)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет обработку серверных ошибок HTTP (например, 500 Internal Server Error) при оплате.
     * </summary>
     **/
    @Test
    void payShouldReturnUnavailableOnInternalServerError() {
        var responseException = Mockito.mock(WebClientResponseException.class);

        var unavailableMessage = "Сервис недоступен";

        var expectedResult = OrderPaymentResultViewModel.unavailable(unavailableMessage);

        Mockito.when(paymentsApi.pay(Mockito.any(PaymentRequest.class))).thenReturn(Mono.error(responseException));

        Mockito.when(responseException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        Mockito.when(paymentHelper.resolveServiceUnavailableMessage()).thenReturn(unavailableMessage);

        StepVerifier.create(paymentClientService.pay(1000L))
                .expectNext(expectedResult)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет обработку сетевых исключений транспортного уровня при оплате.
     * </summary>
     **/
    @Test
    void payShouldReturnUnavailableOnRequestException() {
        var requestException = Mockito.mock(WebClientRequestException.class);

        var unavailableMessage = "Таймаут подключения";

        var expectedResult = OrderPaymentResultViewModel.unavailable(unavailableMessage);

        Mockito.when(paymentsApi.pay(Mockito.any(PaymentRequest.class))).thenReturn(Mono.error(requestException));

        Mockito.when(paymentHelper.resolveServiceUnavailableMessage()).thenReturn(unavailableMessage);

        StepVerifier.create(paymentClientService.pay(1000L))
                .expectNext(expectedResult)
                .verifyComplete();
    }

    // endregion
}