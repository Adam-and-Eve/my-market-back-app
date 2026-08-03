package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;

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
    public Mono<String> getCart(final Model model) {
        return cartService.findCart().doOnNext(cartPage -> {
            model.addAttribute("items", cartPage.items());
            model.addAttribute("total", cartPage.total());
        }).thenReturn("cart");
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы со страницы корзины для изменения количества выбранного товара.
     * </summary>
     * @param id Уникальный идентификатор изменяемого товара.
     * @param action Тип действия над позицией (PLUS, MINUS, DELETE).
     * <return>
     * @return Строка перенаправления (redirect) на GET-метод отображения корзины.
     * </return>
     **/
    @PostMapping("/cart/items")
    public Mono<String> updateCartItem(
            @RequestParam final long id,
            @RequestParam final CartActionEnumModel action,
            Model model){

        return cartService.updateItemCount(id, action)
                .thenReturn("redirect:/cart/items");
    }

    // endregion
}