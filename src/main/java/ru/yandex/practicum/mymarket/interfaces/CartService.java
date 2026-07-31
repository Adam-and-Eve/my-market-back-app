package ru.yandex.practicum.mymarket.interfaces;

import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;

import java.util.List;
import java.util.Map;

/**
 * <summary>
 * Контракт сервиса для управления бизнес-логикой корзины покупателя.
 * </summary>
 **/
public interface CartService {

    // region Methods

    /**
     * <summary>
     * Сборка и расчет агрегированных данных корзины для формирования полноценной страницы в UI.
     * </summary>
     * <return>
     * @return Модель представления страницы корзины CartPageViewModel с подсчитанной итоговой стоимостью.
     * </return>
     **/
    public CartPageViewModel findCart();

    /**
     * <summary>
     * Обновляет количество указанного товара в корзине в соответствии с переданным действием.
     * Отвечает за логику добавления новых позиций, уменьшения их количества или полного удаления.
     * </summary>
     * @param itemId Уникальный идентификатор товара, состояние которого изменяется.
     * @param cartAction Тип выполняемого действия над корзиной (PLUS, MINUS, DELETE).
     **/
    public void updateItemCount(final long itemId, final CartActionEnumModel cartAction);

    /**
     * <summary>
     * Выполняет пакетный поиск количества добавленных в корзину единиц для списка идентификаторов товаров.
     * Используется для оптимизации отображения каталога и предотвращения проблемы N+1.
     * </summary>
     * @param itemIds Список идентификаторов интересующих товаров.
     * <return>
     * @return Карта (Map), где ключ — идентификатор товара, а значение — его количество в корзине.
     * </return>
     **/
    public Map<Long, Integer> findCountsForItems(final List<Long> itemIds);

    /**
     * <summary>
     * Возвращает количество единиц конкретного товара, находящегося в корзине.
     * </summary>
     * @param itemId Уникальный идентификатор проверяемого товара.
     * <return>
     * @return Количество товара в корзине, либо 0, если товар отсутствует.
     * </return>
     **/
    public int findCountForItem(final long itemId);

    // endregion
}