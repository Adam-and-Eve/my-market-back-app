package ru.yandex.practicum.mymarket.controllers;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.models.CartActionEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CartItemFormViewModel;

import java.security.Principal;

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
     * Обрабатывает GET-запросы к эндпоинту "/cart/items" для формирования страницы корзины.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * @param model Контекст модели Spring MVC для передачи данных в HTML-шаблон.
     * <return>
     * @return Реактивный контейнер Mono с логическим именем HTML-шаблона ("cart").
     * </return>
     **/
    @GetMapping("/cart/items")
    public Mono<String> getCart(
            final Principal principal,
            final Model model) {

        return cartService.findCart(username(principal)).doOnNext(cartPage -> {
            model.addAttribute("items", cartPage.items());
            model.addAttribute("total", cartPage.total());
            model.addAttribute("paymentAvailable", cartPage.paymentAvailable());
            model.addAttribute("balance", cartPage.balance());
            model.addAttribute("purchaseAvailable", cartPage.purchaseAvailable());
            model.addAttribute("paymentMessage", cartPage.paymentMessage());
        }).thenReturn("cart");
    }

    /**
     * <summary>
     * Обрабатывает POST-запросы со страницы корзины для изменения количества позиций товара или их удаления.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * @param form Валидированная модель представления формы изменения состава корзины.
     * @param model Контекст модели Spring MVC для передачи данных в HTML-шаблон.
     * <return>
     * @return Реактивный контейнер Mono со строкой перенаправления (redirect) на страницу корзины.
     * </return>
     **/
    @PostMapping("/cart/items")
    public Mono<String> updateCartItem(
            final Principal principal,
            @Valid @ModelAttribute final CartItemFormViewModel form,
            final Model model){

        return cartService.updateItemCount(username(principal), form.getId(), form.getAction())
                .thenReturn("redirect:/cart/items");
    }

    /**
     * <summary>
     * Извлекает имя пользователя из объекта аутентификации Principal.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя.
     * <return>
     * @return Имя пользователя или null, если объект Principal отсутствует.
     * </return>
     **/
    private String username(final Principal principal) {
        return principal == null ? null : principal.getName();
    }

    // endregion
}