package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.helpers.PaymentHelper;
import ru.yandex.practicum.mymarket.interfaces.PaymentClientService;
import ru.yandex.practicum.mymarket.mappers.PaymentMapper;
import ru.yandex.practicum.mymarket.payment.client.api.PaymentsApi;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentRequest;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentResponse;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;
import tools.jackson.databind.ObjectMapper;

/**
 * <summary>
 * Сервис для взаимодействия с удаленным платежным шлюзом.
 * </summary>
 **/
@Service
public class PaymentClientServiceImpl implements PaymentClientService {

    // region Fields

    /**
     * Сгенерированный реактивный API-клиент для отправки HTTP-запросов к платежному сервису.
     **/
    private final PaymentsApi paymentsApi;

    /**
     * Компонент для десериализации тела HTTP-ошибок в типизированные объекты ответов шлюза.
     **/
    private final ObjectMapper objectMapper;

    /**
     * Компонент-маппер для трансформации сетевых ответов шлюза в модели представления.
     **/
    private final PaymentMapper paymentMapper;

    /**
     * Вспомогательный компонент для разрешения системных сообщений об ошибках.
     **/
    private final PaymentHelper paymentHelper;

    // endregion

    // region Constructors

    public PaymentClientServiceImpl(
            final PaymentsApi paymentsApi,
            final ObjectMapper objectMapper,
            final PaymentMapper paymentMapper,
            final PaymentHelper paymentHelper) {

        this.paymentsApi = paymentsApi;
        this.objectMapper = objectMapper;
        this.paymentMapper = paymentMapper;
        this.paymentHelper = paymentHelper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Запрашивает текущий доступный баланс пользователя в платежной системе.
     * </summary>
     * <return>
     * @return Реактивный контейнер Mono с информацией о доступности сервиса и балансе.
     * </return>
     **/
    public Mono<PaymentAvailabilityViewModel> getBalance() {
        return paymentsApi.getBalance()
                .map(paymentMapper::toAvailabilityViewModel)
                .onErrorResume(WebClientResponseException.class, this::handleBalanceResponseError)
                .onErrorResume(WebClientRequestException.class, this::handleBalanceRequestError);
    }

    /**
     * <summary>
     * Инициирует операцию списания денежных средств в счет оплаты заказа.
     * </summary>
     * @param amount Сумма списания.
     * <return>
     * @return Реактивный контейнер Mono с детализированным результатом проведения платежа.
     * </return>
     **/
    public Mono<OrderPaymentResultViewModel> pay(final long amount) {
        PaymentRequest request = new PaymentRequest().amount(amount);
        return paymentsApi.pay(request)
                .map(paymentMapper::toOrderPaymentResultViewModel)
                .onErrorResume(WebClientResponseException.class, this::handlePaymentError)
                .onErrorResume(WebClientRequestException.class, this::handlePaymentRequestError);
    }

    /**
     * <summary>
     * Обрабатывает исключения транспортного уровня (сетевые тайм-ауты, сбои DNS) при запросе баланса.
     * </summary>
     * @param error Исключение запроса WebClient.
     * <return>
     * @return Реактивный контейнер Mono с моделью недоступности сервиса.
     * </return>
     **/
    private Mono<PaymentAvailabilityViewModel> handleBalanceRequestError(final WebClientRequestException error) {
        return Mono.just(PaymentAvailabilityViewModel.unavailable(paymentHelper.resolveServiceUnavailableMessage()));
    }

    /**
     * <summary>
     * Обрабатывает некорректные HTTP-ответы (ошибки 4xx/5xx) от удаленного сервиса при запросе баланса.
     * </summary>
     * @param error Исключение ответа WebClient.
     * <return>
     * @return Реактивный контейнер Mono с моделью недоступности сервиса.
     * </return>
     **/
    private Mono<PaymentAvailabilityViewModel> handleBalanceResponseError(final WebClientResponseException error) {
        return Mono.just(PaymentAvailabilityViewModel.unavailable(paymentHelper.resolveServiceUnavailableMessage()));
    }

    /**
     * <summary>
     * Обрабатывает сетевые сбои отправки запроса непосредственно в процессе проведения транзакции оплаты.
     * </summary>
     * @param error Исключение запроса WebClient.
     * <return>
     * @return Реактивный контейнер Mono с результатом недоступности сервиса оплаты.
     * </return>
     **/
    private Mono<OrderPaymentResultViewModel> handlePaymentRequestError(final WebClientRequestException error) {
        return Mono.just(OrderPaymentResultViewModel.unavailable(paymentHelper.resolveServiceUnavailableMessage()));
    }

    /**
     * <summary>
     * Выполняет реактивный перехват ошибочных HTTP-статусов при оплате.
     * При конфликтах (HTTP 409) извлекает бизнес-логику ошибки, при прочих статусах возвращает недоступность шлюза.
     * </summary>
     * @param error Исключение ответа WebClient.
     * <return>
     * @return Реактивный контейнер Mono с моделью результата проведения платежа.
     * </return>
     **/
    private Mono<OrderPaymentResultViewModel> handlePaymentError(final WebClientResponseException error) {
        if (error.getStatusCode() == HttpStatus.CONFLICT) {
            return Mono.just(paymentMapper.toOrderPaymentResultViewModel(parsePaymentResponse(error)));
        }
        return Mono.just(OrderPaymentResultViewModel.unavailable(paymentHelper.resolveServiceUnavailableMessage()));
    }

    /**
     * <summary>
     * Безопасно парсит массив байт из тела HTTP-ошибки обратно в объект ответа PaymentResponse.
     * </summary>
     * @param error Исключение ответа WebClient с телом ошибки.
     * <return>
     * @return Десериализованный объект PaymentResponse или дефолтный объект неуспешной операции при сбое парсинга.
     * </return>
     **/
    private PaymentResponse parsePaymentResponse(final WebClientResponseException error) {
        try {
            return objectMapper.readValue(error.getResponseBodyAsByteArray(), PaymentResponse.class);
        } catch (Exception ignored) {
            return new PaymentResponse()
                    .success(false)
                    .balance(0L)
                    .message(null);
        }
    }

    // endregion
}