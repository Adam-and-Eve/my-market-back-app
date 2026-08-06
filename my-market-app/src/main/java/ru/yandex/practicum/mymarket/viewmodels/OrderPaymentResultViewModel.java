package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления результатов оплаты заказа.
 * </summary>
 **/
public record OrderPaymentResultViewModel(
        boolean success,
        boolean serviceAvailable,
        long balance,
        String message
) {
    /**
     * <summary>
     * Фабричный метод для формирования успешного результата транзакции.
     * </summary>
     * @param balance Остаток средств на счете после списания.
     * <return>
     * @return Объект OrderPaymentResultViewModel со статусом успешной операции.
     * </return>
     **/
    public static OrderPaymentResultViewModel success(long balance) {
        return new OrderPaymentResultViewModel(true, true, balance, null);
    }

    /**
     * <summary>
     * Фабричный метод для формирования отклоненного результата транзакции (например, при нехватке средств).
     * </summary>
     * @param balance Текущий остаток средств на счете.
     * @param message Текст сообщения с причиной отклонения платежа.
     * <return>
     * @return Объект OrderPaymentResultViewModel со статусом отклоненной операции.
     * </return>
     **/
    public static OrderPaymentResultViewModel rejected(long balance, String message) {
        return new OrderPaymentResultViewModel(false, true, balance, message);
    }

    /**
     * <summary>
     * Фабричный метод для сценариев технического сбоя или полной недоступности удаленного сервиса авторизации платежей.
     * </summary>
     * @param message Описание ошибки сетевого взаимодействия.
     * <return>
     * @return Объект OrderPaymentResultViewModel со статусом недоступности сервиса.
     * </return>
     **/
    public static OrderPaymentResultViewModel unavailable(String message) {
        return new OrderPaymentResultViewModel(false, false, 0, message);
    }
}