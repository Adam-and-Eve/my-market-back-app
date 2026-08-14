package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.interfaces.OrderService;
import ru.yandex.practicum.mymarket.interfaces.PurchaseService;

import java.security.Principal;

/**
 * <summary>
 * Веб-контроллер, обрабатывающий пользовательские HTTP-запросы для взаимодействия с заказами.
 * </summary>
 **/
@Controller
public class OrderController {

    // region Fields

    /**
     * Сервис для управления бизнес-логикой создания и получения заказов.
     **/
    private final OrderService orderService;

    /**
     * Сервис для обработки транзакций покупки и проведения оплаты.
     **/
    private final PurchaseService purchaseService;

    // endregion

    // region Constructors

    public OrderController(
            final OrderService orderService,
            final PurchaseService purchaseService) {

        this.orderService = orderService;
        this.purchaseService = purchaseService;
    }

    // endregion

    // region Actions

    /**
     * <summary>
     * Обрабатывает GET-запрос на получение и отображение страницы со списком всех оформленных заказов.
     * </summary>
     * @param model Контейнер Spring MVC для передачи коллекции заказов в шаблон отображения.
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * <return>
     * @return Реактивный контейнер Mono с логическим именем HTML-шаблона ("orders").
     * </return>
     **/
    @GetMapping("/orders")
    public Mono<String> getOrders(
            final Model model,
            final Principal principal) {
        return orderService.findAll(username(principal))
                .collectList()
                .doOnNext(orders -> {
                    model.addAttribute("orders", orders);
                })
                .thenReturn("orders");
    }

    /**
     * <summary>
     * Обрабатывает GET-запрос на получение и отображение страницы конкретного заказа по его идентификатору.
     * </summary>
     * @param id Уникальный идентификатор запрашиваемого заказа.
     * @param newOrder Флаг, указывающий, является ли этот заказ только что оформленным (для вывода приветственного сообщения).
     * @param model Контейнер Spring MVC для передачи атрибутов в шаблон отображения.
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * <return>
     * @return Реактивный контейнер Mono с логическим именем HTML-шаблона ("order").
     * </return>
     **/
    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable final long id,
            @RequestParam(defaultValue = "false") final boolean newOrder,
            final Model model,
            final Principal principal
    ){
        return orderService.findById(username(principal), id)
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }

    /**
     * <summary>
     * Обрабатывает POST-запрос на проведение покупки содержимого текущей корзины пользователя.
     * Анализирует результат выполнения покупки (успех, пустая корзина или отказ оплаты)
     * и осуществляет соответствующий редирект.
     * </summary>
     * @param principal Объект текущего аутентифицированного пользователя Spring Security.
     * <return>
     * @return Реактивный контейнер Mono со строкой перенаправления (redirect) на страницу заказа или корзины.
     * </return>
     **/
    @PostMapping("/buy")
    public Mono<String> buy(final Principal principal) {
        return purchaseService.buy(username(principal))
                .map(result -> {
                    if (result.success()) {
                        return "redirect:/orders/" + result.orderId() + "?newOrder=true";
                    }

                    if (result.emptyCart()) {
                        return "redirect:/cart/items";
                    }

                    return "redirect:/cart/items?paymentError=true";
                })
                .defaultIfEmpty("redirect:/cart/items");
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
    private String username(Principal principal) {
        return principal == null ? null : principal.getName();
    }

    // endregion
}