package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.mymarket.models.ItemModel;

/**
 * <summary>
 * Интерфейс репозитория для работы с сущностями товаров в базе данных.
 * </summary>
 **/
public interface ItemRepository extends ReactiveCrudRepository<ItemModel, Long> {

    // region Methods

    /**
     * <summary>
     * Выполняет поиск товаров, содержащих указанную поисковую подстроку в наименовании или описании, без учета регистра.
     * Возвращает результат в виде объекта страницы (Page) с учетом переданных параметров пагинации и сортировки.
     * </summary>
     * @param title Часть наименования товара для проверки совпадения.
     * @param description Часть текстового описания товара для проверки совпадения.
     * @param pageable Объект с параметрами пагинации, смещения и сортировки.
     * <return>
     * @return Страница (Page), содержащая отфильтрованные модели товаров, соответствующие критериям поиска.
     * </return>
     **/
    Flux<ItemModel> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            final String title,
            final String description,
            final Pageable pageable
    );

    // endregion
}