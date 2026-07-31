package ru.yandex.practicum.mymarket.models;

import jakarta.persistence.*;

/**
 * <summary>
 * Доменная модель, представляющая отдельную товарную позицию внутри сохраненного заказа.
 * Фиксирует состояние товара (название и цену) на момент совершения покупки, обеспечивая
 * неизменность финансовой истории при последующих редактированиях основного каталога.
 * </summary>
 **/
@Entity
@Table(name = "order_items")
public class OrderItemModel {

    // region Fields

    /**
     * Идентификатор позиции заказа.
     **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Информация о заказе.
     **/
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderModel order;

    /**
     * Название товара, зафиксированное при оформлении заказа.
     **/
    @Column(nullable = false)
    private String title;

    /**
     * Историческая стоимость единицы товара на момент покупки.
     **/
    @Column(nullable = false)
    private long price;

    /**
     * Количество единиц товара, приобретенных в рамках данной позиции.
     **/
    @Column(nullable = false)
    private int quantity;

    // endregion

    // region Constructors

    protected OrderItemModel() {

    }

    public OrderItemModel(
            final OrderModel order,
            final String title,
            final long price,
            final int quantity) {

        this.order = order;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
    }

    // endregion

    // region Properties

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
