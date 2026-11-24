package com.example.restaurant.data.repository

import androidx.room.withTransaction
import com.example.restaurant.data.local.AppDatabase
import com.example.restaurant.data.local.CartDao
import com.example.restaurant.data.local.CartRow
import com.example.restaurant.data.local.DishDao
import com.example.restaurant.data.local.OrderDao
import com.example.restaurant.data.local.OrderEntity
import com.example.restaurant.data.local.OrderItemEntity
import com.example.restaurant.data.local.OrderWithItems
import com.example.restaurant.data.local.toDomain
import com.example.restaurant.data.local.toEntity
import com.example.restaurant.data.remote.MealApi
import com.example.restaurant.data.remote.toDishEntity
import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.model.Order
import com.example.restaurant.domain.repository.CartRepository
import com.example.restaurant.domain.repository.MenuRepository
import com.example.restaurant.domain.repository.OrderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Меню по принципу offline-first: источник истины — Room, сеть только пополняет базу. */
class MenuRepositoryImpl(
    private val dao: DishDao,
    private val api: MealApi
) : MenuRepository {

    override fun observeDishes(query: String, category: String): Flow<List<Dish>> =
        dao.observe(query.trim(), category).map { list -> list.map { it.toDomain() } }

    override suspend fun getDish(id: Long): Dish? = dao.getById(id)?.toDomain()

    override suspend fun saveDish(dish: Dish) {
        if (dish.id == 0L) dao.insert(dish.toEntity()) else dao.update(dish.toEntity())
    }

    override suspend fun deleteDish(id: Long) = dao.deleteById(id)

    override suspend fun refreshFromNetwork(): Result<Int> = try {
        val entities = REMOTE_LETTERS.flatMap { letter ->
            api.searchByLetter(letter).meals.orEmpty().mapNotNull { it.toDishEntity() }
        }
        Result.success(dao.insertAll(entities).count { it != -1L })
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        val REMOTE_LETTERS = listOf("b", "c")
    }
}

/** Корзина поверх Room. */
class CartRepositoryImpl(private val dao: CartDao) : CartRepository {

    override fun observeCart(): Flow<List<CartLine>> =
        dao.observeRows().map { rows -> rows.map(CartRow::toDomain) }

    override suspend fun add(dishId: Long) = dao.increment(dishId)

    override suspend fun decrement(dishId: Long) = dao.decrement(dishId)
}

/** Заказы поверх Room. Оформление выполняется одной транзакцией. */
class OrderRepositoryImpl(
    private val db: AppDatabase,
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) : OrderRepository {

    override fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { list -> list.map(OrderWithItems::toDomain) }

    override suspend fun placeOrder(tableNumber: Int): Long? = db.withTransaction {
        val rows = cartDao.snapshot()
        if (rows.isEmpty()) return@withTransaction null
        val orderId = orderDao.insertOrder(
            OrderEntity(
                createdAt = System.currentTimeMillis(),
                tableNumber = tableNumber,
                total = rows.sumOf { it.price * it.quantity }
            )
        )
        orderDao.insertItems(rows.map { OrderItemEntity(orderId = orderId, name = it.name, price = it.price, quantity = it.quantity) })
        cartDao.clear()
        orderId
    }

    override suspend fun deleteOrder(id: Long) = orderDao.deleteById(id)
}
