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
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.Collections;
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
    void findAllShouldReturnListOfItemsFromRepository() {
        var item1 = new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L);

        var item2 = new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L);

        Mockito.when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2));

        Mockito.when(itemCacheService.findAll(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(itemService.findAll().collectList())
                .expectNextMatches(actualItems ->
                        actualItems.size() == 2 &&
                                actualItems.getFirst().getTitle().equals("Novation Launchkey") &&
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
     * Проверяет успешное получение и маппинг обогащенной View-модели товара из кэша/БД при его наличии.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnMappedViewModelWhenItemExists() {
        var itemId = 1L;

        var cartCount = 3;

        var itemModel = new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        var expectedViewModel = new ItemViewModel(itemId, "Novation Launchkey", "Studio MIDI", "/img1.png", 45000L, cartCount);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(itemModel));

        Mockito.when(itemCacheService.findById(Mockito.eq(itemId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        Mockito.when(cartService.findCountForItem(itemId)).thenReturn(Mono.just(cartCount));

        Mockito.when(itemMapper.toViewModel(itemModel, cartCount)).thenReturn(expectedViewModel);

        StepVerifier.create(itemService.findById(itemId))
                .expectNextMatches(actualViewModel ->
                        actualViewModel.title().equals(expectedViewModel.title()) && actualViewModel.count() == cartCount
                )
                .verifyComplete();

        Mockito.verify(itemCacheService, Mockito.times(1)).findById(Mockito.eq(itemId), Mockito.any());
    }

    /**
     * <summary>
     * Проверяет генерацию исключения ResponseStatusException со статусом 404 NOT FOUND из Mono.error(), если товар отсутствует.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowResponseStatusExceptionWhenItemDoesNotExist() {
        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Mono.empty());

        Mockito.when(itemCacheService.findById(Mockito.eq(nonExistingId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findById(nonExistingId))
                .expectErrorMatches(throwable ->
                        {
                            if (!(throwable instanceof ResponseStatusException) ||
                                    !((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND))
                                return false;
                            assert ((ResponseStatusException) throwable).getReason() != null;
                            return ((ResponseStatusException) throwable).getReason().equals("Item not found.");
                        }
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
        long itemId = 42L;

        var expectedModel = new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Mono.just(expectedModel));

        Mockito.when(itemCacheService.findById(Mockito.eq(itemId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findModelById(itemId))
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
        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Mono.empty());

        Mockito.when(itemCacheService.findById(Mockito.eq(nonExistingId), Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        StepVerifier.create(itemService.findModelById(nonExistingId))
                .expectErrorMatches(throwable ->
                        {
                            if (!(throwable instanceof ResponseStatusException) ||
                                    !((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND))
                                return false;
                            assert ((ResponseStatusException) throwable).getReason() != null;
                            return ((ResponseStatusException) throwable).getReason().equals("Item not found.");
                        }
                )
                .verify();
    }

    // endregion

    // region Tests for findCatalog

    /**
     * <summary>
     * Проверяет сборку нефильтрованной страницы каталога с использованием мока CatalogHelper.
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnUnfilteredPageWhenSearchIsBlank() {
        var rawSearch = "   ";

        var normalizedSearch = "";

        var rawSort = "DEFAULT";

        var pageNumber = 1;

        var pageSize = 10;

        var item = new ItemModel("Товар", "Описание", "/img.png", 100L);

        ReflectionTestUtils.setField(item, "id", 1L);

        var itemsList = List.of(item);

        var mockCounts = Map.of(item.getId(), 2);

        var pageable = PageRequest.of(0, 10, Sort.unsorted());

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(Mockito.any())).thenReturn(Sort.unsorted());

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", pageable))
                .thenReturn(Flux.just(item));

        Mockito.when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", ""))
                .thenReturn(Mono.just(1L));

        Mockito.when(cartService.findCountsForItems(List.of(1L))).thenReturn(Mono.just(mockCounts));

        Mockito.when(itemMapper.toRows(itemsList, mockCounts)).thenReturn(Collections.emptyList());

        StepVerifier.create(itemService.findCatalog(rawSearch, rawSort, pageNumber, pageSize))
                .expectNextMatches(catalogResult ->
                        catalogResult.search().isEmpty()
                )
                .verifyComplete();

        Mockito.verify(catalogHelper, Mockito.times(1)).normalizeSearch(rawSearch);

        Mockito.verify(itemRepository, Mockito.times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", pageable);
    }

    /**
     * <summary>
     * Проверяет сборку страницы каталога с фильтрацией по поисковой строке.
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnFilteredPageWhenSearchIsNotBlank() {
        var rawSearch = "novation";

        var normalizedSearch = "novation";

        var rawSort = "DEFAULT";

        var pageNumber = 1;

        var pageSize = 5;

        var matchingItem = new ItemModel("Novation Launchkey 88", "MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(matchingItem, "id", 1L);

        var mockCounts = Map.of(matchingItem.getId(), 1);

        var pageable = PageRequest.of(0, 5, Sort.unsorted());

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(Mockito.any())).thenReturn(Sort.unsorted());

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch, pageable))
                .thenReturn(Flux.just(matchingItem));

        Mockito.when(itemRepository.countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch))
                .thenReturn(Mono.just(1L));

        Mockito.when(cartService.findCountsForItems(List.of(1L))).thenReturn(Mono.just(mockCounts));

        Mockito.when(itemMapper.toRows(List.of(matchingItem), mockCounts)).thenReturn(Collections.emptyList());

        StepVerifier.create(itemService.findCatalog(rawSearch, rawSort, pageNumber, pageSize))
                .expectNextMatches(catalogResult ->
                        catalogResult.search().equals(normalizedSearch)
                )
                .verifyComplete();

        Mockito.verify(catalogHelper, Mockito.times(1)).normalizeSearch(rawSearch);

        Mockito.verify(itemRepository, Mockito.times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch, pageable);
    }

    // endregion
}