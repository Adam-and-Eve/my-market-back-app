package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogCellViewModel;
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
     * Проверяет отображение страницы каталога с сеточной структурой расположения товаров.
     * </summary>
     **/
    @Test
    public void getCatalogShouldReturnItemsPageWithGridStructure() {
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

        var cell1 = CatalogCellViewModel.of(asusLaptop);

        var cell2 = CatalogCellViewModel.of(keychronKeyboard);

        var row = List.of(cell1, cell2);

        var grid = List.of(row);

        var paging = new PagingViewModel(2, 0, false, true);

        var catalogPageViewModel = new CatalogPageViewModel(grid, "Gaming", "price_desc", paging);

        Mockito.when(itemService.findCatalog("Gaming", "price_desc", 0, 2))
                .thenReturn(Mono.just(catalogPageViewModel));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("search", "Gaming")
                        .queryParam("sort", "price_desc")
                        .queryParam("pageNumber", "0")
                        .queryParam("pageSize", "2")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    Assertions.assertNotNull(htmlBody);
                    Assertions.assertTrue(htmlBody.contains("Ноутбук ASUS ROG Strix SCAR 18"));
                    Assertions.assertTrue(htmlBody.contains("Клавиатура Keychron Q1 Pro"));
                });

        Mockito.verify(itemService, Mockito.times(1)).findCatalog("Gaming", "price_desc", 0, 2);
    }

    /**
     * <summary>
     * Проверяет отображение детальной карточки для конкретного инициализированного товара (мышь Logitech).
     * </summary>
     **/
    @Test
    public void getItemShouldReturnItemDetailsForLogitechMouse() {
        var itemId = 3L;

        var logitechMouseViewModel = new ItemViewModel(
                itemId,
                "Мышь Logitech G Pro X Superlight 2",
                "Сверхлегкая беспроводная игровая мышь...",
                "images/logitech_mouse.png",
                16800L,
                0
        );

        Mockito.when(itemService.findById(itemId)).thenReturn(Mono.just(logitechMouseViewModel));

        webTestClient.get().uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(htmlBody -> {
                    Assertions.assertNotNull(htmlBody);
                    Assertions.assertTrue(htmlBody.contains("Мышь Logitech G Pro X Superlight 2"));
                });

        Mockito.verify(itemService, Mockito.times(1)).findById(itemId);
    }

    /**
     * <summary>
     * Проверяет добавление процессора Intel в корзину с последующим сохранением контекста поиска Arrow Lake и редиректом.
     * </summary>
     **/
    @Test
    public void updateCatalogItemShouldAddIntelProcessorAndRedirectWithSearchContext() {
        var itemId = 4L;

        Mockito.when(cartService.updateItemCount(itemId, CartActionEnumModel.PLUS)).thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("id", String.valueOf(itemId));

        formData.add("action", "PLUS");

        formData.add("search", "Arrow Lake");

        formData.add("sort", "price_asc");

        formData.add("pageNumber", "0");

        formData.add("pageSize", "10");

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items?search=Arrow%20Lake&sort=price_asc&pageNumber=0&pageSize=10");

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.PLUS);
    }

    /**
     * <summary>
     * Проверяет изменение количества товара со страницы детальной карточки и последующий редирект обратно на эту карточку.
     * </summary>
     **/
    @Test
    public void updateItemShouldDecreaseKeychronQuantityAndRedirectToItemDetails() {
        var itemId = 2L;

        Mockito.when(cartService.updateItemCount(itemId, CartActionEnumModel.MINUS)).thenReturn(Mono.empty());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", String.valueOf(itemId));

        formData.add("action", "MINUS");

        webTestClient.post().uri("/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items/" + itemId);

        Mockito.verify(cartService, Mockito.times(1)).updateItemCount(itemId, CartActionEnumModel.MINUS);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос в витрине без указания действия (action == null) возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateCatalogItemShouldReturnBadRequestWhenActionIsNull() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "4");

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService, itemService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос в витрине без указания идентификатора товара (id == null) возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateCatalogItemShouldReturnBadRequestWhenIdIsNull() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("action", "PLUS");

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService, itemService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос в витрине с неизвестным типом действия возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateCatalogItemShouldReturnBadRequestWhenActionIsInvalid() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "4");

        formData.add("action", "INVALID_ACTION");

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService, itemService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос со страницы товара без указания действия (action == null) возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateItemShouldReturnBadRequestWhenActionIsNull() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "2");

        webTestClient.post().uri("/items/{id}", 2L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService, itemService);
    }

    /**
     * <summary>
     * Проверяет, что POST-запрос со страницы товара с некорректным экшеном возвращает ошибку 400 Bad Request.
     * </summary>
     **/
    @Test
    public void updateItemShouldReturnBadRequestWhenActionIsInvalid() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("id", "2");

        formData.add("action", "UNKNOWN_ACTION");

        webTestClient.post().uri("/items/{id}", 2L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isBadRequest();

        Mockito.verifyNoInteractions(cartService, itemService);
    }

    /**
     * <summary>
     * Проверяет успешную обработку GET-запроса без указания размера страницы (pageSize = null).
     * </summary>
     **/
    @Test
    public void getCatalogShouldHandleNullPageSize() {
        var mockPaging = new PagingViewModel(5, 1, false, false);

        var mockPage = new CatalogPageViewModel(List.of(), "", "NO", mockPaging);

        Mockito.when(itemService.findCatalog(null, null, null, null))
                .thenReturn(Mono.just(mockPage));

        webTestClient.get().uri("/items")
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(itemService, Mockito.times(1)).findCatalog(null, null, null, null);
    }

    /**
     * <summary>
     * Проверяет передачу значения pageSize = 0 в сервис каталога.
     * </summary>
     **/
    @Test
    public void getCatalogShouldPassZeroPageSizeToService() {
        var mockPaging = new PagingViewModel(5, 1, false, false);

        var mockPage = new CatalogPageViewModel(List.of(), "", "NO", mockPaging);

        Mockito.when(itemService.findCatalog(null, null, null, 0))
                .thenReturn(Mono.just(mockPage));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("pageSize", 0)
                        .build())
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(itemService, Mockito.times(1)).findCatalog(null, null, null, 0);
    }

    /**
     * <summary>
     * Проверяет передачу значения pageSize = 1000000 в сервис каталога.
     * </summary>
     **/
    @Test
    public void getCatalogShouldPassHugePageSizeToService() {
        var hugePageSize = 1_000_000;

        var mockPaging = new PagingViewModel(100, 1, false, false);

        var mockPage = new CatalogPageViewModel(List.of(), "", "NO", mockPaging);

        Mockito.when(itemService.findCatalog(null, null, null, hugePageSize))
                .thenReturn(Mono.just(mockPage));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
                        .queryParam("pageSize", hugePageSize)
                        .build())
                .exchange()
                .expectStatus().isOk();

        Mockito.verify(itemService, Mockito.times(1)).findCatalog(null, null, null, hugePageSize);
    }

    // endregion
}