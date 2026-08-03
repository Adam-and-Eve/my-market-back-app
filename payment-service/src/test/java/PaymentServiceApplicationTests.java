import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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