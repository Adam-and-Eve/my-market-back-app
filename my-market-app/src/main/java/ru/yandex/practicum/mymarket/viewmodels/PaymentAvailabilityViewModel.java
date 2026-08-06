package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления для проверки доступности платежного сервиса.
 * </summary>
 **/
public record PaymentAvailabilityViewModel (
        boolean available,
        long balance,
        String message
) {
    /**
     * <summary>
     * Фабричный метод для формирования успешного ответа, когда платежный сервис доступен.
     * </summary>
     * @param balance Текущий подтвержденный остаток средств на счете пользователя.
     * <return>
     * @return Объект PaymentAvailabilityViewModel с активным статусом доступности.
     * </return>
     **/
    public static PaymentAvailabilityViewModel available(long balance) {
        return new PaymentAvailabilityViewModel(true, balance, null);
    }

    /**
     * <summary>
     * Фабричный метод для сценариев, когда платежный сервис недоступен из-за сетевых или системных сбоев.
     * </summary>
     * @param message Описание возникшей технической ошибки или причины отказа.
     * <return>
     * @return Объект PaymentAvailabilityViewModel со статусом недоступности сервиса.
     * </return>
     **/
    public static PaymentAvailabilityViewModel unavailable(String message) {
        return new PaymentAvailabilityViewModel(false, 0, message);
    }
}