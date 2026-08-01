package ru.yandex.practicum.mymarket.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <summary>
 * Доменная модель, представляющая оформленный заказ покупателя.
 * </summary>
 **/
@Table(name = "orders")
public class OrderModel {

    // region Fields

    /**
     * Уникальный идентификатор заказа в базе данных.
     **/
    @Id
    @Column("id")
    private Long id;

    /**
     * Дата и время создания заказа.
     **/
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Текущий статус заказа.
     **/
    @Column("status")
    private String status;

    /**
     * Коллекция связанных исторических товарных позиций, входящих в данный заказ.
     **/
    @Transient
    private List<OrderItemModel> items = new ArrayList<>();

    // endregion

    // region Constructors

    protected OrderModel() {
        this.createdAt = LocalDateTime.now();
        this.status = "CREATED";
    }

    private OrderModel(
            final LocalDateTime createdAt,
            final String status) {

        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = status != null ? status : "CREATED";
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает уникальный идентификатор сохраненного заказа.
     * </summary>
     * <return>
     * @return Идентификатор заказа или null, если сущность еще не сохранена в БД.
     * </return>
     **/
    public Long getId(){
        return id;
    }

    /**
     * <summary>
     * Возвращает дату и время создания заказа.
     * </summary>
     * <return>
     * @return Метка времени создания заказа.
     * </return>
     **/
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * <summary>
     * Возвращает текущий статус заказа.
     * </summary>
     * <return>
     * @return Строковое представление статуса.
     * </return>
     **/
    public String getStatus() {
        return status;
    }

    /**
     * <summary>
     * Возвращает список всех товарных позиций, зафиксированных внутри данного заказа.
     * </summary>
     * <return>
     * @return Список доменных моделей элементов заказа.
     * </return>
     **/
    public List<OrderItemModel> getItems() {
        return items;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Создает и добавляет новую историческую позицию в заказ на основе переданной модели товара и количества.
     * </summary>
     * @param item Исходная доменная модель товара из каталога для фиксации его актуальных метрик.
     * @param quantity Количество приобретаемых единиц товара.
     **/
    public void addItem(final ItemModel item, final int quantity) {

        if (item == null) {
            throw new IllegalArgumentException("Товар не может быть null при добавлении в заказ");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество добавляемого в заказ товара должно быть строго больше нуля");
        }

        items.add(new OrderItemModel(id, item.getTitle(), item.getPrice(), quantity));
    }

    /**
     * <summary>
     * Статический фабричный метод для создания нового пустого экземпляра доменной модели заказа.
     * </summary>
     * <return>
     * @return Новый пустой объект OrderModel.
     * </return>
     **/
    public static OrderModel create() {
        return new OrderModel();
    }

    // endregion
}