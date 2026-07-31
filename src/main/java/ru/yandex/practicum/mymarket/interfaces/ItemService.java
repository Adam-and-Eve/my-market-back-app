package ru.yandex.practicum.mymarket.interfaces;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

import java.util.List;

/**
 * <summary>
 * Контракт сервиса для управления каталогом товаров.
 * </summary>
 **/
public interface ItemService {

    // region Methods

    /**
     * <summary>
     * Извлекает полный нефильтрованный список всех товаров из базы данных.
     * </summary>
     * <return>
     * @return Список доменных моделей всех существующих товаров.
     * </return>
     **/
    public Flux<ItemModel> findAll();

    /**
     * <summary>
     * Получает View-модель товара по его уникальному идентификатору с обогащением данными из корзины.
     * </summary>
     * @param id Уникальный идентификатор товара.
     * <return>
     * @return Модель представления товара с актуальным количеством в корзине текущего пользователя.
     * </return>
     * @throws ResponseStatusException Если товар с указанным идентификатором не найден (HTTP 404).
     **/
    public Mono<ItemViewModel> findById(final long id);

    /**
     * <summary>
     * Находит чистую доменную модель товара по его идентификатору.
     * Используется для внутренних нужд других компонентов и междоменного взаимодействия.
     * </summary>
     * @param id Уникальный идентификатор товара.
     * <return>
     * @return Доменная модель товара ItemModel.
     * </return>
     * @throws ResponseStatusException Если товар с указанным идентификатором не найден (HTTP 404).
     **/
    public Mono<ItemModel> findModelById(final long id);

    /**
     * <summary>
     * Выполняет построение страницы каталога товаров на основе переданных фильтров, правил сортировки и пагинации.
     * Обеспечивает безопасную нормализацию параметров перед отправкой запроса в слой хранения данных.
     * </summary>
     * @param search Необработанная поисковая строка для фильтрации по названию или описанию.
     * @param sort Строковое имя стратегии сортировки элементов.
     * @param pageNumber Запрашиваемый номер страницы каталога.
     * @param pageSize Желаемое количество элементов на одной странице.
     * <return>
     * @return Сформированная модель представления страницы каталога с сеткой товаров и метаданными навигации.
     * </return>
     **/
    public Mono<CatalogPageViewModel> findCatalog(
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize);

    // endregion
}