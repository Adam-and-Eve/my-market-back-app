package ru.yandex.practicum.payment.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <summary>
 * Конфигурационные свойства платежного сервиса.
 * Используется для автоматического маппинга настроек из файлов конфигурации (.properties / .yaml) с префиксом payment.
 * </summary>
 * @param initialBalance Начальное значение баланса на счете, устанавливаемое при старте приложения.
 **/
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        long initialBalance
) {
}