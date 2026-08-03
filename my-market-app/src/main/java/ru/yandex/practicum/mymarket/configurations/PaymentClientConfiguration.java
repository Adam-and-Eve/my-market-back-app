package ru.yandex.practicum.mymarket.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // region Methods

    /**
     * <summary>
     * Создает и настраивает базовый HTTP-клиент для платежного API, устанавливая целевой адрес шлюза.
     * </summary>
     * @param properties Конфигурационные свойства с параметрами подключения.
     * <return>
     * @return Сконфигурированный экземпляр ApiClient.
     * </return>
     **/
    @Bean
    public ApiClient paymentApiClient(PaymentClientProperties properties) {
        return new ApiClient().setBasePath(properties.baseUrl());
    }

    /**
     * <summary>
     * Инициализирует сгенерированный компонент PaymentsApi для выполнения вызовов к эндпоинтам платежной системы.
     * </summary>
     * @param paymentApiClient Настроенный базовый HTTP-клиент взаимодействия.
     * <return>
     * @return Реактивный клиент PaymentsApi, готовый к внедрению в сервисный слой.
     * </return>
     **/
    @Bean
    public PaymentsApi paymentsApi(ApiClient paymentApiClient) {
        return new PaymentsApi(paymentApiClient);
    }

    // endregion
}