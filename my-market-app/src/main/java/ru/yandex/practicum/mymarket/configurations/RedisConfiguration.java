package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <summary>
 * Конфигурация взаимодействия с Redis.
 * </summary>
 **/
@Configuration
public class RedisConfiguration {

    // region Methods

    /**
     * <summary>
     * Создает и настраивает шаблон ReactiveRedisTemplate для работы с JSON-данными.
     * </summary>
     * @param connectionFactory Фабрика соединений с Redis.
     * <return>
     * @return Сконфигурированный шаблон для выполнения операций кэширования.
     * </return>
     **/
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveJsonRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        var keySerializer = new StringRedisSerializer();

        var valueSerializer = RedisSerializer.json();

        var serializationContext = RedisSerializationContext
                .<String, Object>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    // endregion
}