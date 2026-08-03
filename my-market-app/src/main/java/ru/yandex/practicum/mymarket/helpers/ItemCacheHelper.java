package ru.yandex.practicum.mymarket.helpers;

import org.springframework.stereotype.Component;

/**
 * <summary>
 * Вспомогательный компонент (Helper) для работы с инфраструктурой кэширования товаров.
 * </summary>
 **/
@Component
public class ItemCacheHelper {

    // region Constants

    /**
     * Статический ключ для хранения полного списка всех товаров каталога.
     **/
    private static final String ALL_ITEMS_KEY = "items:all";

    /**
     * Строковый префикс для формирования уникальных ключей отдельных карточек товаров.
     **/
    private static final String ITEM_KEY_PREFIX = "items:card:";

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает постоянный ключ для кэширования всей коллекции товаров каталога.
     * </summary>
     * <return>
     * @return Строковый ключ Redis для общего списка товаров.
     * </return>
     **/
    public String resolveAllItemsKey() {
        return ALL_ITEMS_KEY;
    }

    /**
     * <summary>
     * Формирует и возвращает валидный строковый ключ Redis для конкретной карточки товара на основе его идентификатора.
     * </summary>
     * @param id Уникальный идентификатор целевого товара.
     * <return>
     * @return Универсальный строковый ключ для кэша карточки товара.
     * </return>
     **/
    public String resolveItemKey(final long id) {
        return ITEM_KEY_PREFIX + id;
    }

    // endregion
}