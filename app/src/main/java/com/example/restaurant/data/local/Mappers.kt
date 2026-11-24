package com.example.restaurant.data.local

import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.model.Order
import com.example.restaurant.domain.model.OrderItem

/** Сущность БД → доменная модель. */
fun DishEntity.toDomain() = Dish(id, name, description, price, category, remoteId)

/** Доменная модель → сущность БД. */
fun Dish.toEntity() = DishEntity(id, name, description, price, category, remoteId)

/** Строка JOIN-запроса → строка корзины. */
fun CartRow.toDomain() = CartLine(dishId, name, price, quantity)

/** Заказ с позициями → доменная модель. */
fun OrderWithItems.toDomain() = Order(
    id = order.id,
    createdAt = order.createdAt,
    tableNumber = order.tableNumber,
    total = order.total,
    items = items.map { OrderItem(it.name, it.price, it.quantity) }
)
