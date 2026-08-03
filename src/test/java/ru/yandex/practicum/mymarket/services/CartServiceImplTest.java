package ru.yandex.practicum.mymarket.services;

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
import ru.yandex.practicum.mymarket.mappers.CartMapper;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.CartItemModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.repositories.CartItemRepository;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.Collections;
import java.util.List;

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

    @InjectMocks
    private CartServiceImpl cartService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет сборку пустой корзины, когда в реактивном репозитории нет записей.
     * </summary>
     **/
    @Test
    void findCartShouldReturnEmptyCartWhenNoItemsExist() {
        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.empty());

        Mockito.when(cartMapper.toViewModel(Collections.emptyList()))
                .thenReturn(new CartPageViewModel(Collections.emptyList(), 0L));

        StepVerifier.create(cartService.findCart())
                .expectNextMatches(cartPage ->
                        cartPage.items().isEmpty() && cartPage.total() == 0L
                )
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет сборку и маппинг наполненной корзины с получением данных из ItemRepository и правильной сигнатурой ItemMapper.
     * </summary>
     **/
    @Test
    void findCartShouldReturnPopulatedCartWhenItemsExist() {
        var itemId = 1L;

        var cartItem = new CartItemModel(itemId, 2);

        var itemModel = new ItemModel("Novation Launchkey 88", "MIDI-контроллер", "/novation.png", 45000L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        var itemViewModel = new ItemViewModel(itemId, "Novation Launchkey 88", "MIDI-контроллер", "/novation.png", 45000L, 2);

        var expectedPage = new CartPageViewModel(List.of(itemViewModel), 90000L);

        Mockito.when(cartItemRepository.findAllByOrderByItemIdAsc()).thenReturn(Flux.just(cartItem));

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(itemModel));

        Mockito.when(itemMapper.toViewModel(cartItem, itemModel)).thenReturn(itemViewModel);

        Mockito.when(cartMapper.toViewModel(List.of(itemViewModel))).thenReturn(expectedPage);

        StepVerifier.create(cartService.findCart())
                .expectNextMatches(cartPage ->
                        cartPage.items().size() == 1 && cartPage.total() == 90000L && cartPage.items().getFirst().title().equals("Novation Launchkey 88")
                )
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет корректное добавление нового товара в корзину (когда его там еще не было) со стартовым количеством.
     * </summary>
     **/
    @Test
    void updateItemCountShouldCreateNewCartItemWhenActionIsPlusAndItemNotPresent() {
        var itemId = 1L;

        var itemModel = new ItemModel("Товар", "Описание", "/img.png", 100L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.empty());

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(itemModel));

        Mockito.when(cartItemRepository.save(Mockito.any(CartItemModel.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(cartService.updateItemCount(itemId, CartActionEnumModel.PLUS))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).save(Mockito.any(CartItemModel.class));
    }

    /**
     * <summary>
     * Проверяет, что при добавлении несуществующего в каталоге товара выбрасывается ResponseStatusException 404.
     * </summary>
     **/
    @Test
    void updateItemCountShouldThrowNotFoundWhenItemDoesNotExistInCatalog() {
        var itemId = 999L;

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.empty());

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(itemId, CartActionEnumModel.PLUS))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND)
                )
                .verify();
    }

    /**
     * <summary>
     * Проверяет уменьшение количества товара в корзине и его полное удаление, если количество достигло нуля.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteCartItemWhenActionIsMinusAndQuantityDropsToZero() {
        var itemId = 1L;

        var cartItem = new CartItemModel(itemId, 1);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.just(cartItem));

        Mockito.when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(itemId, CartActionEnumModel.MINUS))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);

        Mockito.verify(cartItemRepository, Mockito.never()).save(Mockito.any());
    }

    /**
     * <summary>
     * Проверяет безусловное удаление позиции из корзины при действии DELETE.
     * </summary>
     **/
    @Test
    void updateItemCountShouldDeleteImmediatelyWhenActionIsDelete() {
        var itemId = 1L;

        var cartItem = new CartItemModel(itemId, 5);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.just(cartItem));

        Mockito.when(cartItemRepository.delete(cartItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateItemCount(itemId, CartActionEnumModel.DELETE))
                .verifyComplete();

        Mockito.verify(cartItemRepository, Mockito.times(1)).delete(cartItem);
    }

    /**
     * <summary>
     * Проверяет пакетное получение мапы количеств товаров из реактивного потока.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnCorrectMap() {
        var itemId1 = 10L;

        var itemId2 = 20L;

        var itemIds = List.of(itemId1, itemId2);

        var cartItem1 = new CartItemModel(itemId1, 2);

        var cartItem2 = new CartItemModel(itemId2, 5);

        Mockito.when(cartItemRepository.findAllByItemIdIn(itemIds)).thenReturn(Flux.just(cartItem1, cartItem2));

        StepVerifier.create(cartService.findCountsForItems(itemIds))
                .expectNextMatches(resultMaps ->
                        resultMaps.size() == 2 && resultMaps.get(itemId1) == 2 && resultMaps.get(itemId2) == 5
                )
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет, что findCountsForItems возвращает пустую мапу, если передан пустой список ID, без обращений к репозиторию.
     * </summary>
     **/
    @Test
    void findCountsForItemsShouldReturnEmptyMapWhenParamIsEmpty() {
        StepVerifier.create(cartService.findCountsForItems(Collections.emptyList()))
                .expectNextMatches(result -> result != null && result.isEmpty())
                .verifyComplete();

        Mockito.verifyNoInteractions(cartItemRepository);
    }

    /**
     * <summary>
     * Проверяет получение количества для конкретного товара, если он присутствует в корзине.
     * </summary>
     **/
    @Test
    void findCountForItemShouldReturnQuantityWhenItemExists() {
        var itemId = 5L;

        var cartItem = new CartItemModel(itemId, 4);

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(cartService.findCountForItem(itemId))
                .expectNext(4)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет, что метод возвращает 0, если запрашиваемый товар отсутствует в корзине.
     * </summary>
     **/
    @Test
    void findCountForItemShouldReturnZeroWhenItemDoesNotExist() {
        var itemId = 5L;

        Mockito.when(cartItemRepository.findByItemId(itemId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.findCountForItem(itemId))
                .expectNext(0)
                .verifyComplete();
    }

    // endregion
}