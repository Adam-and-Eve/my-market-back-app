package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * <summary>
 * Конфигурационный класс для адаптации обработки данных HTML-форм в среде Spring WebFlux.
 * Устраняет различие в поведении между Spring MVC и WebFlux при связывании параметров тела запроса.
 * </summary>
 **/
@Configuration
public class WebFluxFormBindingConfiguration {

    // region Methods

    /**
     * <summary>
     * Создает и регистрирует реактивный WebFilter, который преобразует параметры из тела HTML-формы в Query-параметры.
     * Перехватывает POST-запросы с типом application/x-www-form-urlencoded и прозрачно переносит их в URI.
     * </summary>
     * <return>
     * @return Компонент WebFilter для нормализации входящих запросов от форм.
     * </return>
     **/
    @Bean
    public WebFilter formToQueryParamFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (request.getMethod() == HttpMethod.POST &&
                    MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(request.getHeaders().getContentType())) {

                return exchange.getFormData()
                        .flatMap(formData -> {
                            if (formData.isEmpty()) {
                                return chain.filter(exchange);
                            }

                            URI mutatedUri = UriComponentsBuilder.fromUri(request.getURI())
                                    .queryParams(formData)
                                    .encode()
                                    .build()
                                    .toUri();

                            ServerHttpRequest mutatedRequest = request.mutate()
                                    .uri(mutatedUri)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        });
            }

            return chain.filter(exchange);
        };
    }

    // endregion
}