package com.example.restaurant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.restaurant.ui.cart.CartScreen
import com.example.restaurant.ui.dish.DishEditScreen
import com.example.restaurant.ui.menu.MenuScreen
import com.example.restaurant.ui.orders.OrdersScreen

/** Маршруты навигации. */
object Routes {
    const val MENU = "menu"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ARG_DISH_ID = "dishId"
    /** Экран блюда; без аргумента открывается в режиме создания. */
    const val DISH = "dish?$ARG_DISH_ID={$ARG_DISH_ID}"

    /** Маршрут редактирования блюда [id]. */
    fun dishRoute(id: Long) = "dish?$ARG_DISH_ID=$id"
}

/** Граф навигации: меню → корзина → заказы, меню → редактирование блюда. */
@Composable
fun RestaurantNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.MENU) {
        composable(Routes.MENU) {
            MenuScreen(
                onCartClick = { nav.navigate(Routes.CART) },
                onOrdersClick = { nav.navigate(Routes.ORDERS) },
                onAddDishClick = { nav.navigate("dish") },
                onDishClick = { id -> nav.navigate(Routes.dishRoute(id)) }
            )
        }
        composable(Routes.CART) {
            CartScreen(
                onBack = { nav.popBackStack() },
                onOrderPlaced = {
                    nav.navigate(Routes.ORDERS) {
                        popUpTo(Routes.CART) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ORDERS) {
            OrdersScreen(onBack = { nav.popBackStack() })
        }
        composable(
            route = Routes.DISH,
            arguments = listOf(
                navArgument(Routes.ARG_DISH_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            DishEditScreen(onBack = { nav.popBackStack() })
        }
    }
}
