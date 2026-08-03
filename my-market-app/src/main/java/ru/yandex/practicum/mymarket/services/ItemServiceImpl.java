package ru.yandex.practicum.mymarket.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.helpers.CatalogHelper;
import ru.yandex.practicum.mymarket.interfaces.CartService;
import ru.yandex.practicum.mymarket.interfaces.ItemCacheService;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;
import ru.yandex.practicum.mymarket.viewmodels.ItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.PagingViewModel;

import java.util.List;

/**
 * <summary>
 * Сервис для управления каталогом товаров.
 * </summary>
 **/
@Service
public class ItemServiceImpl implements ItemService {

    // region Fields

    /**
     * Репозиторий доступа к данным для выполнения операций с сущностями товаров в БД.
     **/
    private final ItemRepository itemRepository;

    /**
     * Сервис для работы с корзиной.
     **/
    private final CartService cartService;

    /**
     * Компонент-маппер для трансформации доменных моделей товаров в структуры интерфейса.
     **/
    private final ItemMapper itemMapper;

    /**
     * Вспомогательный компонент для валидации и нормализации параметров запроса каталога.
     **/
    private final CatalogHelper catalogHelper;

    private final ItemCacheService itemCacheService;

    // endregion

    // region Constructors

    public ItemServiceImpl(
            final ItemRepository itemRepository,
            final CartService cartService,
            final ItemMapper itemMapper,
            final CatalogHelper catalogHelper,
            final ItemCacheService itemCacheService) {

        this.itemRepository = itemRepository;
        this.cartService = cartService;
        this.itemMapper = itemMapper;
        this.catalogHelper = catalogHelper;
        this.itemCacheService = itemCacheService;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Извлекает полный нефильтрованный список всех товаров из базы данных.
     * </summary>
     * <return>
     * @return Список доменных моделей всех существующих товаров.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Flux<ItemModel> findAll() {
        return itemCacheService.findAll(itemRepository.findAll());
    }

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
    @Transactional(readOnly = true)
    @Override
    public Mono<ItemViewModel> findById(final long id) {
        return itemCacheService.findById(id, itemRepository.findById(id))
                .flatMap(item -> cartService.findCountForItem(item.getId())
                        .map(count -> itemMapper.toViewModel(item, count)))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found.")));

    }

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
    @Transactional(readOnly = true)
    @Override
    public Mono<ItemModel> findModelById(final long id) {
        return itemCacheService.findById(id, itemRepository.findById(id))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found.")));
    }

    /**
     * <summary>
     * Выполняет построение страницы каталога товаров на основе переданных фильтров, правил сортировки и пагинации.
     * Запросы на фильтрацию, сортировку и выборку нужного окна данных делегируются на уровень базы данных.
     * </summary>
     * @param search Необработанная поисковая строка для фильтрации по названию или описанию.
     * @param sort Строковое имя стратегии сортировки элементов.
     * @param pageNumber Запрашиваемый номер страницы каталога.
     * @param pageSize Желаемое количество элементов на одной странице.
     * <return>
     * @return Сформированная модель представления страницы каталога с сеткой товаров и метаданными навигации.
     * </return>
     **/
    @Transactional(readOnly = true)
    @Override
    public Mono<CatalogPageViewModel> findCatalog(
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize
    ) {
        var normalizedSearch = catalogHelper.normalizeSearch(search);

        var itemSort = ItemSortEnumModel.from(sort);

        var normalizedPageNumber = catalogHelper.normalizePageNumber(pageNumber);

        var normalizedPageSize = catalogHelper.normalizePageSize(pageSize);


        var springSort = catalogHelper.resolveSort(itemSort);

        var pageable = PageRequest.of(normalizedPageNumber - 1, normalizedPageSize, springSort);

        var itemsMono = itemRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch, pageable)
                .collectList();

        var countMono = itemRepository
                .countByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedSearch, normalizedSearch);

        return Mono.zip(itemsMono, countMono)
                .flatMap(tuple -> {
                    var pageItems = tuple.getT1();
                    var totalCount = tuple.getT2();

                    return buildCatalogPage(
                            pageItems,
                            totalCount,
                            normalizedSearch,
                            itemSort,
                            normalizedPageNumber,
                            normalizedPageSize
                    );
                });
    }

    /**
     * <summary>
     * Обогащает отфильтрованную страницу товаров данными о количестве в корзине текущего пользователя
     * и собирает итоговую View-модель страницы каталога.
     * </summary>
     * @param pageItems Список моделей товаров, полученный из БД для текущей страницы.
     * @param totalCount Общее количество товаров в БД, удовлетворяющих критериям поиска.
     * @param search Нормализованная поисковая строка, использованная при фильтрации.
     * @param sort Примененная стратегия сортировки элементов каталога.
     * @param pageNumber Номер текущей отображаемой страницы.
     * @param pageSize Количество элементов, отображаемых на одной странице.
     * <return>
     * @return Реактивный контейнер Mono с заполненной моделью представления страницы каталога.
     * </return>
     **/
    private Mono<CatalogPageViewModel> buildCatalogPage(
            final List<ItemModel> pageItems,
            final long totalCount,
            final String search,
            final ItemSortEnumModel sort,
            final int pageNumber,
            final int pageSize
    ){
        var hasPrevious = pageNumber > 1;

        var hasNext = ((long) pageNumber * pageSize) < totalCount;

        var itemIds = pageItems.stream().map(ItemModel::getId).toList();

        return cartService.findCountsForItems(itemIds)
                .map(counts -> new CatalogPageViewModel(
                        itemMapper.toRows(pageItems, counts),
                        search,
                        sort.name(),
                        new PagingViewModel(
                                pageSize,
                                pageNumber,
                                hasPrevious,
                                hasNext
                        )
                ));
    }

    // endregion
}