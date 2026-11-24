package com.example.restaurant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO блюд: полный набор CRUD-операций. */
@Dao
interface DishDao {

    /** Блюда с названием, содержащим [query]; пустая [category] отключает фильтр по категории. */
    @Query(
        "SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' " +
            "AND (:category = '' OR category = :category) ORDER BY category, name"
    )
    fun observe(query: String, category: String): Flow<List<DishEntity>>

    /** Блюдо по идентификатору. */
    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getById(id: Long): DishEntity?

    /** Вставка блюда. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DishEntity): Long

    /** Вставка списка; блюда с уже существующим `remoteId` пропускаются (результат `-1`). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<DishEntity>): List<Long>

    /** Обновление блюда. */
    @Update
    suspend fun update(entity: DishEntity)

    /** Удаление блюда. */
    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/** DAO корзины. Составные операции выполняются в транзакции. */
@Dao
abstract class CartDao {

    /** Содержимое корзины вместе с названиями и ценами блюд. */
    @Query(
        "SELECT c.dishId AS dishId, d.name AS name, d.price AS price, c.quantity AS quantity " +
            "FROM cart c INNER JOIN dishes d ON d.id = c.dishId ORDER BY d.name"
    )
    abstract fun observeRows(): Flow<List<CartRow>>

    /** Разовый снимок корзины (для оформления заказа). */
    @Query(
        "SELECT c.dishId AS dishId, d.name AS name, d.price AS price, c.quantity AS quantity " +
            "FROM cart c INNER JOIN dishes d ON d.id = c.dishId"
    )
    abstract suspend fun snapshot(): List<CartRow>

    @Query("SELECT quantity FROM cart WHERE dishId = :dishId")
    abstract suspend fun getQuantity(dishId: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: CartEntity)

    @Query("DELETE FROM cart WHERE dishId = :dishId")
    abstract suspend fun deleteLine(dishId: Long)

    /** Очистка корзины. */
    @Query("DELETE FROM cart")
    abstract suspend fun clear()

    /** Увеличивает количество порций блюда на 1. */
    @Transaction
    open suspend fun increment(dishId: Long) {
        upsert(CartEntity(dishId, (getQuantity(dishId) ?: 0) + 1))
    }

    /** Уменьшает количество порций на 1; строка с нулём удаляется. */
    @Transaction
    open suspend fun decrement(dishId: Long) {
        val quantity = getQuantity(dishId) ?: return
        if (quantity <= 1) deleteLine(dishId) else upsert(CartEntity(dishId, quantity - 1))
    }
}

/** DAO заказов. */
@Dao
interface OrderDao {

    /** Все заказы с позициями, новые — первыми. */
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC, id DESC")
    fun observeOrders(): Flow<List<OrderWithItems>>

    /** Вставка шапки заказа. @return id заказа. */
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    /** Вставка позиций заказа. */
    @Insert
    suspend fun insertItems(items: List<OrderItemEntity>)

    /** Удаление заказа (позиции удаляются каскадно). */
    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
