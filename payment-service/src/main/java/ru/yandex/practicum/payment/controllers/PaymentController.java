package ru.yandex.practicum.payment.controllers;

import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.generated.api.PaymentsApi;
import ru.yandex.practicum.payment.generated.model.BalanceResponse;
import ru.yandex.practicum.payment.generated.model.PaymentRequest;
import ru.yandex.practicum.payment.generated.model.PaymentResponse;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.payment.helpers.SecurityHelper;
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
    private final SecurityHelper securityHelper;

    // endregion

    // region Constructors

    public PaymentController(
            final PaymentService paymentService,
            final SecurityHelper securityHelper) {

        this.paymentService = paymentService;
        this.securityHelper = securityHelper;
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
    public Mono<ResponseEntity<BalanceResponse>> getBalance(final ServerWebExchange exchange) {
        return securityHelper.currentUsername()
                .flatMap(paymentService::getBalance)
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
            final Mono<PaymentRequest> paymentRequest,
            final ServerWebExchange exchange
    ) {
        return securityHelper.currentUsername()
                .flatMap(username -> paymentRequest.flatMap(req ->
                        paymentService.pay(username, req.getAmount())
                ))
                .map(result -> {
                    PaymentResponse body = new PaymentResponse(result.success(), result.balance())
                            .message(result.message());

                    var status = result.success() ? HttpStatus.OK : HttpStatus.CONFLICT;

                    return ResponseEntity.status(status).body(body);
                });
    }

    // endregion
}