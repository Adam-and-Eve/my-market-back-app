package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в CartItemRepository.
 * </summary>
 **/
public class CartItemRepositoryIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет успешный поиск элемента корзины по идентификатору существующего товара.
     * </summary>
     **/
    @Test
    void findByItemIdShouldReturnCartItemWhenItemExistsInCart()
    {
        var item = new ItemModel("Novation Launchkey 88", "MIDI-контроллер", "/novation.png", 45000L);

        itemRepository.save(item);

        var cartItem = new CartItemModel(item, 1);

        cartItemRepository.save(cartItem);

        var result = cartItemRepository.findByItemId(item.getId());

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(item.getId(), result.get().getItem().getId());

        Assertions.assertEquals(1, result.get().getQuantity());
    }

    /**
     * <summary>
     * Проверяет, что поиск по идентификатору товара возвращает Optional.empty(), если такого товара нет в корзине.
     * </summary>
     **/
    @Test
    void findByItemIdShouldReturnEmptyOptionalWhenItemIsNotInCart()
    {
        var item = new ItemModel(" Xiaomi Mi Mix 4", "Смартфон", "/xiaomi.png", 60000L);

        itemRepository.save(item);

        var result = cartItemRepository.findByItemId(item.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * <summary>
     * Проверяет пакетную выборку элементов корзины по списку идентификаторов товаров (In-запрос).
     * </summary>
     **/
    @Test
    void findAllByItemIdInShouldReturnOnlyMatchingCartItems()
    {
        var item1 = new ItemModel("Товар 1", "Описание 1", "/img1.png", 1000L);

        var item2 = new ItemModel("Товар 2", "Описание 2", "/img2.png", 2000L);

        var item3 = new ItemModel("Товар 3", "Описание 3", "/img3.png", 3000L);

        itemRepository.saveAll(List.of(item1, item2, item3));

        var cartItem1 = new CartItemModel(item1, 5);

        var cartItem2 = new CartItemModel(item2, 2);

        cartItemRepository.saveAll(List.of(cartItem1, cartItem2));

        var targetIds = List.of(item1.getId(), item2.getId(), item3.getId());

        var result = cartItemRepository.findAllByItemIdIn(targetIds);

        Assertions.assertEquals(2, result.size());

        var containsItem1 = result.stream().anyMatch(ci -> ci.getItem().getId().equals(item1.getId()));

        var containsItem2 = result.stream().anyMatch(ci -> ci.getItem().getId().equals(item2.getId()));

        Assertions.assertTrue(containsItem1);

        Assertions.assertTrue(containsItem2);
    }

    /**
     * <summary>
     * Проверяет, что выборка всех элементов корзины строго отсортирована по возрастанию ID товара.
     * </summary>
     **/
    @Test
    void findAllByOrderByItemIdAscShouldReturnCartItemsSortedCorrectly()
    {
        var itemA = new ItemModel("Клавиатура A", "Описание A", "/imgA.png", 1000L);

        var itemB = new ItemModel("Клавиатура B", "Описание B", "/imgB.png", 2000L);

        var itemC = new ItemModel("Клавиатура C", "Описание C", "/imgC.png", 3000L);


        itemRepository.save(itemA);

        itemRepository.save(itemB);

        itemRepository.save(itemC);

        var cartItemC = new CartItemModel(itemC, 1);

        var cartItemA = new CartItemModel(itemA, 3);

        var cartItemB = new CartItemModel(itemB, 2);

        cartItemRepository.save(cartItemC);

        cartItemRepository.save(cartItemA);

        cartItemRepository.save(cartItemB);

        var sortedCartItems = cartItemRepository.findAllByOrderByItemIdAsc();

        Assertions.assertEquals(3, sortedCartItems.size());

        Assertions.assertEquals(itemA.getId(), sortedCartItems.get(0).getItem().getId());

        Assertions.assertEquals(itemB.getId(), sortedCartItems.get(1).getItem().getId());

        Assertions.assertEquals(itemC.getId(), sortedCartItems.get(2).getItem().getId());
    }

    // endregion
}