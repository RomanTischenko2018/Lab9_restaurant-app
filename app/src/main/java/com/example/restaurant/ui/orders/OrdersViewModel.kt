package com.example.restaurant.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.restaurant.domain.model.Order
import com.example.restaurant.domain.usecase.DeleteOrderUseCase
import com.example.restaurant.domain.usecase.ObserveOrdersUseCase
import com.example.restaurant.ui.appContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Состояние экрана истории заказов. */
data class OrdersUiState(val orders: List<Order> = emptyList())

/** ViewModel истории заказов. */
class OrdersViewModel(
    observeOrders: ObserveOrdersUseCase,
    private val deleteOrder: DeleteOrderUseCase
) : ViewModel() {

    /** Реактивное состояние: список обновляется при любом изменении БД. */
    val uiState: StateFlow<OrdersUiState> = observeOrders()
        .map { OrdersUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState())

    /** Удаляет заказ [order] из истории. */
    fun onDelete(order: Order) {
        viewModelScope.launch { deleteOrder(order.id) }
    }

    companion object {
        /** Фабрика, получающая зависимости из `AppContainer`. */
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                OrdersViewModel(c.observeOrders, c.deleteOrder)
            }
        }
    }
}
