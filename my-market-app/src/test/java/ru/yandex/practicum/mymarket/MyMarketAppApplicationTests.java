package ru.yandex.practicum.mymarket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
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
public class MyMarketAppApplicationTests {

	// region Fields

	@Autowired
	protected ApplicationContext applicationContext;

	protected WebTestClient webTestClient;

	@Autowired
	private DatabaseClient databaseClient;

	// endregion

	// region Setup

	@BeforeEach
	void clearDatabase() {
		this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
				.apply(SecurityMockServerConfigurers.springSecurity())
				.build();

		databaseClient.sql("DELETE FROM cart_items").then()
				.then(databaseClient.sql("DELETE FROM items").then())
				.then(databaseClient.sql("DELETE FROM order_items").then())
				.then(databaseClient.sql("DELETE FROM orders").then())
				.block();
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