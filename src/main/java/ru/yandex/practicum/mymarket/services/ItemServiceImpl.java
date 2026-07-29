package ru.yandex.practicum.mymarket.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.helpers.CatalogHelper;
import ru.yandex.practicum.mymarket.mappers.ItemMapper;
import ru.yandex.practicum.mymarket.repositories.ItemRepository;
import ru.yandex.practicum.mymarket.interfaces.ItemService;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.models.ItemSortEnumModel;
import ru.yandex.practicum.mymarket.viewmodels.CatalogPageViewModel;
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
     * Компонент-маппер для трансформации доменных моделей товаров в структуры интерфейса.
     **/
    private final ItemMapper itemMapper;

    /**
     * Вспомогательный компонент для валидации и нормализации параметров запроса каталога.
     **/
    private final CatalogHelper catalogHelper;

    // endregion

    // region Constructors

    public ItemServiceImpl(
            final ItemRepository itemRepository,
            final ItemMapper itemMapper,
            final CatalogHelper catalogHelper) {

        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.catalogHelper = catalogHelper;
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
    public List<ItemModel> findAll() {
        return itemRepository.findAll();
    }

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
    @Transactional(readOnly = true)
    public CatalogPageViewModel findCatalog(
            final String search,
            final String sort,
            final Integer pageNumber,
            final Integer pageSize
    ) {
        var normalizedSearch = catalogHelper.normalizeSearch(search);

        var itemSort = ItemSortEnumModel.from(sort);

        var normalizedPageNumber = catalogHelper.normalizePageNumber(pageNumber);

        var normalizedPageSize = catalogHelper.normalizePageSize(pageSize);

        var pageable = PageRequest.of(normalizedPageNumber - 1, normalizedPageSize, catalogHelper.resolveSort(itemSort));

        var page = normalizedSearch.isBlank()
                ? itemRepository.findAll(pageable)
                : itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
          normalizedSearch,
          normalizedSearch,
          pageable
        );

        return new CatalogPageViewModel(
                itemMapper.toRows(page.getContent()),
                normalizedSearch,
                itemSort.name(),
                new PagingViewModel(normalizedPageSize, normalizedPageNumber, page.hasPrevious(), page.hasNext())
        );
    }

    // endregion
}