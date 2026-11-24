package com.example.restaurant.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.PlaceOrderResult
import com.example.restaurant.domain.usecase.AddToCartUseCase
import com.example.restaurant.domain.usecase.DecrementCartUseCase
import com.example.restaurant.domain.usecase.ObserveCartUseCase
import com.example.restaurant.domain.usecase.PlaceOrderUseCase
import com.example.restaurant.ui.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние экрана корзины.
 *
 * @property total итоговая сумма.
 * @property tableNumber введённый номер столика (текст поля).
 * @property tableError `true`, если номер столика неверный.
 * @property orderPlaced `true` после успешного оформления — сигнал перейти к заказам.
 */
data class CartUiState(
    val lines: List<CartLine> = emptyList(),
    val total: Int = 0,
    val tableNumber: String = "",
    val tableError: Boolean = false,
    val orderPlaced: Boolean = false
)

/** ViewModel корзины: изменение количества порций и оформление заказа. */
class CartViewModel(
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val decrementCart: DecrementCartUseCase,
    private val placeOrder: PlaceOrderUseCase
) : ViewModel() {

    private data class Form(
        val table: String = "",
        val tableError: Boolean = false,
        val placed: Boolean = false
    )

    private val form = MutableStateFlow(Form())

    /** Единый поток состояния для UI. */
    val uiState: StateFlow<CartUiState> = combine(observeCart(), form) { lines, f ->
        CartUiState(lines, lines.sumOf { it.sum }, f.table, f.tableError, f.placed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    /** Добавляет порцию. */
    fun onIncrease(line: CartLine) {
        viewModelScope.launch { addToCart(line.dishId) }
    }

    /** Убирает порцию. */
    fun onDecrease(line: CartLine) {
        viewModelScope.launch { decrementCart(line.dishId) }
    }

    /** Изменение номера столика (принимаются только цифры). */
    fun onTableChange(value: String) =
        form.update { it.copy(table = value.filter(Char::isDigit), tableError = false) }

    /** Оформляет заказ; при неверном столике показывает ошибку. */
    fun onPlaceOrder() {
        viewModelScope.launch {
            val result = placeOrder(form.value.table.toIntOrNull() ?: 0)
            form.update {
                when (result) {
                    PlaceOrderResult.SUCCESS -> it.copy(placed = true)
                    PlaceOrderResult.INVALID_TABLE -> it.copy(tableError = true)
                    PlaceOrderResult.EMPTY_CART -> it
                }
            }
        }
    }

    companion object {
        /** Фабрика, получающая зависимости из `AppContainer`. */
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                CartViewModel(c.observeCart, c.addToCart, c.decrementCart, c.placeOrder)
            }
        }
    }
}
