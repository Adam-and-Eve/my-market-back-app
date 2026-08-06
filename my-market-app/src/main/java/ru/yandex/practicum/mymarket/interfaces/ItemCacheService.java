package ru.yandex.practicum.mymarket.interfaces;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.models.ItemModel;

/**
 * <summary>
 * Контракт сервиса кэширования данных о товарах в хранилище Redis.
 * </summary>
 **/
public interface ItemCacheService {

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
    public Flux<ItemModel> findAll(Flux<ItemModel> databaseItems);

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
    public Mono<ItemModel> findById(long id, Mono<ItemModel> databaseItem);

    // endregion
}