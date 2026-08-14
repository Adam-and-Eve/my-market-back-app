package ru.yandex.practicum.payment.helpers;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Map;

/**
 * <summary>
 * Модульные тесты для проверки корректности работы SecurityHelper.
 * </summary>
 **/
public class SecurityHelperTest {

    // region Fields

    private final SecurityHelper securityHelper = new SecurityHelper();

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет успешное извлечение имени пользователя из клейма preferred_username.
     * </summary>
     **/
    @Test
    void currentUsernameShouldReturnPreferredUsernameWhenPresent() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", "user")
                .claim("sub", "sub_id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authentication = new JwtAuthenticationToken(jwt);
        var securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        securityHelper.currentUsername()
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .expectNext("user")
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет fallback на subject (sub), если preferred_username отсутствует или пустой.
     * </summary>
     **/
    @Test
    void currentUsernameShouldFallBackToSubWhenPreferredUsernameIsMissing() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "subject_user_id")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authentication = new JwtAuthenticationToken(jwt);

        var securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        securityHelper.currentUsername()
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .expectNext("subject_user_id")
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет возврат ошибки HTTP 401 Unauthorized, если контекст безопасности пустой.
     * </summary>
     **/
    @Test
    void currentUsernameShouldReturnUnauthorizedWhenContextIsEmpty() {
        StepVerifier.create(securityHelper.currentUsername())
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().value() == 401 &&
                                "Не удалось определить пользователя из токена".equals(((ResponseStatusException) throwable).getReason())
                )
                .verify();
    }

    /**
     * <summary>
     * Проверяет возврат ошибки HTTP 401 Unauthorized, если тип аутентификации не JWT.
     * </summary>
     **/
    @Test
    void currentUsernameShouldReturnUnauthorizedWhenNotJwtAuthentication() {
        var authentication = new UsernamePasswordAuthenticationToken("user", "password");

        var securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        securityHelper.currentUsername()
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().value() == 401
                )
                .verify();
    }

    /**
     * <summary>
     * Проверяет возврат ошибки HTTP 401 Unauthorized, если и preferred_username, и sub оказались пустыми.
     * </summary>
     **/
    @Test
    void currentUsernameShouldReturnUnauthorizedWhenUsernameClaimsAreBlank() {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claims(claims -> claims.putAll(Map.of()))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        var authentication = new JwtAuthenticationToken(jwt);

        var securityContext = new SecurityContextImpl(authentication);

        StepVerifier.create(
                        securityHelper.currentUsername()
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
                )
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().value() == 401
                )
                .verify();
    }

    // endregion
}