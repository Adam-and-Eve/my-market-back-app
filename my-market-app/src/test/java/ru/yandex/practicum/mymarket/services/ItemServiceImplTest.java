package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.helpers.CatalogHelper;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemCacheService;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики ItemServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    // region Fields

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private CatalogHelper catalogHelper;

    @Mock
    private ItemCacheService itemCacheService;

    @InjectMocks
    private ItemServiceImpl itemService;

    // endregion

    // region Tests for findAll

    /**
     * <summary>
     * Проверяет, что метод возвращает полный список доменных моделей товаров через взаимодействие с ItemCacheService.
     * </summary>
     **/
    @Test
    void findAllShouldReturnListOfItemsFromCacheService() {
        var username = "user";

        var item1 = new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L);

        var item2 = new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L);

        Mockito.when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2));

        Mockito.when(itemCacheService.findAll(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(itemService.findAll(username).collectList())
                .expectNextMatches(actualItems ->
                        actualItems.size() == 2 &&
                                actualItems.get(0).getTitle().equals("Novation Launchkey") &&
                                actualItems.get(1).getTitle().equals("Xiaomi Mi Mix 4")
                )
                .verifyComplete();

        Mockito.verify(itemRepository, Mockito.times(1)).findAll();

        Mockito.verify(itemCacheService, Mockito.times(1)).findAll(Mockito.any());
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет успешное получение и маппинг обогащенной View-модели товара из кэша/БД с количеством из корзины конкретного пользователя.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnMappedViewModelWhenItemExists() {
        var username = "user";

        var itemId = 1L;

        var cartCount = 3;

        var itemModel = new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        var expectedViewModel = new ItemViewModel(itemId, "Novation Launchkey", "Studio MIDI", "/img1.png", 45000L, cartCount);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(itemModel));

        Mockito.when(itemCacheService.findById(Mockito.eq(itemId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        Mockito.when(cartService.findCountForItem(username, itemId)).thenReturn(Mono.just(cartCount));

        Mockito.when(itemMapper.toViewModel(itemModel, cartCount)).thenReturn(expectedViewModel);

        StepVerifier.create(itemService.findById(username, itemId))
                .expectNextMatches(actualViewModel ->
                        actualViewModel.title().equals(expectedViewModel.title()) && actualViewModel.count() == cartCount
                )
                .verifyComplete();

        Mockito.verify(itemCacheService, Mockito.times(1)).findById(Mockito.eq(itemId), Mockito.any());

        Mockito.verify(cartService, Mockito.times(1)).findCountForItem(username, itemId);
    }

    /**
     * <summary>
     * Проверяет генерацию исключения ResponseStatusException со статусом 404 NOT FOUND из Mono.error(), если товар отсутствует.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowResponseStatusExceptionWhenItemDoesNotExist() {
        var username = "user";

        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Mono.empty());

        Mockito.when(itemCacheService.findById(Mockito.eq(nonExistingId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findById(username, nonExistingId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException resEx &&
                                resEx.getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                                "Item not found.".equals(resEx.getReason())
                )
                .verify();
    }

    // endregion

    // region Tests for findModelById

    /**
     * <summary>
     * Проверяет извлечение чистой доменной модели по идентификатору товара через Mono и ItemCacheService.
     * </summary>
     **/
    @Test
    void findModelByIdShouldReturnDomainModelWhenItemExists() {
        var username = "user";

        long itemId = 42L;

        var expectedModel = new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(expectedModel));

        Mockito.when(itemCacheService.findById(Mockito.eq(itemId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findModelById(username, itemId))
                .expectNext(expectedModel)
                .verifyComplete();
    }

    /**
     * <summary>
     * Проверяет генерацию 404 ошибки в реактивном потоке при поиске доменной модели несуществующего товара.
     * </summary>
     **/
    @Test
    void findModelByIdShouldThrowResponseStatusExceptionWhenItemDoesNotExist() {
        var username = "user";

        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Mono.empty());

        Mockito.when(itemCacheService.findById(Mockito.eq(nonExistingId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findModelById(username, nonExistingId))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException resEx &&
                                resEx.getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                                "Item not found.".equals(resEx.getReason())
                )
                .verify();
    }

    // endregion

    // region Tests for findCatalog

    /**
     * <summary>
     * Проверяет сборку нефильтрованной страницы каталога с выполнением запросов к базе данных и расчетом количества элементов в корзине пользователя.
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnUnfilteredPageWhenSearchIsBlank() {
        var username = "user";

        var rawSearch = "   ";

        var normalizedSearch = "";

        var rawSort = "NO";

        var pageNumber = 1;

        var pageSize = 10;

        var item = new ItemModel("Товар", "Описание", "/img.png", 100L);

        ReflectionTestUtils.setField(item, "id", 1L);

        var mockCounts = Map.of(item.getId(), 2);

        var sort = Sort.unsorted();

        var pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(ItemSortEnumModel.NO)).thenReturn(sort);

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedSearch, normalizedSearch, pageable
        )).thenReturn(Flux.just(item));

        Mockito.when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedSearch, normalizedSearch
        )).thenReturn(Mono.just(1L));

        Mockito.when(cartService.findCountsForItems(username, List.of(1L))).thenReturn(Mono.just(mockCounts));

        Mockito.when(itemMapper.toRows(List.of(item), mockCounts)).thenReturn(Collections.emptyList());

        StepVerifier.create(itemService.findCatalog(username, rawSearch, rawSort, pageNumber, pageSize))
                .expectNextMatches(catalogResult ->
                        catalogResult.search().isEmpty() &&
                                catalogResult.sort().equals(ItemSortEnumModel.NO.name()) &&
                                catalogResult.paging().pageNumber() == 1 &&
                                !catalogResult.paging().hasPrevious() &&
                                !catalogResult.paging().hasNext()
                )
                .verifyComplete();

        Mockito.verify(catalogHelper, Mockito.times(1)).normalizeSearch(rawSearch);

        Mockito.verify(catalogHelper, Mockito.times(1)).resolveSort(ItemSortEnumModel.NO);

        Mockito.verify(cartService, Mockito.times(1)).findCountsForItems(username, List.of(1L));
    }

    /**
     * <summary>
     * Проверяет сборку страницы каталога с фильтрацией элементов в памяти по поисковой строке и сортировкой PRICE.
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnFilteredPageWhenSearchIsNotBlank() {
        var username = "user";

        var rawSearch = "novation";

        var normalizedSearch = "novation";

        var rawSort = "PRICE";

        var pageNumber = 1;

        var pageSize = 5;

        var matchingItem = new ItemModel("Novation Launchkey 88", "MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(matchingItem, "id", 1L);

        var mockCounts = Map.of(matchingItem.getId(), 1);

        var sort = Sort.by("price").ascending();

        var pageable = PageRequest.of(0, pageSize, sort);

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(ItemSortEnumModel.PRICE)).thenReturn(sort);

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedSearch, normalizedSearch, pageable
        )).thenReturn(Flux.just(matchingItem));

        Mockito.when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedSearch, normalizedSearch
        )).thenReturn(Mono.just(1L));

        Mockito.when(cartService.findCountsForItems(username, List.of(1L))).thenReturn(Mono.just(mockCounts));

        Mockito.when(itemMapper.toRows(List.of(matchingItem), mockCounts)).thenReturn(Collections.emptyList());

        StepVerifier.create(itemService.findCatalog(username, rawSearch, rawSort, pageNumber, pageSize))
                .expectNextMatches(catalogResult ->
                        catalogResult.search().equals(normalizedSearch) &&
                                catalogResult.sort().equals(ItemSortEnumModel.PRICE.name())
                )
                .verifyComplete();

        Mockito.verify(catalogHelper, Mockito.times(1)).normalizeSearch(rawSearch);

        Mockito.verify(catalogHelper, Mockito.times(1)).resolveSort(ItemSortEnumModel.PRICE);

        Mockito.verify(cartService, Mockito.times(1)).findCountsForItems(username, List.of(1L));
    }

    /**
     * <summary>
     * Проверяет правильность определения флагов пагинации (hasPrevious и hasNext) при запросе промежуточной страницы.
     * Запросы выборок и подсчета общего количества записей делегируются в базу данных.
     * </summary>
     **/
    @Test
    void findCatalogShouldSetCorrectPagingFlagsForMiddlePage() {
        var username = "user";

        var pageNumber = 2;

        var pageSize = 1;

        var item2 = new ItemModel("Item 2", "Desc 2", "/2.png", 200L);

        ReflectionTestUtils.setField(item2, "id", 2L);

        var sort = Sort.unsorted();

        var pageable = PageRequest.of(pageNumber - 1, pageSize, sort);

        Mockito.when(catalogHelper.normalizeSearch(Mockito.any())).thenReturn("");

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(ItemSortEnumModel.NO)).thenReturn(sort);

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "", "", pageable
        )).thenReturn(Flux.just(item2));

        Mockito.when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                "", ""
        )).thenReturn(Mono.just(3L));

        Mockito.when(cartService.findCountsForItems(username, List.of(2L))).thenReturn(Mono.just(Map.of(2L, 1)));

        Mockito.when(itemMapper.toRows(List.of(item2), Map.of(2L, 1))).thenReturn(Collections.emptyList());

        StepVerifier.create(itemService.findCatalog(username, null, "NO", pageNumber, pageSize))
                .expectNextMatches(catalogResult ->
                        catalogResult.paging().pageNumber() == 2 &&
                                catalogResult.paging().hasPrevious() &&
                                catalogResult.paging().hasNext()
                )
                .verifyComplete();

        Mockito.verify(catalogHelper, Mockito.times(1)).resolveSort(ItemSortEnumModel.NO);

        Mockito.verify(cartService, Mockito.times(1)).findCountsForItems(username, List.of(2L));
    }

    // endregion
}