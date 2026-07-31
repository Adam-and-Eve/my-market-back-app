package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CartPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в CartController.
 * </summary>
 **/
public class CartControllerIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @MockitoBean
    private CartService cartService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет отображение страницы корзины со списком добавленных товаров и корректным подсчетом общей стоимости.
     * </summary>
     **/
    @Test
    public void getCartShouldReturnCartPageWithItemsAndTotal() throws Exception
    {
        var keychronKeyboard = new ItemViewModel(
                2L,
                "Клавиатура Keychron Q1 Pro",
                "Кастомная механическая клавиатура...",
                "images/keychron_q1.png",
                22500L,
                1
        );

        var logitechMouse = new ItemViewModel(
                3L,
                "Мышь Logitech G Pro X Superlight 2",
                "Сверхлегкая беспроводная игровая мышь...",
                "images/logitech_mouse.png",
                16800L,
                2
        );

        var cartItems = List.of(keychronKeyboard, logitechMouse);

        var totalSum = 22500L + (16800L * 2);

        var mockCartPage = new CartPageViewModel(cartItems, totalSum);

        Mockito.when(cartService.findCart()).thenReturn(mockCartPage);

        mockMvc.perform(MockMvcRequestBuilders.get("/cart/items"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("cart"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("items", "total"))
                .andExpect(MockMvcResultMatchers.model().attribute("items", cartItems))
                .andExpect(MockMvcResultMatchers.model().attribute("total", totalSum));
    }

    /**
     * <summary>
     * Проверяет успешное увеличение количества товара в корзине и последующий редирект.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldIncreaseQuantityAndRedirectToCart() throws Exception
    {
        var itemId = 2L;

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", "PLUS"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/cart/items"));

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.PLUS);
    }

    /**
     * <summary>
     * Проверяет сценарий полного удаления позиции из корзины с помощью экшена DELETE.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldRemoveItemWhenActionIsDelete() throws Exception
    {
        var itemId = 3L;

        mockMvc.perform(MockMvcRequestBuilders.post("/cart/items")
                        .param("id", String.valueOf(itemId))
                        .param("action", "DELETE"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/cart/items"));

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.DELETE);
    }

    // endregion
}