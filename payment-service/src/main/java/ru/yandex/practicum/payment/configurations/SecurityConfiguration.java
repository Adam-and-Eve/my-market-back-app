package ru.yandex.practicum.payment.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * <summary>
 * Конфигурационный класс Spring Security WebFlux для платежного сервиса.
 * Отвечает за настройку правил доступа к HTTP-эндпоинтам и валидацию токенов OAuth2 Resource Server (JWT).
 * </summary>
 **/
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    // region Methods

    /**
     * <summary>
     * Настраивает реактивную цепочку фильтров безопасности Spring Security (SecurityWebFilterChain).
     * Отключает защиту CSRF, определяет правила авторизации для служебных и бизнес-маршрутов,
     * а также подключает проверку JWT-токенов через OAuth2 Resource Server.
     * </summary>
     * @param http Построитель конфигурации реактивной HTTP-безопасности ServerHttpSecurity.
     * <return>
     * @return Сконфигурированная и собранная цепочка фильтров SecurityWebFilterChain.
     * </return>
     **/
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/payments/**").authenticated()
                        .anyExchange().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    // endregion
}