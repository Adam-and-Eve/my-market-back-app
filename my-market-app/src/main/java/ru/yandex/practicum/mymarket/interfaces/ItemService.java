package ru.yandex.practicum.mymarket.interfaces;

import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;

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
     * @param username Имя пользователя, для которого запрашиваются данные товаров.
     * <return>
     * @return Реактивный поток Flux с доменными моделями всех существующих товаров.
     * </return>
     **/
    public Flux<ItemModel> findAll(final String username);

    /**
     * <summary>
     * Получает View-модель товара по его уникальному идентификатору с обогащением данными из корзины.
     * </summary>
     * @param username Имя пользователя для контекстного расчета состояния товара в корзине.
     * @param id Уникальный идентификатор товара.
     * <return>
     * @return Реактивный контейнер Mono с моделью представления товара.
     * </return>
     * @throws ResponseStatusException Если товар с указанным идентификатором не найден (HTTP 404).
     **/
    public Mono<ItemViewModel> findById(final String username, final long id);

    /**
     * <summary>
     * Находит чистую доменную модель товара по его идентификатору.
     * Используется для внутренних нужд других компонентов и междоменного взаимодействия.
     * </summary>
     * @param username Имя пользователя, инициирующего запрос.
     * @param id Уникальный идентификатор товара.
     * <return>
     * @return Реактивный контейнер Mono с доменной моделью товара ItemModel.
     * </return>
     * @throws ResponseStatusException Если товар с указанным идентификатором не найден (HTTP 404).
     **/
    public Mono<ItemModel> findModelById(final String username, final long id);

    /**
     * <summary>
     * Выполняет построение страницы каталога товаров на основе переданных фильтров, правил сортировки и пагинации.
     * Обеспечивает безопасную нормализацию параметров перед отправкой запроса в слой хранения данных.
     * </summary>
     * @param username Имя пользователя для связывания каталога с состоянием корзины.
     * @param search Необработанная поисковая строка для фильтрации по названию или описанию.
     * @param sort Строковое имя стратегии сортировки элементов.
     * @param pageNumber Запрашиваемый номер страницы каталога.
     * @param pageSize Желаемое количество элементов на одной странице.
     * <return>
     * @return Реактивный контейнер Mono со сформированной моделью представления страницы каталога CatalogPageViewModel.
     * </return>
     **/
    public Mono<CatalogPageViewModel> findCatalog(
            final String username,
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize);

    // endregion
}