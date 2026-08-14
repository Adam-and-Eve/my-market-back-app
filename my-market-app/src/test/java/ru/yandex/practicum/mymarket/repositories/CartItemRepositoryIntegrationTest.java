package ru.yandex.practicum.mymarket.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.UserModel;

import java.util.List;
import java.util.UUID;

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

    @Autowired
    private UserRepository userRepository;

    // endregion

    // region Setup

    @BeforeEach
    void clear() {
        cartItemRepository.deleteAll().block();

        itemRepository.deleteAll().block();

        var systemIds = List.of(1L, 2L);

        userRepository.findAll()
                .filter(u -> !systemIds.contains(u.getId()))
                .flatMap(u -> userRepository.delete(u))
                .blockLast();
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет пакетную реактивную выборку элементов корзины пользователя по списку идентификаторов товаров с учетом изоляции пользователей.
     * </summary>
     **/
    @Test
    void findAllByUserIdAndItemIdInShouldReturnOnlyMatchingCartItemsForSpecificUser() {
        var uniqueSuffix = UUID.randomUUID().toString();

        var user1 = userRepository.save(new UserModel("user1_" + uniqueSuffix, true)).block();

        var user2 = userRepository.save(new UserModel("user2_" + uniqueSuffix, true)).block();

        Assertions.assertNotNull(user1);

        Assertions.assertNotNull(user2);

        var item1 = new ItemModel("Товар 1", "Описание 1", "/img1.png", 1000L);

        var item2 = new ItemModel("Товар 2", "Описание 2", "/img2.png", 2000L);

        var item3 = new ItemModel("Товар 3", "Описание 3", "/img3.png", 3000L);

        var savedItems = itemRepository.saveAll(List.of(item1, item2, item3)).collectList().block();

        Assertions.assertNotNull(savedItems);

        var s1 = savedItems.get(0);

        var s2 = savedItems.get(1);

        var s3 = savedItems.get(2);

        var cartItem1User1 = new CartItemModel(user1.getId(), s1.getId(), 5);

        var cartItem2User1 = new CartItemModel(user1.getId(), s2.getId(), 2);

        var cartItem1User2 = new CartItemModel(user2.getId(), s1.getId(), 10);

        cartItemRepository.saveAll(List.of(cartItem1User1, cartItem2User1, cartItem1User2)).collectList().block();

        var targetIds = List.of(s1.getId(), s2.getId(), s3.getId());

        var result = cartItemRepository.findAllByUserIdAndItemIdIn(user1.getId(), targetIds).collectList().block();

        Assertions.assertNotNull(result);

        Assertions.assertEquals(2, result.size());

        var allBelongToUser1 = result.stream().allMatch(ci -> ci.getUserId().equals(user1.getId()));

        Assertions.assertTrue(allBelongToUser1);

        var containsItem1 = result.stream().anyMatch(ci -> ci.getItemId().equals(s1.getId()));

        var containsItem2 = result.stream().anyMatch(ci -> ci.getItemId().equals(s2.getId()));

        Assertions.assertTrue(containsItem1);

        Assertions.assertTrue(containsItem2);
    }

    /**
     * <summary>
     * Проверяет, что выборка всех элементов корзины конкретного пользователя строго отсортирована по возрастанию ID товара и не содержит позиций других пользователей.
     * </summary>
     **/
    @Test
    void findAllByUserIdOrderByItemIdAscShouldReturnCartItemsSortedCorrectlyForSpecificUser() {
        var uniqueSuffix = UUID.randomUUID().toString();

        var user1 = userRepository.save(new UserModel("user1_" + uniqueSuffix, true)).block();

        var user2 = userRepository.save(new UserModel("user2_" + uniqueSuffix, true)).block();

        Assertions.assertNotNull(user1);

        Assertions.assertNotNull(user2);

        var itemA = new ItemModel("Клавиатура A", "Описание A", "/imgA.png", 1000L);

        var itemB = new ItemModel("Клавиатура B", "Описание B", "/imgB.png", 2000L);

        var itemC = new ItemModel("Клавиатура C", "Описание C", "/imgC.png", 3000L);

        var sA = itemRepository.save(itemA).block();

        var sB = itemRepository.save(itemB).block();

        var sC = itemRepository.save(itemC).block();

        Assertions.assertNotNull(sA);

        Assertions.assertNotNull(sB);

        Assertions.assertNotNull(sC);

        var cartItemCUser1 = new CartItemModel(user1.getId(), sC.getId(), 1);

        var cartItemAUser1 = new CartItemModel(user1.getId(), sA.getId(), 3);

        var cartItemBUser1 = new CartItemModel(user1.getId(), sB.getId(), 2);

        var cartItemBUser2 = new CartItemModel(user2.getId(), sB.getId(), 99);

        cartItemRepository.saveAll(List.of(cartItemCUser1, cartItemAUser1, cartItemBUser1, cartItemBUser2)).collectList().block();

        var sortedCartItems = cartItemRepository.findAllByUserIdOrderByItemIdAsc(user1.getId()).collectList().block();

        Assertions.assertNotNull(sortedCartItems);

        Assertions.assertEquals(3, sortedCartItems.size());

        var allBelongToUser1 = sortedCartItems.stream().allMatch(ci -> ci.getUserId().equals(user1.getId()));

        Assertions.assertTrue(allBelongToUser1);

        Assertions.assertEquals(sA.getId(), sortedCartItems.get(0).getItemId());

        Assertions.assertEquals(sB.getId(), sortedCartItems.get(1).getItemId());

        Assertions.assertEquals(sC.getId(), sortedCartItems.get(2).getItemId());
    }

    // endregion
}