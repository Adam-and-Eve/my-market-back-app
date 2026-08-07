package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Mono;
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
     * @param username Имя пользователя, для которого запрашиваются данные корзины.
     * <return>
     * @return Реактивный контейнер Mono с моделью представления страницы корзины CartPageViewModel.
     * </return>
     **/
    public Mono<CartPageViewModel> findCart(final String username);

    /**
     * <summary>
     * Обновляет количество указанного товара в корзине в соответствии с переданным действием.
     * Отвечает за логику добавления новых позиций, уменьшения их количества или полного удаления.
     * </summary>
     * @param username Имя пользователя, для которого выполняется изменение состава корзины.
     * @param itemId Уникальный идентификатор товара, состояние которого изменяется.
     * @param cartAction Тип выполняемого действия над корзиной (PLUS, MINUS, DELETE).
     * <return>
     * @return Пустой реактивный контейнер Mono, сигнализирующий о завершении операции.
     * </return>
     **/
    public Mono<Void> updateItemCount(final String username, final long itemId, final CartActionEnumModel cartAction);

    /**
     * <summary>
     * Выполняет пакетный поиск количества добавленных в корзину единиц для списка идентификаторов товаров.
     * Используется для оптимизации отображения каталога и предотвращения проблемы N+1.
     * </summary>
     * @param username Имя пользователя, с чьей корзиной производится сопоставление.
     * @param itemIds Список идентификаторов интересующих товаров.
     * <return>
     * @return Реактивный контейнер Mono с картой (Map), где ключ — идентификатор товара, а значение — его количество в корзине.
     * </return>
     **/
    public Mono<Map<Long, Integer>> findCountsForItems(final String username, final List<Long> itemIds);

    /**
     * <summary>
     * Возвращает количество единиц конкретного товара, находящегося в корзине.
     * </summary>
     * @param username Имя пользователя для проверки состава корзины.
     * @param itemId Уникальный идентификатор проверяемого товара.
     * <return>
     * @return Реактивный контейнер Mono с количеством товара в корзине (или 0, если товар отсутствует).
     * </return>
     **/
    public Mono<Integer> findCountForItem(final String username, final long itemId);

    // endregion
}