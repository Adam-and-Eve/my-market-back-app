package ru.yandex.practicum.mymarket.models;

import jakarta.persistence.*;

/**
 * <summary>
 * Сущность элемента корзины покупателя.
 * Связывает конкретный товар из каталога с количеством единиц, добавленных пользователем в корзину.
 * </summary>
 **/
@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_cartitems_item_id", columnNames = "item_id")
)
public class CartItemModel {

    // region Fields

    /**
     * Уникальный идентификатор записи элемента корзины в базе данных.
     **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на связанную доменную модель товара из каталога.
     **/
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemModel item;

    /**
     * Количество единиц данного товара в корзине.
     **/
    @Column(nullable = false)
    private int quantity;

    // endregion

    // region Constructors

    protected CartItemModel() {

    }

    public CartItemModel(final ItemModel item, final int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает модель товара, привязанную к данной позиции в корзине.
     * </summary>
     * <return>
     * @return Доменная модель товара ItemModel.
     * </return>
     **/
    public ItemModel getItem() {
        return item;
    }

    /**
     * <summary>
     * Возвращает текущее добавленное количество единиц товара.
     * </summary>
     * <return>
     * @return Целочисленное значение количества.
     * </return>
     **/
    public int getQuantity() {
        return quantity;
    }

    /**
     * <summary>
     * Увеличивает количество единиц товара в корзине на одну единицу.
     * </summary>
     * */
    public void increase() {
        this.quantity++;
    }

    /**
     * <summary>
     * Уменьшает количество единиц товара в корзине на одну единицу.
     * </summary>
     **/
    public void decrease() {
        if (this.quantity > 0) {
            this.quantity--;
        }
    }

    // endregion
}