package ru.yandex.practicum.mymarket.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.UserService;
import ru.yandex.practicum.mymarket.models.UserModel;
import ru.yandex.practicum.mymarket.repositories.UserRepository;

/**
 * <summary>
 * Сервис для управления персистентным состоянием и бизнес-логикой покупателей.
 * </summary>
 **/
@Service
public class UserServiceImpl implements UserService {

    // region Fields

    private final UserRepository userRepository;

    // endregion

    // region Constructors

    public UserServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Извлекает учетную запись покупателя из базы данных по его имени пользователя без автоматического создания нового профиля.
     * </summary>
     * @param username Имя покупателя для поиска.
     * <return>
     * @return Реактивный контейнер Mono с доменной моделью UserModel, или Mono.empty(), если пользователь не найден.
     * </return>
     **/
    @Override
    @Transactional(readOnly = true)
    public Mono<UserModel> findByUsername(final String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * <summary>
     * Извлекает учетную запись покупателя по имени пользователя или создает и активирует новую при ее отсутствии.
     * </summary>
     * @param username Имя покупателя.
     * <return>
     * @return Реактивный контейнер Mono с созданной или найденной доменной моделью UserModel.
     * </return>
     **/
    @Override
    @Transactional
    public Mono<UserModel> findOrCreateByUsername(final String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> {
                    var userModel = new UserModel(username);

                    userModel.activate();

                    return userRepository.save(userModel);
                }));
    }

    // endregion
}