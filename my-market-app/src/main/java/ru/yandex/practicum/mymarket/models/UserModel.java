package ru.yandex.practicum.mymarket.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * <summary>
 * Доменная модель, представляющая покупателя.
 * </summary>
 **/
@Table(name = "users")
public class UserModel {

    // region Fields

    /**
     * Уникальный идентификатор покупателя в базе данных.
     **/
    @Id
    @Column("id")
    private Long id;

    /**
     * Имя покупателя в базе данных.
     **/
    @Column("username")
    private String username;

    /**
     * Статус активности покупателя в базе данных.
     **/
    @Column("enabled")
    private boolean enabled;

    /**
     * Дата и время создания учетной записи покупателя в базе данных.
     **/
    @Column("created_at")
    private final LocalDateTime createdAt;

    // endregion

    // region Constructors

    public UserModel(
            final String username
    ) {
        this(username, false);
    }

    public UserModel(
            final String username,
            final boolean enabled
    ) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя покупателя не может быть пустым");
        }

        this.username = username;
        this.enabled = enabled;
        this.createdAt = LocalDateTime.now();
    }

    @PersistenceCreator
    public UserModel(
            final Long id,
            final String username,
            final Boolean enabled,
            final LocalDateTime createdAt
    )
    {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя покупателя не может быть пустым");
        }

        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает уникальный идентификатор покупателя.
     * </summary>
     * <return>
     * @return Идентификатор покупателя или null, если сущность еще не сохранена в БД.
     * </return>
     **/
    public final Long getId() {
        return id;
    }

    /**
     * <summary>
     * Возвращает имя покупателя.
     * </summary>
     * <return>
     * @return Имя пользователя.
     * </return>
     **/
    public final String getUsername() {
        return username;
    }

    /**
     * <summary>
     * Возвращает текущий статус активности учетной записи покупателя.
     * </summary>
     * <return>
     * @return Флаг активности учетной записи.
     * </return>
     **/
    public final boolean getEnabled() {
        return enabled;
    }

    /**
     * <summary>
     * Возвращает дату и время создания учетной записи покупателя.
     * </summary>
     * <return>
     * @return Метка времени создания учетной записи.
     * </return>
     **/
    public final LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Обновляет имя покупателя.
     * </summary>
     * @param username Новое имя покупателя.
     **/
    public final void changeUsername(final String username) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя покупателя не может быть пустым");
        }

        this.username = username;
    }

    /**
     * <summary>
     * Переводит учетную запись покупателя в активное состояние.
     * </summary>
     **/
    public final void activate() {
        this.enabled = true;
    }

    /**
     * <summary>
     * Переводит учетную запись покупателя в неактивное состояние.
     * </summary>
     **/
    public final void deactivate() {
        this.enabled = false;
    }

    // endregion
}