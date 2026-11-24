package com.example.restaurant.domain.repository

import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.model.Order
import kotlinx.coroutines.flow.Flow

/** Доступ к меню (блюдам). Реализация — в слое data (Room + Retrofit). */
interface MenuRepository {

    /**
     * Реактивный список блюд, название которых содержит [query].
     * Пустая [category] означает «все категории».
     */
    fun observeDishes(query: String, category: String): Flow<List<Dish>>

    /** Блюдо по [id] или `null`. */
    suspend fun getDish(id: Long): Dish?

    /** Вставляет блюдо (`id == 0`) или обновляет существующее. */
    suspend fun saveDish(dish: Dish)

    /** Удаляет блюдо; связанные строки корзины удаляются каскадно. */
    suspend fun deleteDish(id: Long)

    /** Подгружает блюда с сервера. @return число добавленных блюд или ошибка сети. */
    suspend fun refreshFromNetwork(): Result<Int>
}

/** Доступ к корзине. */
interface CartRepository {
    /** Реактивное содержимое корзины. */
    fun observeCart(): Flow<List<CartLine>>

    /** Добавляет одну порцию блюда [dishId]. */
    suspend fun add(dishId: Long)

    /** Убирает одну порцию блюда [dishId]; при нуле строка удаляется. */
    suspend fun decrement(dishId: Long)
}

/** Доступ к заказам. */
interface OrderRepository {
    /** Реактивный список заказов, новые — первыми. */
    fun observeOrders(): Flow<List<Order>>

    /**
     * Превращает содержимое корзины в заказ для столика [tableNumber] и очищает корзину
     * (одной транзакцией). @return id заказа или `null`, если корзина пуста.
     */
    suspend fun placeOrder(tableNumber: Int): Long?

    /** Удаляет заказ. */
    suspend fun deleteOrder(id: Long)
}
