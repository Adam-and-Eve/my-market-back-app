package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.models.UserModel;
import ru.yandex.practicum.mymarket.repositories.UserRepository;

import java.time.LocalDateTime;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики UserServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    // region Constants

    private static final String TEST_USERNAME = "user";

    private static final Long TEST_USER_ID = 1L;

    // endregion

    // region Fields

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // endregion

    // region Tests for findOrCreateByUsername

    /**
     * <summary>
     * Проверяет, что метод возвращает существующую учетную запись покупателя из репозитория
     * и не вызывает процедуру сохранения новой модели.
     * </summary>
     **/
    @Test
    void findOrCreateByUsernameShouldReturnExistingUserWhenUserExists() {
        var existingUser = new UserModel(TEST_USER_ID, TEST_USERNAME, true, LocalDateTime.now());

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.just(existingUser));

        StepVerifier.create(userService.findOrCreateByUsername(TEST_USERNAME))
                .expectNext(existingUser)
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(TEST_USERNAME);

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    /**
     * <summary>
     * Проверяет, что при отсутствии покупателя в базе данных создается новая сущность UserModel,
     * активируется (enabled = true) и сохраняется через репозиторий.
     * </summary>
     **/
    @Test
    void findOrCreateByUsernameShouldCreateAndActivateNewUserWhenUserDoesNotExist() {
        var expectedSavedUserId = 10L;

        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.empty());

        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);

        Mockito.when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
            UserModel userToSave = invocation.getArgument(0);

            ReflectionTestUtils.setField(userToSave, "id", expectedSavedUserId);

            return Mono.just(userToSave);
        });

        StepVerifier.create(userService.findOrCreateByUsername(TEST_USERNAME))
                .assertNext(savedUser -> {
                    Assertions.assertEquals(expectedSavedUserId, savedUser.getId());
                    Assertions.assertEquals(TEST_USERNAME, savedUser.getUsername());
                    Assertions.assertTrue(savedUser.getEnabled());
                    Assertions.assertNotNull(savedUser.getCreatedAt());
                })
                .verifyComplete();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(TEST_USERNAME);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserModel.class));

        UserModel capturedUser = userCaptor.getValue();

        Assertions.assertNotNull(capturedUser);

        Assertions.assertEquals(TEST_USERNAME, capturedUser.getUsername());

        Assertions.assertTrue(capturedUser.getEnabled(), "Новый пользователь должен быть активирован перед сохранением");
    }

    /**
     * <summary>
     * Проверяет проброс ошибки создания UserModel, если передан пустое или некорректное имя покупателя.
     * </summary>
     **/
    @Test
    void findOrCreateByUsernameShouldThrowExceptionWhenUsernameIsBlankAndUserDoesNotExist() {
        var invalidUsername = "   ";

        Mockito.when(userRepository.findByUsername(invalidUsername)).thenReturn(Mono.empty());

        StepVerifier.create(userService.findOrCreateByUsername(invalidUsername))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                "Имя покупателя не может быть пустым".equals(throwable.getMessage())
                )
                .verify();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(invalidUsername);

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    /**
     * <summary>
     * Проверяет проброс ошибки базы данных, если сбой происходит на этапе сохранения сущности.
     * </summary>
     **/
    @Test
    void findOrCreateByUsernameShouldPropagateErrorWhenSaveFails() {
        Mockito.when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Mono.empty());

        Mockito.when(userRepository.save(Mockito.any(UserModel.class)))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(userService.findOrCreateByUsername(TEST_USERNAME))
                .expectError(RuntimeException.class)
                .verify();

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(TEST_USERNAME);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserModel.class));
    }

    // endregion
}