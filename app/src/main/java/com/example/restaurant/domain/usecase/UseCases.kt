package com.example.restaurant.domain.usecase

import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.model.Order
import com.example.restaurant.domain.model.PlaceOrderResult
import com.example.restaurant.domain.model.SaveDishResult
import com.example.restaurant.domain.repository.CartRepository
import com.example.restaurant.domain.repository.MenuRepository
import com.example.restaurant.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

/** Подписка на меню с поиском и фильтром по категории. */
class ObserveDishesUseCase(private val repository: MenuRepository) {
    operator fun invoke(query: String, category: String): Flow<List<Dish>> =
        repository.observeDishes(query, category)
}

/** Получение блюда по идентификатору. */
class GetDishUseCase(private val repository: MenuRepository) {
    suspend operator fun invoke(id: Long): Dish? = repository.getDish(id)
}

/** Создание или обновление блюда с проверкой названия и цены. */
class SaveDishUseCase(private val repository: MenuRepository) {
    /** @return [SaveDishResult.SUCCESS], если блюдо сохранено, иначе причина отказа. */
    suspend operator fun invoke(dish: Dish): SaveDishResult {
        val name = dish.name.trim()
        if (name.isEmpty()) return SaveDishResult.INVALID_NAME
        if (dish.price <= 0) return SaveDishResult.INVALID_PRICE
        repository.saveDish(dish.copy(name = name, description = dish.description.trim()))
        return SaveDishResult.SUCCESS
    }
}

/** Удаление блюда из меню. */
class DeleteDishUseCase(private val repository: MenuRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteDish(id)
}

/** Загрузка блюд с сервера. */
class RefreshMenuUseCase(private val repository: MenuRepository) {
    suspend operator fun invoke(): Result<Int> = repository.refreshFromNetwork()
}

/** Подписка на корзину. */
class ObserveCartUseCase(private val repository: CartRepository) {
    operator fun invoke(): Flow<List<CartLine>> = repository.observeCart()
}

/** Добавление порции блюда в корзину. */
class AddToCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(dishId: Long) = repository.add(dishId)
}

/** Уменьшение количества порций в корзине. */
class DecrementCartUseCase(private val repository: CartRepository) {
    suspend operator fun invoke(dishId: Long) = repository.decrement(dishId)
}

/** Оформление заказа из корзины. */
class PlaceOrderUseCase(private val repository: OrderRepository) {
    /** Проверяет номер столика и оформляет заказ. */
    suspend operator fun invoke(tableNumber: Int): PlaceOrderResult {
        if (tableNumber <= 0) return PlaceOrderResult.INVALID_TABLE
        return if (repository.placeOrder(tableNumber) != null) {
            PlaceOrderResult.SUCCESS
        } else {
            PlaceOrderResult.EMPTY_CART
        }
    }
}

/** Подписка на историю заказов. */
class ObserveOrdersUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<List<Order>> = repository.observeOrders()
}

/** Удаление заказа из истории. */
class DeleteOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteOrder(id)
}
