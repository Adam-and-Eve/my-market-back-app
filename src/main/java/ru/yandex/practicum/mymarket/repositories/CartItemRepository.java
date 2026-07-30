package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.models.CartItemModel;

import java.util.List;
import java.util.Optional;

/**
 * <summary>
 * Интерфейс репозитория для управления доменными моделями элементов корзины в базе данных.
 * Расширяет JpaRepository для реализации базовых CRUD-операций над сущностями CartItemModel.
 * </summary>
 **/
public interface CartItemRepository extends JpaRepository<CartItemModel, Long> {

    // region Methods

    /**
     * <summary>
     * Выполняет поиск элемента корзины по уникальному идентификатору связанного с ним товара.
     * </summary>
     * @param itemId Уникальный идентификатор товара.
     * <return>
     * @return Контейнер Optional, содержащий сущность элемента корзины, если она найдена, иначе Optional.empty().
     * </return>
     **/
    Optional<CartItemModel> findByItemId(final long itemId);

    /**
     * <summary>
     * Выполняет пакетную выборку элементов корзины для переданного списка идентификаторов товаров.
     * Используется для оптимизации запросов и предотвращения проблемы N+1 при пагинации каталога.
     * </summary>
     * @param itemIds Коллекция идентификаторов интересующих товаров.
     * <return>
     * @return Список доменных моделей элементов корзины, соответствующих переданным идентификаторам.
     * </return>
     **/
    List<CartItemModel> findAllByItemIdIn(List<Long> itemIds);

    /**
     * <summary>
     * Выполняет выборку всех элементов корзины с сортировкой по возрастанию идентификатора товара.
     * Используется для обеспечения стабильного порядка отображения позиций в UI при изменении их количества.
     * </summary>
     * <return>
     * @return Список доменных моделей всех элементов корзины, упорядоченных по ID товара.
     * </return>
     **/
    List<CartItemModel> findAllByOrderByItemIdAsc();

    // endregion
}