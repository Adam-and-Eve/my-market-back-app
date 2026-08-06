package ru.yandex.practicum.mymarket.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <summary>
 * Конфигурационные свойства интеграции с Keycloak.
 * </summary>
 * @param logoutUri URL эндпоинта завершения сессии (logout) в Keycloak.
 **/
@ConfigurationProperties(prefix = "app.security.keycloak")
public record KeycloakProperties(
        String logoutUri
) {
}