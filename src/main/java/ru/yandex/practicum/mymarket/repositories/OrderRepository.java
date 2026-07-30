package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.models.OrderModel;
import ru.yandex.practicum.mymarket.viewmodels.OrderViewModel;

import java.util.List;

/**
 * <summary>
 * Интерфейс репозитория для выполнения операций CRUD и управления персистентным состоянием доменных моделей заказов OrderModel.
 * </summary>
 **/
public interface OrderRepository extends JpaRepository<OrderModel, Long> {

    // region Methods

    /**
     * <summary>
     * Извлекает все заказы из базы данных, сортируя их по возрастанию идентификатора)
     * </summary>
     * <return>
     * @return Список доменных сущностей заказов OrderModel.
     * </return>
     **/
    public List<OrderModel> findAllByOrderByIdAsc();

    // endregion
}