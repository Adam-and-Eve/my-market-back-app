package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.configurations.properties.PaymentClientProperties;
import ru.yandex.practicum.mymarket.payment.client.ApiClient;
import ru.yandex.practicum.mymarket.payment.client.api.PaymentsApi;

/**
 * <summary>
 * Конфигурационный класс для настройки инфраструктуры сетевого взаимодействия с платежным шлюзом.
 * </summary>
 **/
@Configuration
public class PaymentClientConfiguration {

    // region Constants

    /**
     * Идентификатор регистрации OAuth2-клиента по умолчанию.
     **/
    private static final String USER_CLIENT_REGISTRATION_ID = "keycloak";

    // endregion

    // region Methods

    /**
     * <summary>
     * Создает и настраивает реактивный WebClient с интегрированным фильтром OAuth2-авторизации.
     * </summary>
     * @param properties Конфигурационные параметры клиента платежного шлюза.
     * @param clientRegistrationRepository Репозиторий регистраций OAuth2-клиентов.
     * @param authorizedClientRepository Репозиторий авторизованных OAuth2-клиентов.
     * <return>
     * @return Сконфигурированный объект WebClient для выполнения аутентифицированных запросов.
     * </return>
     **/
    @Bean
    public WebClient paymentWebClient(
            PaymentClientProperties properties,
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository
    ) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        clientRegistrationRepository,
                        authorizedClientRepository
                );

        oauth2.setDefaultClientRegistrationId(USER_CLIENT_REGISTRATION_ID);

        return ApiClient.buildWebClientBuilder(ApiClient.createDefaultMapper(null))
                .baseUrl(properties.baseUrl())
                .filter(oauth2)
                .build();
    }

    /**
     * <summary>
     * Создает экземпляр ApiClient с базовым URL и настроенным WebClient.
     * </summary>
     * @param properties Конфигурационные параметры клиента платежного шлюза.
     * @param paymentWebClient Сконфигурированный HTTP-клиент WebClient.
     * <return>
     * @return Настроенный клиент ApiClient для взаимодействия с платежным API.
     * </return>
     **/
    @Bean
    public ApiClient paymentApiClient(PaymentClientProperties properties, WebClient paymentWebClient) {
        return new ApiClient(paymentWebClient).setBasePath(properties.baseUrl());
    }

    /**
     * <summary>
     * Создает сервис PaymentsApi для выполнения вызовов к API платежного шлюза.
     * </summary>
     * @param paymentApiClient Сконфигурированный экземпляр ApiClient.
     * <return>
     * @return Готовый к использованию экземпляр PaymentsApi.
     * </return>
     **/
    @Bean
    public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
        return new PaymentsApi(paymentApiClient);
    }

    // endregion
}