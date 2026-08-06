package ru.yandex.practicum.mymarket.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.models.ItemModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemViewModel;
import ru.yandex.practicum.mymarket.viewmodels.CachedItemsViewModel;

import java.util.List;

/**
 * <summary>
 * Компонент-маппер для преобразования моделей данных товаров в модели для кэширования и обратно.
 * </summary>
 **/
@Component
public class ItemCacheMapper {

    // region Methods

    /**
     * <summary>
     * Преобразует плоский список доменных моделей товаров в агрегированную модель представления для пакетного сохранения.
     * </summary>
     * @param items Список доменных моделей исходных товаров.
     * <return>
     * @return Объект CachedItemsViewModel, содержащий список DTO-объектов.
     * </return>
     **/
    public CachedItemsViewModel toCachedItems(final List<ItemModel> items) {
        if (items == null) {
            return new CachedItemsViewModel(List.of());
        }

        return new CachedItemsViewModel(items.stream()
                .map(this::toCachedItem)
                .toList());
    }

    /**
     * <summary>
     * Преобразует одиночную доменную модель товара в плоский неизменяемый DTO (Record) для сериализации в кэш.
     * </summary>
     * @param item Доменная модель исходного товара.
     * <return>
     * @return Сконвертированная модель представления CachedItemViewModel.
     * </return>
     **/
    public CachedItemViewModel toCachedItem(final ItemModel item) {
        return new CachedItemViewModel(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice()
        );
    }

    /**
     * <summary>
     * Восстанавливает доменную модель товара из десериализованного объекта модели представления кэша.
     * </summary>
     * @param item Модель представления товара, извлеченная из Redis.
     * <return>
     * @return Сконструированная чистая доменная модель ItemModel.
     * </return>
     **/
    public ItemModel toItem(final CachedItemViewModel item) {
        return new ItemModel(
                item.id(),
                item.title(),
                item.description(),
                item.imgPath(),
                item.price()
        );
    }

    // endregion
}