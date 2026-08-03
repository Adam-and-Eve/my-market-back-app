package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
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
     * Проверяет отображение страницы корзины. Так как WebTestClient работает на уровне HTTP-ответов,
     * мы проверяем успешный статус и наличие названий товаров в сгенерированном HTML-боди.
     * </summary>
     **/
    @Test
    public void getCartShouldReturnCartPageWithItemsAndTotal()
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

        Mockito.when(cartService.findCart()).thenReturn(Mono.just(mockCartPage));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    Assertions.assertTrue(htmlBody.contains("Клавиатура Keychron Q1 Pro"));
                    Assertions.assertTrue(htmlBody.contains("Мышь Logitech G Pro X Superlight 2"));
                });
    }

    /**
     * <summary>
     * Проверяет успешное увеличение количества товара в корзине через передачу Form Data
     * и последующий HTTP-редирект.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldIncreaseQuantityAndRedirectToCart()
    {
        var itemId = 2L;

        Mockito.when(cartService.updateItemCount(itemId, CartActionEnumModel.PLUS)).thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", String.valueOf(itemId));

        formData.add("action", "PLUS");

        webTestClient.post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.PLUS);
    }

    /**
     * <summary>
     * Проверяет сценарий полного удаления позиции из корзины с помощью экшена DELETE.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldRemoveItemWhenActionIsDelete()
    {
        var itemId = 3L;

        Mockito.when(cartService.updateItemCount(itemId, CartActionEnumModel.DELETE)).thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", String.valueOf(itemId));

        formData.add("action", "DELETE");

        webTestClient.post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.DELETE);
    }

    // endregion
}