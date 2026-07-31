package ru.yandex.practicum.mymarket.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <summary>
 * Доменная модель, представляющая оформленный заказ покупателя.
 * </summary>
 **/
@Entity
@Table(name = "orders")
public class OrderModel {

    // region Fields

    /**
     * Уникальный идентификатор заказа в базе данных.
     **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Коллекция связанных исторических товарных позиций, входящих в данный заказ.
     **/
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemModel> items = new ArrayList<>();

    // endregion

    // region Constructors

    protected OrderModel() {

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
        items.add(new OrderItemModel(this, item.getTitle(), item.getPrice(), quantity));
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