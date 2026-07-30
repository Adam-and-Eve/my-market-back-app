package ru.yandex.practicum.mymarket.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.interfaces.OrderService;

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

    // endregion

    // region Constructors

    public OrderController(final OrderService orderService) {

        this.orderService = orderService;
    }

    // endregion

    // region Actions

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
    public String getOrder(
            @PathVariable final long id,
            @RequestParam(defaultValue = "false") final boolean newOrder,
            Model model
    ){
        var order = orderService.findById(id);

        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);

        return "order";
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
    public String buy() {
        var orderId = orderService.buy();

        if (orderId == -1) {
            return "redirect:/cart/items";
        }

        return "redirect:/orders/" + orderId + "?newOrder=true";
    }

    // endregion
}