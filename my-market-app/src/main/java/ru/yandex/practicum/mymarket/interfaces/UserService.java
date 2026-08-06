package ru.yandex.practicum.mymarket.interfaces;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.UserModel;

/**
 * <summary>
 * Контракт сервиса для управления персистентным состоянием и бизнес-логикой покупателей.
 * </summary>
 **/
public interface UserService {

    // region Methods

    /**
     * <summary>
     * Извлекает учетную запись покупателя по имени пользователя или создает и активирует новую при ее отсутствии.
     * </summary>
     * @param username Имя покупателя.
     * <return>
     * @return Реактивный контейнер Mono с созданной или найденной доменной моделью UserModel.
     * </return>
     **/
    public Mono<UserModel> findOrCreateByUsername(final String username);

    // endregion
}