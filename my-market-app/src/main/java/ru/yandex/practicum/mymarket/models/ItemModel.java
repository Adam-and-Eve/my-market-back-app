package ru.yandex.practicum.mymarket.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * <summary>
 * Модель сущности товара.
 * Представляет позицию в каталоге интернет-магазина с описанием, ценой и метаданными.
 * </summary>
 **/
@Table(name = "items")
public class ItemModel {

    // region Fields

    /**
     * Уникальный идентификатор товара в системе.
     **/
    @Id
    @Column("id")
    private Long id;

    /**
     * Наименование (заголовок) товара.
     **/
    @Column("title")
    private String title;

    /**
     * Подробное текстовое описание характеристик и свойств товара.
     **/
    @Column("description")
    private String description;

    /**
     * Относительный или абсолютный путь к файлу изображения товара.
     **/
    @Column("img_path")
    private String imgPath;

    /**
     * Стоимость товара.
     **/
    @Column("price")
    private long price;

    // endregion

    // region Constructors

    protected ItemModel() {

    }

    public ItemModel(
            final String title,
            final String description,
            final String imgPath,
            final long price) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Наименование товара не может быть пустым");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Описание товара не может быть пустым");
        }

        if (imgPath == null || imgPath.isBlank()) {
            throw new IllegalArgumentException("Изображение товара не может быть пустым");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Цена товара не может быть отрицательной");
        }

        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
    }

    public ItemModel(
            final Long id,
            final String title,
            final String description,
            final String imgPath,
            final long price) {

        if (id == null) {
            throw new IllegalArgumentException("Идентификатор товара не может быть null");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Наименование товара не может быть пустым");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Описание товара не может быть пустым");
        }

        if (imgPath == null || imgPath.isBlank()) {
            throw new IllegalArgumentException("Изображение товара не может быть пустым");
        }

        if (price < 0) {
            throw new IllegalArgumentException("Цена товара не может быть отрицательной");
        }

        this.id = id;
        this.title = title;
        this.description = description;
        this.imgPath = imgPath;
        this.price = price;
    }

    // endregion

    // region Properties

    /**
     * <summary>
     * Возвращает уникальный идентификатор товара.
     * </summary>
     * <return>
     * @return Идентификатор товара.
     * </return>
     **/
    public Long getId() {
        return id;
    }

    /**
     * <summary>
     * Возвращает наименование товара.
     * </summary>
     * <return>
     * @return Наименование товара.
     * </return>
     **/
    public String getTitle() {
        return title;
    }

    /**
     * <summary>
     * Возвращает подробное описание товара.
     * </summary>
     * <return>
     * @return Описание товара.
     * </return>
     **/
    public String getDescription() {
        return description;
    }

    /**
     * <summary>
     * Возвращает путь к изображению товара.
     * </summary>
     * <return>
     * @return Путь к файлу изображения.
     * </return>
     **/
    public String getImgPath() {
        return imgPath;
    }

    /**
     * <summary>
     * Возвращает стоимость товара.
     * </summary>
     * <return>
     * @return Стоимость товара.
     * </return>
     **/
    public long getPrice() {
        return price;
    }

    // endregion

    // region Methods



    // endregion
}