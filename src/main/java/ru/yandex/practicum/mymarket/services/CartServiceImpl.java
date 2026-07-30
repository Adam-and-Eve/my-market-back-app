package ru.yandex.practicum.mymarket.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // endregion

    // region Constructors

    public CartServiceImpl(
            final CartItemRepository cartItemRepository,
            final ItemRepository itemRepository) {
        this.cartItemRepository = cartItemRepository;
        this.itemRepository = itemRepository;
    }

    // endregion

    // region Methods

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
    public void updateItemCount(final long itemId, final CartActionEnumModel cartAction) {
        switch (cartAction) {
            case PLUS -> addItem(itemId);
            case MINUS -> removeOneItem(itemId);
            case DELETE -> cartItemRepository.findByItemId(itemId).ifPresent(cartItemRepository::delete);
        }
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
    public Map<Long, Integer> findCountsForItems(final List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }

        return cartItemRepository.findAllByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.toMap(
                        cartItem -> cartItem.getItem().getId(),
                        CartItemModel::getQuantity));
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
    public int findCountForItem(final long itemId) {
        return cartItemRepository.findByItemId(itemId)
                .map(CartItemModel::getQuantity)
                .orElse(0);
    }

    /**
     * <summary>
     * Вспомогательный метод для добавления товара в корзину или увеличения его текущего количества.
     * Если позиция отсутствует в корзине, создается новый элемент со стартовым количеством 0.
     * </summary>
     * @param itemId Уникальный идентификатор добавляемого товара.
     **/
    private void addItem(final long itemId) {
        var cartItem = cartItemRepository
                .findByItemId(itemId)
                .orElseGet(() -> new CartItemModel(findModelById(itemId), 0));

        cartItem.increase();

        cartItemRepository.save(cartItem);
    }

    /**
     * <summary>
     * Вспомогательный метод для уменьшения количества товара в корзине на единицу.
     * Если после уменьшения количество становится равным нулю, элемент полностью удаляется из репозитория.
     * </summary>
     * @param itemId Уникальный идентификатор изменяемого товара.
     **/
    private void removeOneItem(final long itemId) {
        cartItemRepository
                .findByItemId(itemId)
                .ifPresent(cartItem -> {
                    cartItem.decrease();

                    if (cartItem.getQuantity() == 0) {
                        cartItemRepository.delete(cartItem);
                    }
                });
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
    private ItemModel findModelById(final long itemId) {
        return itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found."));
    }

    // endregion
}