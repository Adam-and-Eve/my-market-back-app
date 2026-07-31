package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PagingViewModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в CatalogController.
 * </summary>
 **/
public class CatalogControllerIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private CartService cartService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет отображение страницы каталога с сеточной структурой расположения View-моделей товаров.
     * </summary>
     **/
    @Test
    public void getCatalogShouldReturnItemsPageWithGridStructure() throws Exception
    {
        var asusLaptop = new ItemViewModel(
                1L,
                "Ноутбук ASUS ROG Strix SCAR 18",
                "Топовый игровой ноутбук...",
                "images/rog_scar18.png",
                385000L,
                0
        );

        var keychronKeyboard = new ItemViewModel(
                2L,
                "Клавиатура Keychron Q1 Pro",
                "Кастомная механическая клавиатура...",
                "images/keychron_q1.png",
                22500L,
                0
        );

        var row = List.of(asusLaptop, keychronKeyboard);

        var grid = List.of(row);

        var paging = new PagingViewModel(2, 0, false, true);

        var catalogPageViewModel = new CatalogPageViewModel(grid, "Gaming", "price_desc", paging);

        Mockito.when(itemService.findCatalog("Gaming", "price_desc", 0, 2))
                .thenReturn(catalogPageViewModel);

        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .param("search", "Gaming")
                        .param("sort", "price_desc")
                        .param("pageNumber", "0")
                        .param("pageSize", "2"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("items"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("items", "search", "sort", "paging"))
                .andExpect(MockMvcResultMatchers.model().attribute("items", grid))
                .andExpect(MockMvcResultMatchers.model().attribute("paging", paging));
    }

    /**
     * <summary>
     * Проверяет отображение детальной карточки для конкретного инициализированного товара (мышь Logitech).
     * </summary>
     **/
    @Test
    public void getItemShouldReturnItemDetailsForLogitechMouse() throws Exception
    {
        var itemId = 3L;
        var logitechMouseViewModel = new ItemViewModel(
                itemId,
                "Мышь Logitech G Pro X Superlight 2",
                "Сверхлегкая беспроводная игровая мышь...",
                "images/logitech_mouse.png",
                16800L,
                0
        );

        Mockito.when(itemService.findById(itemId)).thenReturn(logitechMouseViewModel);

        mockMvc.perform(MockMvcRequestBuilders.get("/items/{id}", itemId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("item"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("item"))
                .andExpect(MockMvcResultMatchers.model().attribute("item", logitechMouseViewModel));
    }

    /**
     * <summary>
     * Проверяет добавление процессора Intel в корзину с последующим сохранением контекста поиска Arrow Lake.
     * </summary>
     **/
    @Test
    public void updateCatalogItemShouldAddIntelProcessorAndRedirectWithSearchContext() throws Exception
    {
        var itemId = 4L;

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", "PLUS")
                        .param("search", "Arrow Lake")
                        .param("sort", "price_asc")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/items?search=Arrow%20Lake&sort=price_asc&pageNumber=0&pageSize=10"));

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.PLUS);
    }

    /**
     * <summary>
     * Проверяет изменение количества товара прямо со страницы карточки клавиатуры Keychron.
     * </summary>
     **/
    @Test
    public void updateItemShouldDecreaseKeychainQuantityAndRedirectToItsOwnCard() throws Exception
    {
        var itemId = 2L;

        mockMvc.perform(MockMvcRequestBuilders.post("/items/{id}", itemId)
                        .param("action", "MINUS"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/items/2"));

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.MINUS);
    }

    // endregion
}