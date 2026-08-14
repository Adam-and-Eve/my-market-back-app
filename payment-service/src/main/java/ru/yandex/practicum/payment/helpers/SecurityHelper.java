package ru.yandex.practicum.payment.helpers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * <summary>
 * Вспомогательный компонент для работы с контекстом безопасности Spring Security WebFlux.
 * Предоставляет методы для извлечения информации о текущем аутентифицированном пользователе из JWT-токена.
 * </summary>
 **/
@Component
public class SecurityHelper {

    // region Methods

    /**
     * <summary>
     * Извлекает имя текущего аутентифицированного пользователя из реактивного контекста безопасности.
     * </summary>
     * <return>
     * @return Реактивный контейнер Mono с имя пользователя из JWT-токена.
     * </return>
     * @throws ResponseStatusException Если контекст аутентификации отсутствует, токен недействителен или не содержит имя пользователя (HTTP 401).
     **/
    public Mono<String> currentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(this::extractUsername)
                .filter(name -> !name.isBlank())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Не удалось определить пользователя из токена"
                )));
    }

    /**
     * <summary>
     * Извлекает имя пользователя из утверждений (claims) JWT-токена.
     * В первую очередь ищет утверждение "preferred_username", а в случае его отсутствия или пустоты использует "sub" (subject).
     * </summary>
     * @param jwt Объект декодированного JWT-токена.
     * <return>
     * @return Имя пользователя или идентификатор субъекта токена.
     * </return>
     **/
    private String extractUsername(Jwt jwt) {
        var preferred = jwt.getClaimAsString("preferred_username");

        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }

        var subject = jwt.getSubject();

        return subject != null ? subject : "";
    }

    // endregion
}