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
     * <return>
     * @return Имя HTML-шаблона "orders" для рендеринга страницы журнала заказов.
     * </return>
     **/
    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.findAll()
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
     * <return>
     * @return Имя HTML-шаблона "order" для рендеринга страницы заказа.
     * </return>
     **/
    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(
            @PathVariable final long id,
            @RequestParam(defaultValue = "false") final boolean newOrder,
            Model model
    ){
        return orderService.findById(id)
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }

    /**
     * <summary>
     * Обрабатывает POST-запрос на проведение покупки содержимого текущей корзины пользователя.
     * </summary>
     * <return>
     * @return Редирект на страницу созданного заказа в случае успеха, либо возврат на страницу корзины, если та пуста.
     * </return>
     **/
    @PostMapping("/buy")
    public Mono<String> buy() {
        return purchaseService.buy()
                .map(order -> "redirect:/orders/" + order.orderId() + "?newOrder=true")
                .defaultIfEmpty("redirect:/cart/items");
    }

    // endregion
}