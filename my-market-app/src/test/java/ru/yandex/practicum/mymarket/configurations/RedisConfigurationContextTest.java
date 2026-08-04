package ru.yandex.practicum.mymarket.configurations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.services.ItemCacheServiceImpl;

/**
 * <summary>
 * Интеграционный тест для проверки корректности конфигурации и внедрения зависимостей Redis.
 * </summary>
 **/
@SpringBootTest
public class RedisConfigurationContextTest extends MyMarketAppApplicationTests {

    // region Fields

    @MockitoBean
    private LettuceConnectionFactory lettuceConnectionFactory;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    private ItemCacheServiceImpl itemCacheService;

    // endregion

    // region Tests

    @Test
    void contextLoadsAndInjectsStringRedisTemplate() {
        Assertions.assertNotNull(
                reactiveRedisTemplate,
                "ReactiveRedisTemplate<String, String> должен успешно создаваться в контексте"
        );

        Assertions.assertNotNull(
                itemCacheService,
                "ItemCacheServiceImpl должен успешно внедряться со строковым шаблоном Redis"
        );
    }

    // endregion
}