package com.example.restaurant.di

import android.content.Context
import com.example.restaurant.data.local.AppDatabase
import com.example.restaurant.data.remote.MealApi
import com.example.restaurant.data.repository.CartRepositoryImpl
import com.example.restaurant.data.repository.MenuRepositoryImpl
import com.example.restaurant.data.repository.OrderRepositoryImpl
import com.example.restaurant.domain.usecase.AddToCartUseCase
import com.example.restaurant.domain.usecase.DecrementCartUseCase
import com.example.restaurant.domain.usecase.DeleteDishUseCase
import com.example.restaurant.domain.usecase.DeleteOrderUseCase
import com.example.restaurant.domain.usecase.GetDishUseCase
import com.example.restaurant.domain.usecase.ObserveCartUseCase
import com.example.restaurant.domain.usecase.ObserveDishesUseCase
import com.example.restaurant.domain.usecase.ObserveOrdersUseCase
import com.example.restaurant.domain.usecase.PlaceOrderUseCase
import com.example.restaurant.domain.usecase.RefreshMenuUseCase
import com.example.restaurant.domain.usecase.SaveDishUseCase
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Ручной контейнер зависимостей: создаёт БД, сеть, репозитории и use case'ы. */
class AppContainer(context: Context) {

    private val database = AppDatabase.create(context)

    private val api: MealApi = Retrofit.Builder()
        .baseUrl(MealApi.BASE_URL)
        .client(
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MealApi::class.java)

    private val menuRepository = MenuRepositoryImpl(database.dishDao(), api)
    private val cartRepository = CartRepositoryImpl(database.cartDao())
    private val orderRepository = OrderRepositoryImpl(database, database.cartDao(), database.orderDao())

    val observeDishes = ObserveDishesUseCase(menuRepository)
    val getDish = GetDishUseCase(menuRepository)
    val saveDish = SaveDishUseCase(menuRepository)
    val deleteDish = DeleteDishUseCase(menuRepository)
    val refreshMenu = RefreshMenuUseCase(menuRepository)

    val observeCart = ObserveCartUseCase(cartRepository)
    val addToCart = AddToCartUseCase(cartRepository)
    val decrementCart = DecrementCartUseCase(cartRepository)

    val placeOrder = PlaceOrderUseCase(orderRepository)
    val observeOrders = ObserveOrdersUseCase(orderRepository)
    val deleteOrder = DeleteOrderUseCase(orderRepository)
}
