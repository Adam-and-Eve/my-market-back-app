package ru.yandex.practicum.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * <summary>
 * Главный класс тестирования контекста приложения.
 * Служит базовым классом для всех интеграционных тестов проекта,
 * обеспечивая сквозное кэширование Spring Context.
 * </summary>
 **/
@SpringBootTest
@ActiveProfiles("test")
public class PaymentServiceApplicationTests {

    // region Fields

    @Autowired
    protected ApplicationContext applicationContext;

    protected WebTestClient webTestClient;

    // endregion

    // region Setup

    @BeforeEach
    void clearDatabase() {
        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .build();
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Базовый тест проверки корректности сборки и поднятия контекста Spring Boot.
     * </summary>
     * */
    @Test
    void contextLoads() {
    }

    // endregion
}