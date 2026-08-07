package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.interfaces.PurchaseService;
import ru.yandex.practicum.mymarket.viewmodels.CheckoutResultViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в OrderController.
 * </summary>
 **/
public class OrderControllerIntegrationTest extends MyMarketAppApplicationTests {

    // region Constants

    private static final String TEST_USERNAME = "user";

    // endregion

    // region Fields

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private PurchaseService purchaseService;

    // endregion

    // region Tests for GET /orders

    /**
     * <summary>
     * Проверяет отображение страницы списка всех оформленных заказов аутентифицированного пользователя из реактивного Flux.
     * </summary>
     **/
    @Test
    @WithMockUser(username = TEST_USERNAME)
    public void getOrdersShouldReturnJournalPageWithOrdersList() {
        var keychronKeyboard = new ItemViewModel(
                2L,
                "Клавиатура Keychron Q1 Pro",
                "Кастомная механическая клавиатура...",
                "images/keychron_q1.png",
                22500L,
                1
        );

        var mockOrder = new OrderViewModel(101L, List.of(keychronKeyboard), 22500L);

        Mockito.when(orderService.findAll(TEST_USERNAME)).thenReturn(Flux.just(mockOrder));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    Assertions.assertNotNull(htmlBody);
                    Assertions.assertTrue(htmlBody.contains("Клавиатура Keychron Q1 Pro"));
                    Assertions.assertTrue(htmlBody.contains("101"));
                });

        Mockito.verify(orderService, Mockito.times(1)).findAll(TEST_USERNAME);
    }

    // endregion

    // region Tests for GET /orders/{id}

    /**
     * <summary>
     * Проверяет отображение страницы конкретного заказа пользователя по умолчанию (без приветственного сообщения).
     * </summary>
     **/
    @Test
    @WithMockUser(username = TEST_USERNAME)
    public void getOrderShouldReturnOrderDetailsWithoutWelcomeMessage() {
        var orderId = 101L;

        var logitechMouse = new ItemViewModel(
                3L,
                "Мышь Logitech G Pro X Superlight 2",
                "Сверхлегкая беспроводная игровая мышь...",
                "images/logitech_mouse.png",
                16800L,
                2
        );

        var mockOrder = new OrderViewModel(orderId, List.of(logitechMouse), 33600L);

        Mockito.when(orderService.findById(TEST_USERNAME, orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get().uri("/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    Assertions.assertNotNull(htmlBody);
                    Assertions.assertTrue(htmlBody.contains("Мышь Logitech G Pro X Superlight 2"));
                });

        Mockito.verify(orderService, Mockito.times(1)).findById(TEST_USERNAME, orderId);
    }

    /**
     * <summary>
     * Проверяет передачу флага newOrder в запросе для вывода приветственного сообщения.
     * </summary>
     **/
    @Test
    @WithMockUser(username = TEST_USERNAME)
    public void getOrderWithNewOrderParamTrueShouldPassFlagToModel() {
        var orderId = 102L;

        var mockOrder = new OrderViewModel(orderId, List.of(), 0L);

        Mockito.when(orderService.findById(TEST_USERNAME, orderId)).thenReturn(Mono.just(mockOrder));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .queryParam("newOrder", "true")
                        .build(orderId))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(Assertions::assertNotNull);

        Mockito.verify(orderService, Mockito.times(1)).findById(TEST_USERNAME, orderId);
    }

    /**
     * <summary>
     * Проверяет, что при передаче некорректного типа идентификатора заказа в пути URL возвращается 400 Bad Request.
     * </summary>
     **/
    @Test
    public void getOrderShouldReturnBadRequestWhenIdIsNotNumeric() {

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockOidcLogin()
                        .idToken(token -> token
                                .claim("preferred_username", TEST_USERNAME)
                                .claim("sub", "user-sub-id"))
                )
                .get()
                .uri("/orders/not-a-number")
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(orderService);
    }

    // endregion

    // region Tests for POST /buy

    /**
     * <summary>
     * Проверяет успешное оформление покупки через PurchaseService для аутентифицированного пользователя
     * и перенаправление на страницу чека заказа.
     * </summary>
     **/
    @Test
    public void buyShouldCreateOrderAndRedirectToReceiptPageWhenCartIsNotEmpty() {
        var expectedOrderId = 42L;

        Mockito.when(purchaseService.buy(TEST_USERNAME))
                .thenReturn(Mono.just(CheckoutResultViewModel.paid(expectedOrderId)));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/42?newOrder=true");

        Mockito.verify(purchaseService, Mockito.times(1)).buy(TEST_USERNAME);
    }

    /**
     * <summary>
     * Проверяет возврат на страницу корзины, если при попытке покупки корзина пользователя оказалась пустой.
     * </summary>
     **/
    @Test
    public void buyShouldRedirectBackToCartPageWhenCartIsEmpty() {
        Mockito.when(purchaseService.buy(TEST_USERNAME))
                .thenReturn(Mono.just(CheckoutResultViewModel.empty()));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(purchaseService, Mockito.times(1)).buy(TEST_USERNAME);
    }

    /**
     * <summary>
     * Проверяет перенаправление в корзину с параметром ошибки оплаты, если транзакция была отклонена платежным сервисом.
     * </summary>
     **/
    @Test
    public void buyShouldRedirectToCartWithPaymentErrorWhenRejected() {
        Mockito.when(purchaseService.buy(TEST_USERNAME))
                .thenReturn(Mono.just(CheckoutResultViewModel.rejected("Недостаточно средств")));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items?paymentError=true");

        Mockito.verify(purchaseService, Mockito.times(1)).buy(TEST_USERNAME);
    }

    /**
     * <summary>
     * Проверяет редирект в корзину при возврате пустого Mono из сервиса покупки (проверка ветки defaultIfEmpty).
     * </summary>
     **/
    @Test
    public void buyShouldRedirectToCartWhenPurchaseServiceReturnsEmptyMono() {
        Mockito.when(purchaseService.buy(TEST_USERNAME)).thenReturn(Mono.empty());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(TEST_USERNAME))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        Mockito.verify(purchaseService, Mockito.times(1)).buy(TEST_USERNAME);
    }

    // endregion
}