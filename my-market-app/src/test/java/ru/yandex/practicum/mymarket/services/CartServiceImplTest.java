package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
import ru.yandex.practicum.mymarket.viewmodels.PaymentAvailabilityViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @Mock
    private CartMapper cartMapper;

    @Mock
    private PaymentClientService paymentClientService;

    @Mock
    private ItemCacheService itemCacheService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CartServiceImpl cartService;

    // endregion


    // region Tests for Authentication & Authorization

    /**
     * <summary>
     * Проверяет выбрасывание ошибки 401 UNAUTHORIZED при передаче невалидного (null или пустого) имени пользователя.
     * </summary>
     **/
    @Test
    void shouldThrowUnauthorizedWhenUsernameIsBlankOrNull() {
        StepVerifier.create(cartService.findCart(""))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.UNAUTHORIZED)
                )
                .verify();

        Mockito.verifyNoInteractions(userService, cartItemRepository);
    }

    /**
     * <summary>
     * Проверяет выбрасывание ошибки 403 FORBIDDEN, если учетная запись пользователя заблокирована (enabled = false).
     * </summary>
     **/
    @Test
    void shouldThrowForbiddenWhenUserIsDisabled() {
        var username = "disabled_user";

        var disabledUser = new UserModel(username, false);

        ReflectionTestUtils.setField(disabledUser, "id", 1L);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(disabledUser));

        StepVerifier.create(cartService.findCart(username))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.FORBIDDEN)
                )
                .verify();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verifyNoInteractions(cartItemRepository);
    }

    // endregion

    // region Tests for findCart

    /**
     * <summary>
     * Проверяет сборку пустой корзины пользователя, когда в БД нет записей.
     * При этом запрос баланса к платежному сервису выполняться не должен.
     * </summary>
     **/
    @Test
    void findCartShouldReturnEmptyCartWhenNoItemsExist() {
        var username = "user";

        var userId = 1L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(userId)).thenReturn(Flux.empty());

        Mockito.when(cartMapper.toViewModel(Mockito.anyList()))
                .thenReturn(new CartPageViewModel(Collections.emptyList(), 0L));

        StepVerifier.create(cartService.findCart(username))
                .expectNextMatches(cartPage ->
                        cartPage.items().isEmpty() && cartPage.total() == 0L
                )
                .verifyComplete();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verify(cartItemRepository, Mockito.times(1)).findAllByUserIdOrderByItemIdAsc(userId);

        Mockito.verifyNoInteractions(paymentClientService);
    }

    /**
     * <summary>
     * Проверяет сборку наполненной корзины пользователя с получением данных через ItemCacheService,
     * а также вызов PaymentClientService для проверки доступности оплаты.
     * </summary>
     **/
    @Test
    void findCartShouldReturnPopulatedCartWhenItemsExist() {
        var username = "user";

        var userId = 1L;

        var itemId = 10L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem = new CartItemModel(userId, itemId, 2);

        var itemModel = new ItemModel("Novation Launchkey 88", "MIDI-контроллер", "/novation.png", 45000L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        var itemViewModel = new ItemViewModel(itemId, "Novation Launchkey 88", "MIDI-контроллер", "/novation.png", 45000L, 2);

        var expectedPage = new CartPageViewModel(List.of(itemViewModel), 90000L);

        var paymentAvailability = Mockito.mock(PaymentAvailabilityViewModel.class);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findAllByUserIdOrderByItemIdAsc(userId)).thenReturn(Flux.just(cartItem));

        Mockito.when(itemCacheService.findById(Mockito.eq(itemId), Mockito.any())).thenReturn(Mono.just(itemModel));

        Mockito.when(itemMapper.toViewModel(cartItem, itemModel)).thenReturn(itemViewModel);

        Mockito.when(paymentClientService.getBalance()).thenReturn(Mono.just(paymentAvailability));

        Mockito.when(cartMapper.toViewModel(List.of(itemViewModel), paymentAvailability)).thenReturn(expectedPage);

        StepVerifier.create(cartService.findCart(username))
                .expectNextMatches(cartPage ->
                        cartPage.items().size() == 1 &&
                                cartPage.total() == 90000L &&
                                cartPage.items().getFirst().title().equals("Novation Launchkey 88")
                )
                .verifyComplete();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verify(paymentClientService, Mockito.times(1)).getBalance();
    }

    // endregion

    // region Tests for updateItemCount (PLUS)

    /**
     * <summary>
     * Проверяет добавление нового товара в корзину конкретного пользователя (когда его там еще не было) со стартовым количеством 1.
     * </summary>
     **/
    @Test
    void updateItemCountShouldCreateNewCartItemWhenActionIsPlusAndItemNotPresent() {
        var username = "user";

        var userId = 1L;

        var itemId = 10L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var itemModel = new ItemModel(
                "Товар",
                "Описание",
                "/img.png",
                100L
        );

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        Mockito.when(userService.findOrCreateByUsername(username))
                .thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Mono.empty());

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Mono.just(itemModel));

        Mockito.when(cartItemRepository.save(Mockito.any()))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0)));

        StepVerifier.create(
                        cartService.updateItemCount(username, itemId, CartActionEnumModel.PLUS)
                )
                .verifyComplete();

        Mockito.verify(cartItemRepository)
                .save(Mockito.argThat(savedItem ->
                        savedItem.getUserId() == userId &&
                                savedItem.getItemId() == itemId &&
                                savedItem.getQuantity() == 1
                ));
    }

    /**
     * <summary>
     * Проверяет увеличение количества товара, который уже присутствует в корзине конкретного пользователя.
     * </summary>
     **/
    @Test
    void updateItemCountShouldIncreaseQuantityWhenActionIsPlusAndItemAlreadyPresent() {
        var username = "user";

        var userId = 1L;

        var itemId = 10L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var existingCartItem = new CartItemModel(userId, itemId, 2);

        Mockito.when(userService.findOrCreateByUsername(username))
                .thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Mono.just(existingCartItem));

        Mockito.when(cartItemRepository.save(Mockito.any(CartItemModel.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0)));

        StepVerifier.create(
                        cartService.updateItemCount(username, itemId, CartActionEnumModel.PLUS)
                )
                .verifyComplete();

        Mockito.verify(cartItemRepository)
                .save(Mockito.argThat(savedItem ->
                        savedItem.getUserId() == userId &&
                                savedItem.getItemId() == itemId &&
                                savedItem.getQuantity() == 3
                ));

        Mockito.verifyNoInteractions(itemRepository);
    }

    /**
     * <summary>
     * Проверяет, что при добавлении несуществующего в каталоге товара выбрасывается ResponseStatusException 404.
     * </summary>
     **/
    @Test
    void updateItemCountShouldThrowNotFoundWhenItemDoesNotExistInCatalog() {
        var username = "user";

        var userId = 1L;

        var itemId = 999L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        Mockito.when(userService.findOrCreateByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Mono.empty());

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(username, itemId, CartActionEnumModel.PLUS))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND)
                )
                .verify();
    }

    // endregion

    // region Tests for updateItemCount (MINUS & DELETE)

    /**
     * <summary>
     * Проверяет уменьшение количества товара в корзине пользователя на 1, если итоговое количество остается больше 0.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDecreaseQuantityWhenActionIsMinusAndQuantityStaysGreaterThanZero() {
        var username = "user";

        var userId = 1L;

        var itemId = 10L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem = new CartItemModel(userId, itemId, 3);

        Mockito.when(userService.findOrCreateByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(cartItem));

        Mockito.when(cartItemRepository.save(Mockito.any(CartItemModel.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.updateItemCount(username, itemId, CartActionEnumModel.MINUS))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).save(Mockito.argThat(savedItem ->
                savedItem.getQuantity() == 2
        ));

        Mockito.verify(cartItemRepository, Mockito.never()).delete(Mockito.any());
    }

    /**
     * <summary>
     * Проверяет полное удаление товара из корзины пользователя, если после уменьшения его количество достигло нуля.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteCartItemWhenActionIsMinusAndQuantityDropToZero() {
        var username = "user";
        var userId = 1L;
        var itemId = 10L;

        var user = new UserModel(username, true);
        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem = new CartItemModel(userId, itemId, 1);

        Mockito.when(userService.findOrCreateByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(cartItem));

        Mockito.when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(username, itemId, CartActionEnumModel.MINUS))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);

        Mockito.verify(cartItemRepository, Mockito.never()).save(Mockito.any());
    }

    /**
     * <summary>
     * Проверяет безусловное удаление позиции из корзины пользователя при действии DELETE.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteImmediatelyWhenActionIsDelete() {
        var username = "user";

        var userId = 1L;

        var itemId = 10L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem = new CartItemModel(userId, itemId, 5);

        Mockito.when(userService.findOrCreateByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(cartItem));

        Mockito.when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(username, itemId, CartActionEnumModel.DELETE))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);
    }

    // endregion

    // region Tests for findCountsForItems & findCountForItem

    /**
     * <summary>
     * Проверяет пакетное получение мапы количеств товаров из реактивного потока для конкретного пользователя.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnCorrectMap() {
        var username = "user";

        var userId = 1L;

        var itemId1 = 10L;

        var itemId2 = 20L;

        var itemIds = List.of(itemId1, itemId2);

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem1 = new CartItemModel(userId, itemId1, 2);

        var cartItem2 = new CartItemModel(userId, itemId2, 5);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findAllByUserIdAndItemIdIn(userId, itemIds)).thenReturn(Flux.just(cartItem1, cartItem2));

        StepVerifier.create(cartService.findCountsForItems(username, itemIds))
                .expectNextMatches(resultMap ->
                        resultMap.size() == 2 && resultMap.get(itemId1) == 2 && resultMap.get(itemId2) == 5
                )
                .verifyComplete();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verify(cartItemRepository, Mockito.times(1)).findAllByUserIdAndItemIdIn(userId, itemIds);
    }

    /**
     * <summary>
     * Проверяет, что findCountsForItems возвращает пустую мапу при переданном пустом списке ID без обращения к userService и БД.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnEmptyMapWhenParamIsEmpty() {
        StepVerifier.create(cartService.findCountsForItems("user", Collections.emptyList()))
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();

        Mockito.verifyNoInteractions(userService, cartItemRepository);
    }

    /**
     * <summary>
     * Проверяет получение количества для конкретного товара, если он присутствует в корзине пользователя.
     * </summary>
     **/
    @Test
    void findCountForItemShouldReturnQuantityWhenItemExists() {
        var username = "user";

        var userId = 1L;

        var itemId = 5L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        var cartItem = new CartItemModel(userId, itemId, 4);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartService.findCountForItem(username, itemId))
                .expectNext(4)
                .verifyComplete();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verify(cartItemRepository, Mockito.times(1)).findByUserIdAndItemId(userId, itemId);
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает 0, если запрашиваемый товар отсутствует в корзине пользователя.
     * </summary>
     **/
    @Test
    void findCountForItemShouldReturnZeroWhenItemDoesNotExist() {
        var username = "user";

        var userId = 1L;

        var itemId = 5L;

        var user = new UserModel(username, true);

        ReflectionTestUtils.setField(user, "id", userId);

        Mockito.when(userService.findByUsername(username)).thenReturn(Mono.just(user));

        Mockito.when(cartItemRepository.findByUserIdAndItemId(userId, itemId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.findCountForItem(username, itemId))
                .expectNext(0)
                .verifyComplete();

        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        Mockito.verify(cartItemRepository, Mockito.times(1)).findByUserIdAndItemId(userId, itemId);
    }

    // endregion
}