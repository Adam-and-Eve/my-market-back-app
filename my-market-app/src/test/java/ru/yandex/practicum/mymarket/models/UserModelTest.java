package ru.yandex.practicum.mymarket.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * <summary>
 * Юнит-тесты для проверки работы свойств, инициализации состояния и бизнес-методов сущности UserModel.
 * </summary>
 **/
public class UserModelTest {

    // region Tests

    /**
     * <summary>
     * Проверяет успешное создание учетной записи покупателя через конструктор с одним именем.
     * Пользователь должен создаваться с неактивным статусом по умолчанию и null-идентификатором.
     * </summary>
     **/
    @Test
    public void constructorWithUsernameShouldCreateNewInstanceWithDefaultStatus() {
        var username = "david";

        var user = new UserModel(username);

        Assertions.assertNotNull(user);

        Assertions.assertNull(user.getId());

        Assertions.assertEquals(username, user.getUsername());

        Assertions.assertFalse(user.getEnabled());

        Assertions.assertNotNull(user.getCreatedAt());
    }

    /**
     * <summary>
     * Проверяет успешное создание учетной записи покупателя через конструктор с указанием флага активности.
     * </summary>
     **/
    @Test
    public void constructorWithUsernameAndEnabledShouldCreateNewInstanceWithValidArguments() {
        var username = "david";

        var enabled = true;

        var user = new UserModel(username, enabled);

        Assertions.assertNotNull(user);

        Assertions.assertNull(user.getId());

        Assertions.assertEquals(username, user.getUsername());

        Assertions.assertTrue(user.getEnabled());

        Assertions.assertNotNull(user.getCreatedAt());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче null, пустой строки или строки из пробелов вместо имени покупателя в конструктор.
     * </summary>
     **/
    @Test
    public void constructorWithNullOrBlankUsernameShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new UserModel(null);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new UserModel("");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new UserModel("   ");
        });
    }

    /**
     * <summary>
     * Проверяет успешное создание сущности через полный конструктор (PersistenceCreator), используемый при маппинге из БД.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithValidArgumentsShouldCreateInstance() {
        var id = 1L;

        var username = "david";

        var enabled = true;

        var createdAt = LocalDateTime.now().minusDays(1);

        var user = new UserModel(id, username, enabled, createdAt);

        Assertions.assertNotNull(user);

        Assertions.assertEquals(id, user.getId());

        Assertions.assertEquals(username, user.getUsername());

        Assertions.assertTrue(user.getEnabled());

        Assertions.assertEquals(createdAt, user.getCreatedAt());
    }

    /**
     * <summary>
     * Проверяет подстановку текущего времени при передаче null-метки времени в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullCreatedAtShouldFallbackToCurrentTime() {
        var id = 1L;

        var username = "david";

        var user = new UserModel(id, username, true, null);

        Assertions.assertNotNull(user);

        Assertions.assertNotNull(user.getCreatedAt());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при передаче некорректного имени покупателя в полный конструктор.
     * </summary>
     **/
    @Test
    public void persistenceConstructorWithNullOrBlankUsernameShouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new UserModel(1L, null, true, LocalDateTime.now());
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new UserModel(1L, "  ", true, LocalDateTime.now());
        });
    }

    /**
     * <summary>
     * Проверяет успешную смену имени покупателя при передаче валидного значения.
     * </summary>
     **/
    @Test
    public void changeUsernameWithValidNameShouldUpdateUsername() {
        var user = new UserModel("old_name");

        var newUsername = "new_name";

        user.changeUsername(newUsername);

        Assertions.assertEquals(newUsername, user.getUsername());
    }

    /**
     * <summary>
     * Проверяет выброс исключения при попытке изменить имя покупателя на null, пустую строку или строку из пробелов.
     * </summary>
     **/
    @Test
    public void changeUsernameWithNullOrBlankNameShouldThrowException() {
        var user = new UserModel("valid_user");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.changeUsername(null);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.changeUsername("");
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            user.changeUsername("   ");
        });
    }

    /**
     * <summary>
     * Проверяет переведение учетной записи покупателя в активное состояние при вызове метода activate.
     * </summary>
     **/
    @Test
    public void activateShouldSetEnabledToTrue() {
        var user = new UserModel("david", false);

        user.activate();

        Assertions.assertTrue(user.getEnabled());
    }

    /**
     * <summary>
     * Проверяет переведение учетной записи покупателя в неактивное состояние при вызове метода deactivate.
     * </summary>
     **/
    @Test
    public void deactivateShouldSetEnabledToFalse() {
        var user = new UserModel("david", true);

        user.deactivate();

        Assertions.assertFalse(user.getEnabled());
    }

    // endregion
}