package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

        var savedItem = itemRepository.save(item).block();
        Assertions.assertNotNull(savedItem);

        var cartItem = new CartItemModel(savedItem, 1);

        cartItemRepository.save(cartItem).block();

        var result = cartItemRepository.findByItemId(savedItem.getId()).blockOptional();

        Assertions.assertTrue(result.isPresent());

        Assertions.assertEquals(savedItem.getId(), result.get().getItemId());

        Assertions.assertEquals(1, result.get().getQuantity());
    }

    /**
     * <summary>
     * Проверяет, что поиск по идентификатору товара возвращает пустой контейнер Mono, если такого товара нет в корзине.
     * </summary>
     **/
    @Test
    void findByItemIdShouldReturnEmptyOptionalWhenItemIsNotInCart()
    {
        var item = new ItemModel(" Xiaomi Mi Mix 4", "Смартфон", "/xiaomi.png", 60000L);

        var savedItem = itemRepository.save(item).block();

        Assertions.assertNotNull(savedItem);

        var result = cartItemRepository.findByItemId(savedItem.getId()).blockOptional();

        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * <summary>
     * Проверяет пакетную реактивную выборку элементов корзины по списку идентификаторов товаров (In-запрос).
     * </summary>
     **/
    @Test
    void findAllByItemIdInShouldReturnOnlyMatchingCartItems()
    {
        var item1 = new ItemModel("Товар 1", "Описание 1", "/img1.png", 1000L);

        var item2 = new ItemModel("Товар 2", "Описание 2", "/img2.png", 2000L);

        var item3 = new ItemModel("Товар 3", "Описание 3", "/img3.png", 3000L);

        var savedItems = itemRepository.saveAll(List.of(item1, item2, item3)).collectList().block();

        Assertions.assertNotNull(savedItems);

        var s1 = savedItems.get(0);

        var s2 = savedItems.get(1);

        var s3 = savedItems.get(2);

        var cartItem1 = new CartItemModel(s1, 5);

        var cartItem2 = new CartItemModel(s2, 2);

        cartItemRepository.saveAll(List.of(cartItem1, cartItem2)).collectList().block();

        var targetIds = List.of(s1.getId(), s2.getId(), s3.getId());

        var result = cartItemRepository.findAllByItemIdIn(targetIds).collectList().block();

        Assertions.assertNotNull(result);

        Assertions.assertEquals(2, result.size());

        var containsItem1 = result.stream().anyMatch(ci -> ci.getItemId().equals(s1.getId()));

        var containsItem2 = result.stream().anyMatch(ci -> ci.getItemId().equals(s2.getId()));

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

        var sA = itemRepository.save(itemA).block();

        var sB = itemRepository.save(itemB).block();

        var sC = itemRepository.save(itemC).block();

        Assertions.assertNotNull(sA);

        Assertions.assertNotNull(sB);

        Assertions.assertNotNull(sC);

        var cartItemC = new CartItemModel(sC, 1);

        var cartItemA = new CartItemModel(sA, 3);

        var cartItemB = new CartItemModel(sB, 2);

        cartItemRepository.saveAll(List.of(cartItemC, cartItemA, cartItemB)).collectList().block();

        var sortedCartItems = cartItemRepository.findAllByOrderByItemIdAsc().collectList().block();

        Assertions.assertNotNull(sortedCartItems);

        Assertions.assertEquals(3, sortedCartItems.size());

        Assertions.assertEquals(sA.getId(), sortedCartItems.get(0).getItemId());

        Assertions.assertEquals(sB.getId(), sortedCartItems.get(1).getItemId());

        Assertions.assertEquals(sC.getId(), sortedCartItems.get(2).getItemId());
    }

    // endregion
}