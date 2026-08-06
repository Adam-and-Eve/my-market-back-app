package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import ru.yandex.practicum.mymarket.configurations.properties.KeycloakProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * <summary>
 * Конфигурация безопасности Spring WebFlux и авторизации OIDC/OAuth2.
 * </summary>
 **/
@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    // region Fields

    private final KeycloakProperties keycloakProperties;

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    // endregion

    // region Constructors

    /**
     * <summary>
     * Инициализирует конфигурацию безопасности необходимыми зависимостями.
     * </summary>
     * @param keycloakProperties Настройки Keycloak из конфигурации приложения.
     * @param clientRegistrationRepository Репозиторий регистраций OAuth2-клиентов.
     **/
    public SecurityConfiguration(
            final KeycloakProperties keycloakProperties,
            final ReactiveClientRegistrationRepository clientRegistrationRepository) {

        this.keycloakProperties = keycloakProperties;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Настраивает реактивную цепочку фильтров безопасности, правила доступа к эндпоинтам, OIDC-вход и логаут.
     * </summary>
     * @param http Объект ServerHttpSecurity для настройки конфигурации реактивной безопасности.
     * <return>
     * @return Сконфигурированная цепочка фильтров безопасности SecurityWebFilterChain.
     * </return>
     **/
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/templates/**","/static/images/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/items", "/items/*").authenticated()
                        .pathMatchers("/cart/**", "/orders/**", "/buy", "/logout").authenticated()
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/*").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(oidcLogoutSuccessHandler())
                )
                .build();
    }

    /**
     * <summary>
     * Создает обработчик успешного завершения сессии OIDC с перенаправлением на главную страницу.
     * </summary>
     * <return>
     * @return Обработчик завершения сессии ServerLogoutSuccessHandler.
     * </return>
     **/
    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {

        OidcClientInitiatedServerLogoutSuccessHandler successHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(oidcLogoutClientRegistrationRepository());

        successHandler.setPostLogoutRedirectUri("{baseUrl}/");

        return successHandler;
    }

    /**
     * <summary>
     * Оборачивает репозиторий регистраций клиентов для подстановки метаданных эндпоинта завершения сессии.
     * </summary>
     * <return>
     * @return Реактивный репозиторий регистраций клиентов с обновленными метаданными.
     * </return>
     **/
    private ReactiveClientRegistrationRepository oidcLogoutClientRegistrationRepository() {

        return registrationId -> clientRegistrationRepository.findByRegistrationId(registrationId)
                .map(this::withEndSessionEndpoint);
    }

    /**
     * <summary>
     * Добавляет эндпоинт выхода из сессии Keycloak в метаданные провайдера клиента.
     * Содержит защиту от NullPointerException при отсутствии метаданных.
     * </summary>
     * @param clientRegistration Исходный объект регистрации клиента.
     * <return>
     * @return Объект регистрации клиента с добавленным параметром end_session_endpoint.
     * </return>
     **/
    private ClientRegistration withEndSessionEndpoint(ClientRegistration clientRegistration) {

        var originalMetadata = clientRegistration.getProviderDetails().getConfigurationMetadata();

        Map<String, Object> metadata = new HashMap<>(originalMetadata);

        metadata.put("end_session_endpoint", keycloakProperties.logoutUri());

        return ClientRegistration.withClientRegistration(clientRegistration)
                .providerConfigurationMetadata(metadata)
                .build();
    }

    // endregion
}