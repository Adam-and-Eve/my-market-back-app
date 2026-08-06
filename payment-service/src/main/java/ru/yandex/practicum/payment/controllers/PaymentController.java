package ru.yandex.practicum.payment.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.generated.api.PaymentsApi;
import ru.yandex.practicum.payment.generated.model.BalanceResponse;
import ru.yandex.practicum.payment.generated.model.PaymentRequest;
import ru.yandex.practicum.payment.generated.model.PaymentResponse;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.payment.interfaces.PaymentService;

/**
 * <summary>
 * REST-контроллер для обработки платежных операций.
 * </summary>
 **/
@RestController
public class PaymentController implements PaymentsApi {

    // region Fields

    private final PaymentService paymentService;

    // endregion

    // region Constructors

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // endregion

    // region Actions

    /**
     * <summary>
     * Обрабатывает входящий GET-запрос на получение текущего баланса счета.
     * </summary>
     * @param exchange Контекст текущего серверного веб-обмена ServerWebExchange.
     * <return>
     * @return Моно-контейнер с ответом ResponseEntity, содержащим объект BalanceResponse со статусом 200 OK.
     * </return>
     **/
    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(balance -> ResponseEntity.ok(new BalanceResponse(balance)));
    }

    /**
     * <summary>
     * Обрабатывает входящий POST-запрос на проведение списания средств за покупку.
     * </summary>
     * @param paymentRequest Моно-контейнер с данными запроса, содержащими сумму платежа.
     * @param exchange Контекст текущего серверного веб-обмена ServerWebExchange.
     * <return>
     * @return Моно-контейнер с результатом транзакции: 200 OK при успехе или 409 Conflict при дефиците средств.
     * </return>
     **/
    @Override
    public Mono<ResponseEntity<PaymentResponse>> pay(
            Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange
    ) {
        return paymentRequest
                .flatMap(request -> paymentService.pay(request.getAmount()))
                .map(result -> {
                    PaymentResponse response = new PaymentResponse(result.success(), result.balance())
                            .message(result.message());
                    HttpStatus status = result.success() ? HttpStatus.OK : HttpStatus.CONFLICT;
                    return ResponseEntity.status(status).body(response);
                });
    }

    // endregion
}