package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.yandex.practicum.mymarket.interfaces.CartService;

/**
 * <summary>
 * Веб-контроллер для обработки запросов, связанных с просмотром содержимого корзины покупателя.
 * </summary>
 **/
@Controller
public class CartController {

    // region Fields

    /**
     * Сервис для работы с бизнес-логикой корзины.
     **/
    private final CartService cartService;

    // endregion

    // region Constructors

    public CartController(final CartService cartService) {
        this.cartService = cartService;
    }

    // endregion

    // region Actions

    /**
     * <summary>
     * Обрабатывает GET-запросы к эндпоинту "/cart/items" для формирования страницы корзины покупателя.
     * Запрашивает данные агрегации у сервиса и передает коллекцию элементов и общую сумму в контекст отображения.
     * </summary>
     * @param model Контекст модели Spring MVC для передачи данных в HTML-шаблон.
     * <return>
     * @return Логическое имя HTML-шаблона ("cart").
     * </return>
     **/
    @GetMapping("/cart/items")
    public String getCart(final Model model) {

        var cartPage = cartService.findCart();

        model.addAttribute("items", cartPage.items());
        model.addAttribute("total", cartPage.total());


        return "cart";
    }

    // endregion
}