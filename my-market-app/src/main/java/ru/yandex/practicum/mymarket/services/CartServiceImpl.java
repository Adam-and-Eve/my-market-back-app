package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemCacheService;
import ru.yandex.practicum.mymarket.interfaces.PaymentClientService;
import ru.yandex.practicum.mymarket.interfaces.UserService;
import ru.yandex.practicum.mymarket.mappers.CartMapper;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.UserModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

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

    private final CartItemRepository cartItemRepository;

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    private final CartMapper cartMapper;

    private final PaymentClientService paymentClientService;

    private final ItemCacheService  itemCacheService;

    private final UserService userService;

    // endregion

    // region Constructors

    public CartServiceImpl(
            final CartItemRepository cartItemRepository,
            final ItemRepository itemRepository,
            final ItemMapper itemMapper,
            final CartMapper cartMapper,
            final PaymentClientService paymentClientService,
            final ItemCacheService itemCacheService,
            final UserService userService) {

        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.cartMapper = cartMapper;
        this.paymentClientService = paymentClientService;
        this.itemCacheService = itemCacheService;
        this.userService = userService;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Сборка и расчет агрегированных данных корзины для формирования полноценной страницы в UI.
     * </summary>
     * @param username Имя пользователя.
     * <return>
     * @return Модель представления страницы корзины CartPageViewModel с подсчитанной итоговой стоимостью.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<CartPageViewModel> findCart(final String username) {
        return findUserIdForRead(username)
                .flatMap(userId -> cartItemRepository.findAllByUserIdOrderByItemIdAsc(userId)
                        .flatMapSequential(cartItem -> itemCacheService.findById(
                                        cartItem.getItemId(),
                                        itemRepository.findById(cartItem.getItemId())
                                )
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")))
                                .map(item -> itemMapper.toViewModel(cartItem, item)))
                        .collectList()
                        .flatMap(items -> items.isEmpty()
                                ? Mono.just(cartMapper.toViewModel(items))
                                : paymentClientService.getBalance().map(payment -> cartMapper.toViewModel(items, payment))
                        ))
                .switchIfEmpty(Mono.fromSupplier(() -> cartMapper.toViewModel(List.of())));
    }

    /**
     * <summary>
     * Маршрутизирует запрос на изменение количества товара в корзине в зависимости от переданного действия.
     * </summary>
     * @param username Имя пользователя.
     * @param itemId Уникальный идентификатор целевого товара.
     * @param cartAction Тип операции (PLUS, MINUS, DELETE).
     **/
    @Transactional
    @Override
    public Mono<Void> updateItemCount(final String username, final long itemId, final CartActionEnumModel cartAction) {
        return findUserIdForWrite(username)
                .flatMap(userId -> switch (cartAction) {
                    case PLUS -> addItem(userId, itemId);
                    case MINUS -> removeOneItem(userId, itemId);
                    case DELETE -> deleteItem(userId, itemId);
                });
    }

    /**
     * <summary>
     * Выполняет пакетный поиск количества добавленных в корзину единиц для списка идентификаторов товаров.
     * </summary>
     * @param username Имя пользователя.
     * @param itemIds Список идентификаторов интересующих товаров.
     * <return>
     * @return Карта (Map), где ключ — идентификатор товара, а значение — его количество в корзине.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<Map<Long, Integer>> findCountsForItems(final String username, final List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return findUserIdForRead(username)
                .flatMap(userId -> cartItemRepository.findAllByUserIdAndItemIdIn(userId, itemIds)
                        .collectMap(
                                CartItemModel::getItemId,
                                CartItemModel::getQuantity
                        ))
                .defaultIfEmpty(Map.of());
    }

    /**
     * <summary>
     * Возвращает количество единиц конкретного товара, находящегося в корзине.
     * </summary>
     * @param username Имя пользователя.
     * @param itemId Уникальный идентификатор проверяемого товара.
     * <return>
     * @return Количество товара в корзине, либо 0, если товар в корзине отсутствует.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<Integer> findCountForItem(final String username, final long itemId) {
        return findUserIdForRead(username)
                .flatMap(userId ->
                        cartItemRepository.findByUserIdAndItemId(userId, itemId)
                                .map(CartItemModel::getQuantity)
                                .defaultIfEmpty(0));
    }

    /**
     * <summary>
     * Вспомогательный метод для добавления товара в корзину конкретного пользователя или увеличения его количества.
     * </summary>
     **/
    private Mono<Void> addItem(final long userId, final long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
                .switchIfEmpty(Mono.defer(() -> itemRepository.findById(itemId)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Item not found")))
                        .map(item -> new CartItemModel(userId, item.getId(), 0))))
                .flatMap(cartItem -> {
                    cartItem.increase();

                    return cartItemRepository.save(cartItem).then();
                });
    }

    /**
     * <summary>
     * Вспомогательный метод для уменьшения количества товара в корзине конкретного пользователя.
     * </summary>
     **/
    private Mono<Void> removeOneItem(final long userId, final long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
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
     * Вспомогательный метод для полного удаления позиции из корзины конкретного пользователя.
     * </summary>
     **/
    private Mono<Void> deleteItem(final long userId, final long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId)
                .flatMap(cartItemRepository::delete);
    }

    /**
     * <summary>
     * Ищет пользователя для операций чтения. Не создает новую запись в БД.
     * Если пользователь не найден, возвращает Mono.empty().
     * </summary>
     **/
    private Mono<Long> findUserIdForRead(String username) {
        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));
        }

        return userService.findByUsername(username)
                .flatMap(user -> user.getEnabled()
                        ? Mono.just(user.getId())
                        : Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is disabled")));
    }

    /**
     * <summary>
     * Ищет пользователя для операций изменения корзины.
     * Если пользователя нет, создает новую активную учетную запись.
     * </summary>
     **/
    private Mono<Long> findUserIdForWrite(String username) {
        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));
        }

        return userService.findOrCreateByUsername(username)
                .flatMap(user -> user.getEnabled()
                        ? Mono.just(user.getId())
                        : Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "User is disabled")));
    }

    // endregion
}