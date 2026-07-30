package ru.yandex.practicum.mymarket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.models.OrderModel;

/**
 * <summary>
 * Интерфейс репозитория для выполнения операций CRUD и управления персистентным состоянием доменных моделей заказов OrderModel.
 * Обеспечивает абстракцию над слоем доступа к данным базы данных с использованием механизмов Spring Data JPA.
 * </summary>
 **/
public interface OrderRepository extends JpaRepository<OrderModel, Long> {

}