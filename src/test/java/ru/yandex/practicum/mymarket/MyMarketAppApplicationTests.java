package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * <summary>
 * Главный класс тестирования контекста приложения.
 * Служит базовым классом для всех интеграционных тестов проекта,
 * обеспечивая сквозное кэширование Spring Context.
 * </summary>
 **/
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MyMarketAppApplicationTests {

	// region Fields

	@Autowired
	protected WebApplicationContext webApplicationContext;

	protected MockMvc mockMvc;

	// endregion

	// region Setup

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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