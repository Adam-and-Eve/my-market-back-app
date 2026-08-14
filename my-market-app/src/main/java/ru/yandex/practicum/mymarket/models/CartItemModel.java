package ru.yandex.practicum.mymarket.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * <summary>
 * Сущность элемента корзины покупателя.
 * Связывает конкретный товар из каталога с количеством единиц, добавленных пользователем в корзину.
 * </summary>
 **/

@Table("cart_items")
public class CartItemModel {

    // region Fields

    /**
     * Уникальный идентификатор записи элемента корзины в базе данных.
     **/
    @Id
    @Column("id")
    private Long id;

    /**
     * Уникальный идентификатор покупателя в базе данных.
     **/
    @Column("user_id")
    private Long userId;

    /**
     * Уникальный идентификатор записи связанного элемента товара в базе данных.
     **/
    @Column("item_id")
    private Long itemId;

    /**
     * Ссылка на связанную доменную модель товара из каталога.
     **/
    @Transient
    private ItemModel item;

    /**
     * Количество единиц данного товара в корзине.
     **/
    @Column("quantity")
    private int quantity;

    // endregion

    // region Constructors

    protected CartItemModel() {

    }

    public CartItemModel(
            final ItemModel item,
            final int quantity) {

        if (item == null) {
            throw new IllegalArgumentException("Модель товара не может быть null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Количество товара в корзине не может быть отрицательным");
        }

        this.item = item;
        this.itemId = item.getId();
        this.quantity = quantity;
    }

    public CartItemModel(
            final Long userId,
            final Long itemId,
            final int quantity) {

        if (userId == null) {
            throw new IllegalArgumentException("Идентификатор покупателя не может быть null");
        }

        if (itemId == null) {
            throw new IllegalArgumentException("Идентификатор товара не может быть null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Количество товара в корзине не может быть отрицательным");
        }

        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    @PersistenceCreator
    public CartItemModel(
            final Long id,
            final Long userId,
            final Long itemId,
            final int quantity) {

        if (id == null) {
            throw new IllegalArgumentException("Идентификатор элемента корзины не может быть null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("Идентификатор покупателя не может быть null");
        }

        if (itemId == null) {
            throw new IllegalArgumentException("Идентификатор товара не может быть null");
        }

        if (quantity < 0) {
            throw new IllegalArgumentException("Количество товара в корзине не может быть отрицательным");
        }

        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает уникальный идентификатор записи элемента корзины в базе данных.
     * </summary>
     * <return>
     * @return Уникальный идентификатор записи элемента корзины в базе данных.
     * </return>
     **/
    public Long getId() {
        return id;
    }

    /**
     * <summary>
     * Возвращает уникальный идентификатор покупателя элемента корзины в базе данных.
     * </summary>
     * <return>
     * @return Уникальный идентификатор покупателя элемента корзины в базе данных.
     * </return>
     **/
    public Long getUserId() {
        return userId;
    }

    /**
     * <summary>
     * Возвращает уникальный идентификатор записи связанного элемента товара в базе данных.
     * </summary>
     * <return>
     * @return Уникальный идентификатор записи связанного элемента товара в базе данных.
     * </return>
     **/
    public Long getItemId() {
        return itemId;
    }

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