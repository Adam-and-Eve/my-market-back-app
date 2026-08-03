package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.mappers.CartMapper;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;

import java.util.List;
import java.util.Map;

/**
 * <summary>
 * Сервис управления корзиной покупателя.
 * </summary>
 **/
@Service
public class CartServiceImpl implements CartService {

    // region Fields

    /**
     * Репозиторий для выполнения операций с сущностями элементов корзины.
     **/
    private final CartItemRepository cartItemRepository;

    /**
     * Репозиторий для проверки существования и получения данных товаров из каталога.
     **/
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final CartMapper cartMapper;

    // endregion

    // region Constructors

    public CartServiceImpl(
            final CartItemRepository cartItemRepository,
            final ItemRepository itemRepository,
            final ItemMapper itemMapper,
            final CartMapper cartMapper) {

        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.cartMapper = cartMapper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Сборка и расчет агрегированных данных корзины для формирования полноценной страницы в UI.
     * </summary>
     * <return>
     * @return Модель представления страницы корзины CartPageViewModel с подсчитанной итоговой стоимостью.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<CartPageViewModel> findCart() {
        return cartItemRepository.findAllByOrderByItemIdAsc()
                .flatMap(cartItem -> findModelById(cartItem.getItemId())
                        .map(item -> itemMapper.toViewModel(cartItem, item)))
                .collectList()
                .map(cartMapper::toViewModel);
    }

    /**
     * <summary>
     * Маршрутизирует запрос на изменение количества товара в корзине в зависимости от переданного действия.
     * Выполняется в контексте транзакции базы данных.
     * </summary>
     * @param itemId Уникальный идентификатор целевого товара.
     * @param cartAction Тип операции (PLUS, MINUS, DELETE).
     **/
    @Transactional
    @Override
    public Mono<Void> updateItemCount(final long itemId, final CartActionEnumModel cartAction) {
        return switch (cartAction) {
            case PLUS -> addItem(itemId);
            case MINUS -> removeOneItem(itemId);
            case DELETE -> deleteItem(itemId);
        };
    }

    /**
     * <summary>
     * Выполняет пакетный поиск количества добавленных в корзину единиц для списка идентификаторов товаров.
     * </summary>
     * @param itemIds Список идентификаторов интересующих товаров.
     * <return>
     * @return Карта (Map), где ключ — идентификатор товара, а значение — его количество в корзине.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<Map<Long, Integer>> findCountsForItems(final List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return cartItemRepository.findAllByItemIdIn(itemIds)
                .collectMap(
                        CartItemModel::getItemId,
                        CartItemModel::getQuantity
                );
    }

    /**
     * <summary>
     * Возвращает количество единиц конкретного товара, находящегося в корзине.
     * </summary>
     * @param itemId Уникальный идентификатор проверяемого товара.
     * <return>
     * @return Количество товара в корзине, либо 0, если товар в корзине отсутствует.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<Integer> findCountForItem(final long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .map(CartItemModel::getQuantity)
                .defaultIfEmpty(0);
    }

    /**
     * <summary>
     * Вспомогательный метод для добавления товара в корзину или увеличения его текущего количества.
     * Если позиция отсутствует в корзине, создается новый элемент со стартовым количеством 0.
     * </summary>
     * @param itemId Уникальный идентификатор добавляемого товара.
     **/
    private Mono<Void> addItem(final long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .switchIfEmpty(findModelById(itemId).map(item -> new CartItemModel(item.getId(), 0)))
                .flatMap(cartItem -> {
                    cartItem.increase();

                    return cartItemRepository.save(cartItem).then();
                });
    }

    /**
     * <summary>
     * Вспомогательный метод для уменьшения количества товара в корзине на единицу.
     * Если после уменьшения количество становится равным нулю, элемент полностью удаляется из репозитория.
     * </summary>
     * @param itemId Уникальный идентификатор изменяемого товара.
     **/
    private Mono<Void> removeOneItem(final long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(cartItem -> {
                    cartItem.decrease();

                    if (cartItem.getQuantity() == 0) {
                        return cartItemRepository.delete(cartItem);
                    }

                    return cartItemRepository.save(cartItem).then();
                });
    }

    /**
     * <summary>
     * Вспомогательный метод для полного удаления товарной позиции из корзины по её идентификатору.
     * </summary>
     * @param itemId Уникальный идентификатор удаляемого товара.
     * <return>
     * @return Реактивный контейнер Mono<Void>, сигнализирующий о завершении операции удаления.
     * </return>
     **/
    private Mono<Void> deleteItem(final long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .flatMap(cartItemRepository::delete);
    }

    /**
     * <summary>
     * Вспомогательный метод для поиска доменной модели товара в каталоге с валидацией его существования.
     * </summary>
     * @param itemId Уникальный идентификатор искомого товара.
     * <return>
     * @return Доменная модель найденного товара ItemModel.
     * </return>
     * @throws ResponseStatusException Если товар с указанным идентификатором отсутствует в базе данных (HTTP 404).
     **/
    private Mono<ItemModel> findModelById(final long itemId) {
        return itemRepository
                .findById(itemId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")));
    }

    // endregion
}