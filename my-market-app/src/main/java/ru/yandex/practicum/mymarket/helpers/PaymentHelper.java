package ru.yandex.practicum.mymarket.helpers;

import org.springframework.stereotype.Component;

/**
 * <summary>
 * Вспомогательный компонент для централизованного управления константами
 * и текстовыми уведомлениями платежной подсистемы.
 * </summary>
 **/
@Component
public class PaymentHelper {

    // region Constants

    /**
     * Сообщение при технической недоступности или сбое сети внешнего сервиса платежей.
     **/
    private static final String PAYMENT_SERVICE_UNAVAILABLE = "Сервис платежей недоступен";

    /**
     * Сообщение по умолчанию, если транзакция отклонена без указания конкретной причины.
     **/
    private static final String PAYMENT_REJECTED = "Платёж не выполнен";

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает стандартное сообщение о недоступности платежного шлюза.
     * </summary>
     * <return>
     * @return Строка уведомления об ошибке сети/сервиса.
     * </return>
     **/
    public String resolveServiceUnavailableMessage() {
        return PAYMENT_SERVICE_UNAVAILABLE;
    }

    /**
     * <summary>
     * Возвращает стандартное сообщение об отклонении платежа.
     * </summary>
     * <return>
     * @return Строка уведомления об отказе в транзакции.
     * </return>
     **/
    public String resolveDefaultRejectedMessage() {
        return PAYMENT_REJECTED;
    }

    /**
     * <summary>
     * Проверяет и нормализует входящее текстовое сообщение от шлюза.
     * Если строка пуста или null, подставляет дефолтное сообщение об отклонении.
     * </summary>
     * @param message Исходное сообщение от внешней системы.
     * <return>
     * @return Нормализованный текст ошибки.
     * </return>
     **/
    public String normalizeMessage(final String message) {
        return message == null || message.isBlank()
                ? PAYMENT_REJECTED
                : message;
    }

    // endregion
}