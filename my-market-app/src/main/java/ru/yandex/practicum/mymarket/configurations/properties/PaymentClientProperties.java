package ru.yandex.practicum.mymarket.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <summary>
 * Конфигурационные свойства для настройки параметров интеграции с удаленным платежным сервисом.
 * </summary>
 * @param baseUrl Базовый URL-адрес удаленного платежного шлюза.
 **/
@ConfigurationProperties(prefix = "app.payment-service")
public record PaymentClientProperties(
        String baseUrl
) {
}