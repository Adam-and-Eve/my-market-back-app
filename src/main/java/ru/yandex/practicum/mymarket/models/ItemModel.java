package ru.yandex.practicum.mymarket.models;

import jakarta.persistence.*;

/**
 * <summary>
 * Модель сущности товара.
 * Представляет позицию в каталоге интернет-магазина с описанием, ценой и метаданными.
 * </summary>
 **/
@Entity
@Table(name = "Items")
public class ItemModel {

    // region Fields

    /**
     * Уникальный идентификатор товара в системе.
     **/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование (заголовок) товара.
     **/
    @Column(nullable = false)
    private String title;

    /**
     * Подробное текстовое описание характеристик и свойств товара.
     **/
    @Column(nullable = false, length = 1024)
    private String description;

    /**
     * Относительный или абсолютный путь к файлу изображения товара.
     **/
    @Column(nullable = false)
    private String imgPath;

    /**
     * Стоимость товара.
     **/
    @Column(nullable = false)
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