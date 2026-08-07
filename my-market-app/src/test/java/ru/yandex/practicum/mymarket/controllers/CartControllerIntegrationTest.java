package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
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
import java.util.Map;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в CartController.
 * </summary>
 **/
public class CartControllerIntegrationTest extends MyMarketAppApplicationTests {

    // region Constants

    private static final String TEST_USERNAME = "admin";

    // endregion

    // region Fields

    @MockitoBean
    private CartService cartService;

    // endregion

    // region Tests for GET /cart/items

    /**
     * <summary>
     * Проверяет отображение страницы корзины для аутентифицированного пользователя.
     * Проверяет статус 200 OK и наличие сгенерированного HTML-контента с именами позиций.
     * </summary>
     **/
    @Test
    @WithMockUser(username = TEST_USERNAME)
    public void getCartShouldReturnCartPageWithItemsAndTotal() {
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

        var mockCartPage = new CartPageViewModel(
                cartItems,
                totalSum,
                true,
                100000L,
                true,
                null
        );

        Mockito.when(cartService.findCart(TEST_USERNAME)).thenReturn(Mono.just(mockCartPage));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    assert htmlBody != null;

                    Assertions.assertTrue(htmlBody.contains("Клавиатура Keychron Q1 Pro"));

                    Assertions.assertTrue(htmlBody.contains("Мышь Logitech G Pro X Superlight 2"));
                });

        Mockito.verify(cartService, Mockito.times(1)).findCart(TEST_USERNAME);
    }

    // endregion

    // region Tests for POST /cart/items

    /**
     * <summary>
     * Проверяет успешное увеличение количества товара в корзине аутентифицированного пользователя
     * и последующий HTTP-редирект на "/cart/items".
     * </summary>
     **/
    @Test
    public void updateCartItemShouldIncreaseQuantityAndRedirectToCart() {
        var itemId = 2L;

        Mockito.when(cartService.updateItemCount(TEST_USERNAME, itemId, CartActionEnumModel.PLUS))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", String.valueOf(itemId));

        formData.add("action", "PLUS");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(cartService, Mockito.times(1))
                .updateItemCount(TEST_USERNAME, itemId, CartActionEnumModel.PLUS);
    }

    /**
     * <summary>
     * Проверяет полный сценарий удаления позиции из корзины при действии DELETE.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldRemoveItemWhenActionIsDelete() {
        var itemId = 3L;

        Mockito.when(cartService.updateItemCount(TEST_USERNAME, itemId, CartActionEnumModel.DELETE))
                .thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", String.valueOf(itemId));

        formData.add("action", "DELETE");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(cartService, Mockito.times(1))
                .updateItemCount(TEST_USERNAME, itemId, CartActionEnumModel.DELETE);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос без параметра action возвращает ошибку 400 Bad Request
     * и не обращается к слою сервисов.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldReturnBadRequestWhenActionIsNull() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "1");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос без указания идентификатора товара возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldReturnBadRequestWhenIdIsNull() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("action", "PLUS");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос с некорректным строковым значением enum в параметре action возвращает 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateCartItemShouldReturnBadRequestWhenActionIsInvalid() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "1");

        formData.add("action", "INVALID_ACTION_NAME");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService);
    }

    // endregion
}