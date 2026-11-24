package com.example.restaurant.domain.model

/** Категории меню. Используются и для фильтра, и при импорте блюд с сервера. */
object Categories {
    const val STARTERS = "Закуски"
    const val MAINS = "Основные блюда"
    const val DESSERTS = "Десерты"
    const val DRINKS = "Напитки"

    /** Все категории в порядке отображения. */
    val all: List<String> = listOf(STARTERS, MAINS, DESSERTS, DRINKS)
}

/**
 * Блюдо меню.
 *
 * @property id локальный идентификатор; `0` — блюдо ещё не сохранено.
 * @property price цена в рублях.
 * @property remoteId идентификатор на сервере; `null` для блюд, добавленных локально.
 */
data class Dish(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val price: Int,
    val category: String,
    val remoteId: Int? = null
)

/** Строка корзины: блюдо и количество порций. */
data class CartLine(
    val dishId: Long,
    val name: String,
    val price: Int,
    val quantity: Int
) {
    /** Стоимость строки. */
    val sum: Int get() = price * quantity
}

/** Позиция оформленного заказа (название и цена фиксируются на момент заказа). */
data class OrderItem(val name: String, val price: Int, val quantity: Int)

/**
 * Оформленный заказ.
 *
 * @property createdAt время оформления в миллисекундах (Unix time).
 * @property tableNumber номер столика.
 * @property total итоговая сумма.
 */
data class Order(
    val id: Long,
    val createdAt: Long,
    val tableNumber: Int,
    val total: Int,
    val items: List<OrderItem>
)

/** Результат оформления заказа. */
enum class PlaceOrderResult { SUCCESS, EMPTY_CART, INVALID_TABLE }

/** Результат сохранения блюда. */
enum class SaveDishResult { SUCCESS, INVALID_NAME, INVALID_PRICE }
