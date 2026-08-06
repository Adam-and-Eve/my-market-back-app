package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    @Primary
    public ReactiveRedisTemplate<String, String> reactiveJsonRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        var stringSerializer = new StringRedisSerializer();

        var serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(stringSerializer)
                .value(stringSerializer)
                .hashKey(stringSerializer)
                .hashValue(stringSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    // endregion
}