package ru.yandex.practicum.payment.viewmodels;

/**
 * <summary>
 * Модель представления результатов выполнения платежной операции.
 * </summary>
 * @param success Флаг успешности проведения транзакции.
 * @param balance Текущий остаток денежных средств на счете после обработки запроса.
 * @param message Текстовое описание ошибки (заполняется только при дефиците средств или сбое).
 **/
public record PaymentResultViewModel(
        boolean success,
        long balance,
        String message
) {
    /**
     * <summary>
     * Фабричный метод для формирования объекта успешного платежа.
     * </summary>
     * @param balance Актуальный остаток средств на счете после списания.
     * <return>
     * @return Экземпляр модели с флагом успеха и пустым сообщением об ошибке.
     * </return>
     **/
    public static PaymentResultViewModel success(long balance) {
        return new PaymentResultViewModel(true, balance, null);
    }

    /**
     * <summary>
     * Фабричный метод для формирования объекта отклоненного платежа.
     * </summary>
     * @param balance Текущий неизмененный баланс пользователя.
     * @param message Причина отказа в проведении транзакции.
     * <return>
     * @return Экземпляр модели с флагом неуспеха и заполненной причиной ошибки.
     * </return>
     **/
    public static PaymentResultViewModel failed(long balance, String message) {
        return new PaymentResultViewModel(false, balance, message);
    }
}