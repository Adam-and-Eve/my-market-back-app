package ru.yandex.practicum.mymarket.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.configurations.properties.ItemCacheProperties;
import ru.yandex.practicum.mymarket.helpers.ItemCacheHelper;
import ru.yandex.practicum.mymarket.mappers.ItemCacheMapper;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemsViewModel;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

/**
 * <summary>
 * Модульные тесты для проверки бизнес-логики кэширования в ItemCacheServiceImpl.
 * </summary>
 **/
@ExtendWith(MockitoExtension.class)
public class ItemCacheServiceImplTest {

    // region Fields

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ItemCacheProperties properties;

    @Mock
    private ItemCacheMapper itemCacheMapper;

    @Mock
    private ItemCacheHelper itemCacheHelper;

    @InjectMocks
    private ItemCacheServiceImpl itemCacheService;

    // endregion

    // region Setup

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // endregion

    // region Tests

    /**
     * <summary>
     * Проверяет успешное извлечение списка всех товаров из кэша Redis без обращения к БД.
     * </summary>
     **/
    @Test
    void findAllShouldReturnItemsFromCacheWhenCacheHit() throws Exception {
        var cacheKey = "items:all";

        var cachedJson = "[{\"id\":1,\"title\":\"Товар 1\"}]";

        var cachedItemDto = new CachedItemViewModel(1L, "Товар 1", "Описание 1", "/img1.png", 1000L);

        var cachedItemsViewModel = new CachedItemsViewModel(List.of(cachedItemDto));

        var itemModel = new ItemModel(1L, "Товар 1", "Описание 1", "/img1.png", 1000L);

        Mockito.when(itemCacheHelper.resolveAllItemsKey()).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.just(cachedJson));

        Mockito.when(objectMapper.readValue(cachedJson, CachedItemsViewModel.class)).thenReturn(cachedItemsViewModel);

        Mockito.when(itemCacheMapper.toItem(cachedItemDto)).thenReturn(itemModel);

        var databaseItems = Flux.<ItemModel>empty();

        StepVerifier.create(itemCacheService.findAll(databaseItems))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(valueOperations, Mockito.times(1)).get(cacheKey);

        Mockito.verify(valueOperations, Mockito.never())
                .set(Mockito.anyString(), Mockito.anyString(), Mockito.any(Duration.class));
    }

    /**
     * <summary>
     * Проверяет ленивую загрузку списка товаров из БД и их сохранение в Redis при промахе кэша.
     * </summary>
     **/
    @Test
    void findAllShouldLoadFromDatabaseAndSaveToCacheWhenCacheMiss() throws Exception {
        var cacheKey = "items:all";

        var ttl = Duration.ofMinutes(10);

        var jsonResult = "[{\"id\":1,\"title\":\"Товар из БД\"}]";

        var itemModel = new ItemModel(1L, "Товар из БД", "Описание", "/img.png", 500L);

        var databaseItems = Flux.just(itemModel);

        var cachedItemDto = new CachedItemViewModel(1L, "Товар из БД", "Описание", "/img.png", 500L);

        var cachedItemsViewModel = new CachedItemsViewModel(List.of(cachedItemDto));

        Mockito.when(itemCacheHelper.resolveAllItemsKey()).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.empty());

        Mockito.when(properties.ttl()).thenReturn(ttl);

        Mockito.when(itemCacheMapper.toCachedItems(List.of(itemModel))).thenReturn(cachedItemsViewModel);

        Mockito.when(objectMapper.writeValueAsString(cachedItemsViewModel)).thenReturn(jsonResult);

        Mockito.when(valueOperations.set(cacheKey, jsonResult, ttl)).thenReturn(Mono.just(true));

        StepVerifier.create(itemCacheService.findAll(databaseItems))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(valueOperations, Mockito.times(1)).set(cacheKey, jsonResult, ttl);
    }

    /**
     * <summary>
     * Проверяет отказоустойчивость: при ошибке доступа к Redis метод возвращает данные из БД.
     * </summary>
     **/
    @Test
    void findAllShouldFallbackToDatabaseWhenCacheThrowsError() {
        var cacheKey = "items:all";

        var itemModel = new ItemModel(1L, "Товар", "Описание", "/img.png", 100L);

        var databaseItems = Flux.just(itemModel);

        Mockito.when(itemCacheHelper.resolveAllItemsKey()).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.error(new RuntimeException("Redis connection refused")));

        StepVerifier.create(itemCacheService.findAll(databaseItems))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(itemCacheMapper, Mockito.never()).toCachedItems(Mockito.any());
    }

    // endregion

    // region Tests for findById

    /**
     * <summary>
     * Проверяет получение карточки товара по ID из кэша Redis при попадании.
     * </summary>
     **/
    @Test
    void findByIdShouldReturnItemFromCacheWhenCacheHit() throws Exception {
        var itemId = 1L;

        var cacheKey = "items:card:1";

        var cachedJson = "{\"id\":1,\"title\":\"Товар 1\"}";

        var cachedItemDto = new CachedItemViewModel(itemId, "Товар 1", "Описание 1", "/img1.png", 1000L);

        var itemModel = new ItemModel(itemId, "Товар 1", "Описание 1", "/img1.png", 1000L);

        Mockito.when(itemCacheHelper.resolveItemKey(itemId)).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.just(cachedJson));

        Mockito.when(objectMapper.readValue(cachedJson, CachedItemViewModel.class)).thenReturn(cachedItemDto);

        Mockito.when(itemCacheMapper.toItem(cachedItemDto)).thenReturn(itemModel);

        var databaseItem = Mono.<ItemModel>empty();

        StepVerifier.create(itemCacheService.findById(itemId, databaseItem))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(valueOperations, Mockito.times(1)).get(cacheKey);

        Mockito.verify(valueOperations, Mockito.never())
                .set(Mockito.anyString(), Mockito.anyString(), Mockito.any(Duration.class));
    }

    /**
     * <summary>
     * Проверяет обращение к БД и запись карточки товара в Redis при промахе кэша.
     * </summary>
     **/
    @Test
    void findByIdShouldLoadFromDatabaseAndSaveToCacheWhenCacheMiss() throws Exception {
        var itemId = 2L;

        var cacheKey = "items:card:2";

        var ttl = Duration.ofMinutes(5);

        var jsonResult = "{\"id\":2,\"title\":\"Товар из БД\"}";

        var itemModel = new ItemModel(itemId, "Товар из БД", "Описание", "/img2.png", 2000L);

        var databaseItem = Mono.just(itemModel);

        var cachedItemDto = new CachedItemViewModel(itemId, "Товар из БД", "Описание", "/img2.png", 2000L);

        Mockito.when(itemCacheHelper.resolveItemKey(itemId)).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.empty());

        Mockito.when(properties.ttl()).thenReturn(ttl);

        Mockito.when(itemCacheMapper.toCachedItem(itemModel)).thenReturn(cachedItemDto);

        Mockito.when(objectMapper.writeValueAsString(cachedItemDto)).thenReturn(jsonResult);

        Mockito.when(valueOperations.set(cacheKey, jsonResult, ttl)).thenReturn(Mono.just(true));

        StepVerifier.create(itemCacheService.findById(itemId, databaseItem))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(valueOperations, Mockito.times(1)).set(cacheKey, jsonResult, ttl);
    }

    /**
     * <summary>
     * Проверяет прозрачный фоллбэк на БД при сбое вызова Redis в findById.
     * </summary>
     **/
    @Test
    void findByIdShouldFallbackToDatabaseWhenCacheThrowsError() {
        var itemId = 3L;

        var cacheKey = "items:card:3";

        var itemModel = new ItemModel(itemId, "Резервный товар", "Описание", "/img3.png", 3000L);

        var databaseItem = Mono.just(itemModel);

        Mockito.when(itemCacheHelper.resolveItemKey(itemId)).thenReturn(cacheKey);

        Mockito.when(valueOperations.get(cacheKey)).thenReturn(Mono.error(new RuntimeException("Redis timeout")));

        StepVerifier.create(itemCacheService.findById(itemId, databaseItem))
                .expectNext(itemModel)
                .verifyComplete();

        Mockito.verify(itemCacheMapper, Mockito.never()).toCachedItem(Mockito.any());
    }

    // endregion
}