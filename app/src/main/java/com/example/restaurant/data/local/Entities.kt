package com.example.restaurant.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Таблица `dishes`. Уникальный индекс по [remoteId] защищает от дублей при загрузке с сервера. */
@Entity(tableName = "dishes", indices = [Index(value = ["remoteId"], unique = true)])
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val price: Int,
    val category: String,
    val remoteId: Int? = null
)

/** Таблица `cart`: одна строка на блюдо. Удаление блюда каскадно удаляет строку корзины. */
@Entity(
    tableName = "cart",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CartEntity(
    @PrimaryKey val dishId: Long,
    val quantity: Int
)

/** Таблица `orders` — шапка заказа. */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val tableNumber: Int,
    val total: Int
)

/** Таблица `order_items` — позиции заказа (копия названия и цены на момент заказа). */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val name: String,
    val price: Int,
    val quantity: Int
)

/** Заказ вместе с позициями (связь «один ко многим»). */
data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>
)

/** Результат JOIN-запроса: строка корзины с данными блюда. */
data class CartRow(
    val dishId: Long,
    val name: String,
    val price: Int,
    val quantity: Int
)
