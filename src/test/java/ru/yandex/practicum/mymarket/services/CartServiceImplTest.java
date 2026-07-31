package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики CartServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    // region Fields

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет сборку пустой корзины, когда в репозитории нет записей.
     * </summary>
     **/
    @Test
    void findCartShouldReturnEmptyCartWhenNoItemsExist()
    {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Collections.emptyList());

        var cartPage = cartService.findCart();

        Assertions.assertNotNull(cartPage);

        Assertions.assertTrue(cartPage.items().isEmpty());

        Assertions.assertEquals(0L, cartPage.total());
    }

    /**
     * <summary>
     * Проверяет корректное добавление нового товара в корзину (когда его там еще не было).
     * </summary>
     **/
    @Test
    void updateItemCountShouldCreateNewCartItemWhenActionIsPlusAndItemNotPresent()
    {
        var itemId = 1L;

        var itemModel = new ItemModel("Товар", "Описание", "/img.png", 100L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Optional.empty());

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemModel));

        cartService.updateItemCount(itemId, CartActionEnumModel.PLUS);

        Mockito.verify(cartItemRepository, Mockito.times(1)).save(Mockito.any(CartItemModel.class));
    }

    /**
     * <summary>
     * Проверяет, что при добавлении несуществующего в каталоге товара выбрасывается ResponseStatusException 404.
     * </summary>
     **/
    @Test
    void updateItemCountShouldThrowNotFoundWhenItemDoesNotExistInCatalog()
    {
        var itemId = 999L;

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Optional.empty());

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(ResponseStatusException.class, () ->
        {
            cartService.updateItemCount(itemId, CartActionEnumModel.PLUS);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    /**
     * <summary>
     * Проверяет уменьшение количества товара в корзине и его полное удаление, если количество достигло нуля.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteCartItemWhenActionIsMinusAndQuantityDropsToZero()
    {
        var itemId = 1L;

        var itemModel = new ItemModel("Товар", "Описание", "/img.png", 100L);

        var cartItem = new CartItemModel(itemModel, 1);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Optional.of(cartItem));

        cartService.updateItemCount(itemId, CartActionEnumModel.MINUS);

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);
    }

    /**
     * <summary>
     * Проверяет безусловное удаление позиции из корзины при действии DELETE.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteImmediatelyWhenActionIsDelete()
    {
        var itemId = 1L;

        var itemModel = new ItemModel("Товар", "Описание", "/img.png", 100L);

        var cartItem = new CartItemModel(itemModel, 5);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Optional.of(cartItem));

        cartService.updateItemCount(itemId, CartActionEnumModel.DELETE);

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);
    }

    /**
     * <summary>
     * Проверяет пакетное получение мапы количеств товаров.
     * Тест защищен от NPE благодаря ручной установке ID через ReflectionTestUtils.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnCorrectMap()
    {
        var itemId1 = 10L;

        var itemId2 = 20L;

        var itemIds = List.of(itemId1, itemId2);

        var item1 = new ItemModel("Товар 1", "Описание 1", "/img1.png", 150L);

        ReflectionTestUtils.setField(item1, "id", itemId1);

        var item2 = new ItemModel("Товар 2", "Описание 2", "/img2.png", 250L);

        ReflectionTestUtils.setField(item2, "id", itemId2);

        var cartItem1 = new CartItemModel(item1, 2);

        var cartItem2 = new CartItemModel(item2, 5);

        Mockito.when(cartItemRepository.findAllByItemIdIn(itemIds)).thenReturn(List.of(cartItem1, cartItem2));

        var resultMaps = cartService.findCountsForItems(itemIds);

        Assertions.assertNotNull(resultMaps);

        Assertions.assertEquals(2, resultMaps.size());

        Assertions.assertEquals(2, resultMaps.get(itemId1));

        Assertions.assertEquals(5, resultMaps.get(itemId2));
    }

    /**
     * <summary>
     * Проверяет, что findCountsForItems возвращает пустую мапу, если передан пустой список ID.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnEmptyMapWhenParamIsEmpty()
    {
        var result = cartService.findCountsForItems(Collections.emptyList());

        Assertions.assertNotNull(result);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verifyNoInteractions(cartItemRepository);
    }

    // endregion
}