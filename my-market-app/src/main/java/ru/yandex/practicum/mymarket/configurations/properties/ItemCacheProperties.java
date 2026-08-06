package ru.yandex.practicum.mymarket.configurations.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * <summary>
 * Конфигурационные свойства кэширования товаров приложения.
 * </summary>
 * @param ttl Время жизни (Time to Live) для кэшированных записей товаров в хранилище.
 **/
@ConfigurationProperties(prefix = "app.items-cache")
public record ItemCacheProperties(
        Duration ttl
) {
}