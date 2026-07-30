package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.helpers.CatalogHelper;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @InjectMocks
    private ItemServiceImpl itemService;

    // endregion

    // region Tests for findAll

    /**
     * <summary>
     * Проверяет, что метод возвращает полный список доменных моделей товаров из репозитория.
     * </summary>
     **/
    @Test
    void findAllShouldReturnListOfItemsFromRepository()
    {
        var expectedItems = List.of(
                new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L),
                new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L)
        );
        Mockito.when(itemRepository.findAll()).thenReturn(expectedItems);

        var actualItems = itemService.findAll();

        Assertions.assertEquals(expectedItems.size(), actualItems.size());

        Assertions.assertEquals(expectedItems, actualItems);

        Mockito.verify(itemRepository, Mockito.times(1)).findAll();
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет успешное получение и маппинг обогащенной View-модели товара при его наличии в БД.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnMappedViewModelWhenItemExists()
    {
        var itemId = 1L;

        var cartCount = 3;

        var itemModel = new ItemModel("Novation Launchkey", "Studio MIDI", "/img1.png", 45000L);

        ReflectionTestUtils.setField(itemModel, "id", itemId);

        var expectedViewModel = new ItemViewModel(itemId, "Novation Launchkey", "Studio MIDI", "/img1.png", 45000L, cartCount);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemModel));

        Mockito.when(cartService.findCountForItem(itemId)).thenReturn(cartCount);

        Mockito.when(itemMapper.toViewModel(itemModel, cartCount)).thenReturn(expectedViewModel);

        var actualViewModel = itemService.findById(itemId);

        Assertions.assertNotNull(actualViewModel);

        Assertions.assertEquals(expectedViewModel.title(), actualViewModel.title());

        Assertions.assertEquals(cartCount, actualViewModel.count());
    }

    /**
     * <summary>
     * Проверяет генерацию исключения ResponseStatusException со статусом 404 NOT FOUND, если товар отсутствует.
     * </summary>
     **/
    @Test
    void findByIdShouldThrowResponseStatusExceptionWhenItemDoesNotExist()
    {
        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            itemService.findById(nonExistingId);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        Assertions.assertEquals("Item not found.", exception.getReason());
    }

    // endregion

    // region Tests for findModelById

    /**
     * <summary>
     * Проверяет извлечение чистой доменной модели по идентификатору товара.
     * </summary>
     **/
    @Test
    void findModelByIdShouldReturnDomainModelWhenItemExists()
    {
        long itemId = 42L;

        var expectedModel = new ItemModel("Xiaomi Mi Mix 4", "Flagship Phone", "/img2.png", 60000L);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(expectedModel));

        var actualModel = itemService.findModelById(itemId);

        Assertions.assertNotNull(actualModel);

        Assertions.assertEquals(expectedModel, actualModel);
    }

    /**
     * <summary>
     * Проверяет генерацию 404 ошибки при поиске доменной модели несуществующего товара.
     * </summary>
     **/
    @Test
    void findModelByIdShouldThrowResponseStatusExceptionWhenItemDoesNotExist()
    {
        long nonExistingId = 999L;

        Mockito.when(itemRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        var exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            itemService.findModelById(nonExistingId);
        });

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // endregion

    // region Tests for findCatalog

    /**
     * <summary>
     * Проверяет сборку нефильтрованной страницы каталога, когда поисковая строка пуста или нормализуется в пустую.
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnUnfilteredPageWhenSearchIsBlank()
    {
        var rawSearch = "   ";

        var normalizedSearch = "";

        var rawSort = "DEFAULT";

        var pageNumber = 1;

        var pageSize = 10;

        var sortOrder = Sort.unsorted();

        var pageable = PageRequest.of(0, 10, sortOrder);

        var item = new ItemModel("Товар", "Описание", "/img.png", 100L);

        ReflectionTestUtils.setField(item, "id", 1L);

        var itemsList = List.of(item);

        var itemsPage = new PageImpl<>(itemsList, pageable, 1);

        var mockCounts = Map.of(item.getId(), 2);

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(Mockito.any(ItemSortEnumModel.class))).thenReturn(sortOrder);

        Mockito.when(itemRepository.findAll(pageable)).thenReturn(itemsPage);

        Mockito.when(cartService.findCountsForItems(Mockito.anyList())).thenReturn(mockCounts);

        Mockito.when(itemMapper.toRows(itemsList, mockCounts)).thenReturn(Collections.emptyList());

        var catalogResult = itemService.findCatalog(rawSearch, rawSort, pageNumber, pageSize);

        Assertions.assertNotNull(catalogResult);

        Assertions.assertEquals(normalizedSearch, catalogResult.search());

        Mockito.verify(itemRepository, Mockito.times(1)).findAll(pageable);

        Mockito.verify(itemRepository, Mockito.never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class)
        );
    }

    /**
     * <summary>
     * Проверяет сборку страницы каталога с фильтрацией по поисковой строке (поиск без учета регистра).
     * </summary>
     **/
    @Test
    void findCatalogShouldReturnFilteredPageWhenSearchIsNotBlank()
    {
        var rawSearch = "novation";

        var normalizedSearch = "novation";

        var rawSort = "DEFAULT";

        var pageNumber = 1;

        var pageSize = 5;

        var sortOrder = Sort.unsorted();

        var pageable = PageRequest.of(0, 5, sortOrder);

        var item = new ItemModel("Novation Launchkey 88", "MIDI", "/img.png", 45000L);

        ReflectionTestUtils.setField(item, "id", 1L);

        var itemsList = List.of(item);

        var itemsPage = new PageImpl<>(itemsList, pageable, 1);

        var mockCounts = Map.of(item.getId(), 1);

        Mockito.when(catalogHelper.normalizeSearch(rawSearch)).thenReturn(normalizedSearch);

        Mockito.when(catalogHelper.normalizePageNumber(pageNumber)).thenReturn(pageNumber);

        Mockito.when(catalogHelper.normalizePageSize(pageSize)).thenReturn(pageSize);

        Mockito.when(catalogHelper.resolveSort(Mockito.any(ItemSortEnumModel.class))).thenReturn(sortOrder);

        Mockito.when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                normalizedSearch, normalizedSearch, pageable)).thenReturn(itemsPage);

        Mockito.when(cartService.findCountsForItems(Mockito.anyList())).thenReturn(mockCounts);

        Mockito.when(itemMapper.toRows(itemsList, mockCounts)).thenReturn(Collections.emptyList());

        var catalogResult = itemService.findCatalog(rawSearch, rawSort, pageNumber, pageSize);

        Assertions.assertNotNull(catalogResult);

        Assertions.assertEquals(normalizedSearch, catalogResult.search());

        Mockito.verify(itemRepository, Mockito.times(1))
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch, pageable);

        Mockito.verify(itemRepository, Mockito.never()).findAll(pageable);
    }

    // endregion
}