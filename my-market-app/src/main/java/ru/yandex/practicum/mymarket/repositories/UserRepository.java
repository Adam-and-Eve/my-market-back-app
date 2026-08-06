package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.UserModel;

/**
 * <summary>
 * Интерфейс репозитория для выполнения операций CRUD и управления персистентным состоянием доменных моделей покупателей UserModel.
 * </summary>
 **/
public interface UserRepository extends ReactiveCrudRepository<UserModel, Long> {

    // region Methods

    /**
     * <summary>
     * Извлекает учетную запись покупателя из базы данных по его имени пользователя.
     * </summary>
     * @param username Имя покупателя для поиска.
     * <return>
     * @return Реактивный контейнер Mono с доменной моделью покупателя UserModel.
     * </return>
     **/
    public Mono<UserModel> findByUsername(String username);

    // endregion
}