package ru.yandex.practicum.mymarket.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * <summary>
 * Доменная модель, представляющая отдельную товарную позицию внутри сохраненного заказа.
 * Фиксирует состояние товара (название и цену) на момент совершения покупки, обеспечивая
 * неизменность финансовой истории при последующих редактированиях основного каталога.
 * </summary>
 **/

@Table(name = "order_items")
public class OrderItemModel {

    // region Fields

    /**
     * Идентификатор позиции заказа.
     **/
    @Id
    @Column("id")
    private Long id;

    /**
     * Идентификатор заказа.
     **/
    @Column("order_id")
    private Long orderId;

    /**
     * Название товара, зафиксированное при оформлении заказа.
     **/
    @Column("title")
    private String title;

    /**
     * Историческая стоимость единицы товара на момент покупки.
     **/
    @Column("price")
    private long price;

    /**
     * Количество единиц товара, приобретенных в рамках данной позиции.
     **/
    @Column("quantity")
    private int quantity;

    // endregion

    // region Constructors

    protected OrderItemModel() {

    }

    public OrderItemModel(
            final Long orderId,
            final String title,
            final long price,
            final int quantity) {

        if (orderId == null) {
            throw new IllegalArgumentException("Идентификатор заказа не может быть null");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Наименование позиции заказа не может быть пустым");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Цена позиции заказа не может быть отрицательной");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество в позиции заказа должно быть строго больше нуля");
        }

        this.orderId = orderId;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает идентификатор позиции заказа.
     * </summary>
     * <return>
     * @return Идентификатор позиции заказа.
     * </return>
     **/
    public Long getId() {
        return id;
    }

    /**
     * <summary>
     * Возвращает идентификатор заказа.
     * </summary>
     * <return>
     * @return Идентификатор заказа.
     * </return>
     **/
    public Long getOrderId() {
        return orderId;
    }

    /**
     * <summary>
     * Возвращает название товара, зафиксированное при оформлении заказа.
     * </summary>
     * <return>
     * @return Текстовое наименование позиции.
     * </return>
     **/
    public String getTitle() {
        return title;
    }

    /**
     * <summary>
     * Возвращает историческую стоимость единицы товара на момент покупки.
     * </summary>
     * <return>
     * @return Цена товара в минимальных денежных единицах.
     * </return>
     **/
    public long getPrice() {
        return price;
    }

    /**
     * <summary>
     * Возвращает количество единиц товара, приобретенных в рамках данной позиции.
     * </summary>
     * <return>
     * @return Количество выкупленного товара.
     * </return>
     **/
    public int getQuantity() {
        return quantity;
    }

    // endregion
}
