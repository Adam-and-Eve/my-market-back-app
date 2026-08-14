package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.UserModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в UserRepository.
 * </summary>
 **/
public class UserRepositoryIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @Autowired
    private UserRepository userRepository;

    // endregion

    /**
     * <summary>
     * Очищает состояние таблицы заказов в базе данных перед каждым тестом для обеспечения их независимости.
     * </summary>
     **/
    @BeforeEach
    void setUp() {
        var systemIds = List.of(1L, 2L);

        userRepository.findAll()
                .filter(u -> !systemIds.contains(u.getId()))
                .flatMap(u -> userRepository.delete(u))
                .blockLast();
    }

    // region Tests

    /**
     * <summary>
     * Проверяет успешный поиск покупателя по имени пользователя, когда учетная запись создается во время теста.
     * </summary>
     **/
    @Test
    void findByUsernameShouldReturnUserWhenUserExists() {
        var username = "new_user";

        var user = new UserModel(username, true);

        var savedUser = userRepository.save(user).block();

        Assertions.assertNotNull(savedUser);

        var result = userRepository.findByUsername(username).blockOptional();

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(savedUser.getId(), result.get().getId());

        Assertions.assertEquals(username, result.get().getUsername());

        Assertions.assertTrue(result.get().getEnabled());

        Assertions.assertNotNull(result.get().getCreatedAt());
    }

    /**
     * Проверяет успешный поиск пользователя по имени.
     */
    @Test
    void findByUsernameShouldReturnSavedUser() {
        var user = userRepository.save(new UserModel("test-user", true)).block();

        Assertions.assertNotNull(user);

        var result = userRepository.findByUsername("test-user").blockOptional();

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(user.getId(), result.get().getId());

        Assertions.assertEquals("test-user", result.get().getUsername());

        Assertions.assertTrue(result.get().getEnabled());
    }

    /**
     * Проверяет, что поиск отсутствующего пользователя возвращает пустой результат.
     */
    @Test
    void findByUsernameShouldReturnEmptyWhenUserDoesNotExist() {
        var result = userRepository.findByUsername("unknown-user").blockOptional();

        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * <summary>
     * Проверяет, что поиск по несуществующему имени пользователя возвращает пустой контейнер Mono.
     * </summary>
     **/
    @Test
    void findByUsernameShouldReturnEmptyOptionalWhenUserDoesNotExist() {
        var nonExistentUsername = "unknown_user_999";

        var result = userRepository.findByUsername(nonExistentUsername).blockOptional();

        Assertions.assertTrue(result.isEmpty());
    }

    // endregion
}