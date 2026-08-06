package ru.yandex.practicum.mymarket.viewmodels;

/**
 * <summary>
 * Модель представления итогового результата оформления и оплаты заказа.
 * Содержит статус успешности операции, идентификатор созданного заказа и диагностическое сообщение при отказе.
 * </summary>
 **/
public record CheckoutResultViewModel(
        Long orderId,
        boolean success,
        boolean emptyCart,
        String message
) {

    /**
     * <summary>
     * Фабричный метод для создания успешного результата покупки с фиксацией идентификатора созданного заказа.
     * </summary>
     * @param orderId Уникальный идентификатор успешно созданного заказа.
     * <return>
     * @return Модель представления успешного результата покупки.
     * </return>
     **/
    public static CheckoutResultViewModel paid(long orderId) {
        return new CheckoutResultViewModel(orderId, true, false, null);
    }

    /**
     * <summary>
     * Фабричный метод для создания результата оформления покупки при обнаружении пустой корзины.
     * </summary>
     * <return>
     * @return Модель представления результата с установленным флагом пустой корзины.
     * </return>
     **/
    public static CheckoutResultViewModel empty() {
        return new CheckoutResultViewModel(null, false, true, null);
    }

    /**
     * <summary>
     * Фабричный метод для создания отклоненного результата покупки с фиксацией причины отказа.
     * </summary>
     * @param message Текст сообщения об ошибке или причине отклонения транзакции от платежного сервиса.
     * <return>
     * @return Модель представления отклоненного результата покупки с описанием ошибки.
     * </return>
     **/
    public static CheckoutResultViewModel rejected(String message) {
        return new CheckoutResultViewModel(null, false, false, message);
    }
}