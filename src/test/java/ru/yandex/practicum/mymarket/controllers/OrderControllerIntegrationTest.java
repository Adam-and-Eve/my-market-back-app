package ru.yandex.practicum.mymarket.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.mymarket.MyMarketAppApplicationTests;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

/**
 * <summary>
 * Интеграционные тесты для проверки корректности выполнения запросов в OrderController.
 * </summary>
 **/
public class OrderControllerIntegrationTest extends MyMarketAppApplicationTests {

    // region Fields

    @MockitoBean
    private OrderService orderService;

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет отображение страницы списка всех оформленных заказов пользователя.
     * </summary>
     **/
    @Test
    public void getOrdersShouldReturnJournalPageWithOrdersList() throws Exception
    {
        var keychronKeyboard = new ItemViewModel(
                2L,
                "Клавиатура Keychron Q1 Pro",
                "Кастомная механическая клавиатура...",
                "images/keychron_q1.png",
                22500L,
                1
        );

        var mockOrder = new OrderViewModel(101L, List.of(keychronKeyboard), 22500L);

        var ordersList = List.of(mockOrder);

        Mockito.when(orderService.findAll()).thenReturn(ordersList);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("orders"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("orders"))
                .andExpect(MockMvcResultMatchers.model().attribute("orders", ordersList));
    }

    /**
     * <summary>
     * Проверяет отображение страницы конкретного заказа по умолчанию (без приветственного сообщения).
     * </summary>
     **/
    @Test
    public void getOrderShouldReturnOrderDetailsWithoutWelcomeMessage() throws Exception
    {
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

        Mockito.when(orderService.findById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", orderId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("order"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("order", "newOrder"))
                .andExpect(MockMvcResultMatchers.model().attribute("order", mockOrder))
                .andExpect(MockMvcResultMatchers.model().attribute("newOrder", false));
    }

    /**
     * <summary>
     * Проверяет передачу флага newOrder в модель для вывода приветственного сообщения только что созданному заказу.
     * </summary>
     **/
    @Test
    public void getOrderWithNewOrderParamTrueShouldPassFlagToModel() throws Exception
    {
        var orderId = 102L;

        var mockOrder = new OrderViewModel(orderId, List.of(), 0L);

        Mockito.when(orderService.findById(orderId)).thenReturn(mockOrder);

        mockMvc.perform(MockMvcRequestBuilders.get("/orders/{id}", orderId)
                        .param("newOrder", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("order"))
                .andExpect(MockMvcResultMatchers.model().attribute("newOrder", true));
    }

    /**
     * <summary>
     * Проверяет успешное оформление покупки и перенаправление на страницу чека, если корзина не пуста.
     * </summary>
     **/
    @Test
    public void buyShouldCreateOrderAndRedirectToReceiptPageWhenCartIsNotEmpty() throws Exception
    {
        var expectedOrderId = 42L;

        Mockito.when(orderService.buy()).thenReturn(expectedOrderId);

        mockMvc.perform(MockMvcRequestBuilders.post("/buy"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/orders/42?newOrder=true"));
    }

    /**
     * <summary>
     * Проверяет возврат на страницу корзины, если при попытке покупки корзина покупателя оказалась пустой.
     * </summary>
     **/
    @Test
    public void buyShouldRedirectBackToCartPageWhenCartIsEmpty() throws Exception
    {
        Mockito.when(orderService.buy()).thenReturn(-1L);

        mockMvc.perform(MockMvcRequestBuilders.post("/buy"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/cart/items"));
    }

    // endregion
}