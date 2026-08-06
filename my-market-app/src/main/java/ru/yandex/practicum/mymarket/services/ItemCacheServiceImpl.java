package ru.yandex.practicum.mymarket.services;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.configurations.properties.ItemCacheProperties;
import ru.yandex.practicum.mymarket.helpers.ItemCacheHelper;
import ru.yandex.practicum.mymarket.interfaces.ItemCacheService;
import ru.yandex.practicum.mymarket.mappers.ItemCacheMapper;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemsViewModel;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * <summary>
 * Сервис кэширования данных о товарах в хранилище Redis.
 * </summary>
 **/
@Service
public class ItemCacheServiceImpl implements ItemCacheService {

    // region Fields

    /**
     * Реактивный шаблон для выполнения операций взаимодействия со строковыми значениями в Redis.
     **/
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * Компонент для высокопроизводительной сериализации и десериализации объектов в JSON-строки.
     **/
    private final ObjectMapper objectMapper;

    /**
     * Конфигурационные свойства кэширования товаров, включая параметры TTL.
     **/
    private final ItemCacheProperties properties;

    /**
     * Компонент для конвертации данных между доменными сущностями и кэша.
     **/
    private final ItemCacheMapper itemCacheMapper;

    /**
     * Вспомогательный компонент для разрешения и генерации ключей кэша.
     **/
    private final ItemCacheHelper itemCacheHelper;

    // endregion

    // region Constructors

    public ItemCacheServiceImpl(
            final ReactiveRedisTemplate<String, String> redisTemplate,
            final ObjectMapper objectMapper,
            final ItemCacheProperties properties,
            final ItemCacheMapper itemCacheMapper,
            final ItemCacheHelper itemCacheHelper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.itemCacheMapper = itemCacheMapper;
        this.itemCacheHelper = itemCacheHelper;
    }

    // endregion

    // region Methods

    /**
     * <summary>
     * Возвращает полный список товаров, пытаясь извлечь его из кэша Redis.
     * При промахе кэша выполняется ленивая загрузка из базы данных с последующим сохранением структуры в кэш.
     * </summary>
     * @param databaseItems Реактивный поток товаров из базы данных на случай отсутствия ключа в кэше.
     * <return>
     * @return Реактивный поток доменных моделей товаров Flux<ItemModel>.
     * </return>
     **/
    public Flux<ItemModel> findAll(Flux<ItemModel> databaseItems) {
        return redisTemplate.opsForValue()
                .get(itemCacheHelper.resolveAllItemsKey())
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, CachedItemsViewModel.class)))
                .flatMapMany(cached -> Flux.fromIterable(cached.items()))
                .map(itemCacheMapper::toItem)
                .switchIfEmpty(Flux.defer(() -> loadAll(databaseItems)))
                .onErrorResume(error -> databaseItems);
    }

    /**
     * <summary>
     * Возвращает карточку товара по его уникальному идентификатору, проверяя наличие в кэше.
     * Если товар отсутствует в Redis, конвейер лениво обращается к БД и кэширует полученный результат.
     * </summary>
     * @param id Уникальный идентификатор искомого товара.
     * @param databaseItem Источник данных карточки товара из БД на случай промаха кэша.
     * <return>
     * @return Реактивный контейнер Mono<ItemModel> с найденным и восстановленным товаром.
     * </return>
     **/
    public Mono<ItemModel> findById(long id, Mono<ItemModel> databaseItem) {
        return redisTemplate.opsForValue()
                .get(itemCacheHelper.resolveItemKey(id))
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, CachedItemViewModel.class)))
                .map(itemCacheMapper::toItem)
                .switchIfEmpty(Mono.defer(() -> loadOne(id, databaseItem)))
                .onErrorResume(error -> databaseItem);
    }

    /**
     * <summary>
     * Вспомогательный метод для агрегации реактивного потока товаров из БД, их сериализации и сохранения в кэш.
     * </summary>
     * @param databaseItems Поток доменных моделей из базы данных.
     * <return>
     * @return Реактивный поток сохраненных доменных моделей товаров Flux<ItemModel>.
     * </return>
     **/
    private Flux<ItemModel> loadAll(Flux<ItemModel> databaseItems) {
        return databaseItems.collectList()
                .flatMap(items -> Mono.fromCallable(() -> objectMapper.writeValueAsString(itemCacheMapper.toCachedItems(items)))
                        .flatMap(json -> redisTemplate.opsForValue().set(itemCacheHelper.resolveAllItemsKey(), json, properties.ttl()))
                        .onErrorReturn(false)
                        .thenReturn(items))
                .flatMapMany(Flux::fromIterable);
    }

    /**
     * <summary>
     * Вспомогательный метод для ленивой загрузки одной сущности товара, преобразования в JSON и записи в Redis.
     * </summary>
     * @param id Уникальный идентификатор кэшируемого товара.
     * @param databaseItem Реактивный источник карточки товара из БД.
     * <return>
     * @return Реактивный контейнер Mono<ItemModel> с сохраненной моделью товара.
     * </return>
     **/
    private Mono<ItemModel> loadOne(long id, Mono<ItemModel> databaseItem) {
        return databaseItem.flatMap(item -> Mono.fromCallable(() -> objectMapper.writeValueAsString(itemCacheMapper.toCachedItem(item)))
                .flatMap(json -> redisTemplate.opsForValue().set(itemCacheHelper.resolveItemKey(id), json, properties.ttl()))
                .onErrorReturn(false)
                .thenReturn(item));
    }

    // endregion
}