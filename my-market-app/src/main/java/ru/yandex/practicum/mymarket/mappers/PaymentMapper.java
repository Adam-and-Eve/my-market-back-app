package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.helpers.PaymentHelper;
import ru.yandex.practicum.mymarket.payment.client.model.BalanceResponse;
import ru.yandex.practicum.mymarket.payment.client.model.PaymentResponse;
import ru.yandex.practicum.mymarket.viewmodels.OrderPaymentResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;

/**
 * <summary>
 * Компонент-маппер для преобразования ответов платежного сервиса в модели представления.
 * </summary>
 **/
@Component
public class PaymentMapper {

    // region Fields

    private final PaymentHelper paymentHelper;

    // endregion

    // region Constructors

    public PaymentMapper(final PaymentHelper paymentHelper) {
        this.paymentHelper = paymentHelper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Преобразует ответ платежной системы в модель представления доступности платежного сервиса и баланса.
     * </summary>
     * @param response Объект ответа платежной системы BalanceResponse.
     * <return>
     * @return Сконвертированная модель представления PaymentAvailabilityViewModel.
     * </return>
     **/
    public PaymentAvailabilityViewModel toAvailabilityViewModel(final BalanceResponse response) {
        if (response == null) {
            return PaymentAvailabilityViewModel.unavailable(paymentHelper.resolveDefaultRejectedMessage());
        }
        return PaymentAvailabilityViewModel.available(response.getBalance());
    }

    /**
     * <summary>
     * Преобразует доменный ответ платежного шлюза в модель представления результата оплаты заказа.
     * Анализирует флаг успешности и нормализует текстовые сообщения ответа с помощью хелпера.
     * </summary>
     * @param response Объект ответа платежной системы PaymentResponse.
     * <return>
     * @return Заполненная модель представления OrderPaymentResultViewModel со статусом операции.
     * </return>
     **/
    public OrderPaymentResultViewModel toOrderPaymentResultViewModel(final PaymentResponse response) {
        if (response == null) {
            return OrderPaymentResultViewModel.rejected(0L, paymentHelper.resolveDefaultRejectedMessage());
        }

        if (response.getSuccess()) {
            return OrderPaymentResultViewModel.success(response.getBalance());
        }

        return OrderPaymentResultViewModel.rejected(
                response.getBalance(),
                paymentHelper.normalizeMessage(response.getMessage())
        );
    }

    // endregion
}