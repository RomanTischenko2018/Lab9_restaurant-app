package com.example.restaurant.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.usecase.AddToCartUseCase
import com.example.restaurant.domain.usecase.ObserveCartUseCase
import com.example.restaurant.domain.usecase.ObserveDishesUseCase
import com.example.restaurant.domain.usecase.RefreshMenuUseCase
import com.example.restaurant.ui.appContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние экрана меню.
 *
 * @property category выбранная категория; пустая строка — «все».
 * @property cartCount общее количество порций в корзине (для значка).
 * @property showNetworkError `true`, если нужно показать сообщение об ошибке сети.
 */
data class MenuUiState(
    val dishes: List<Dish> = emptyList(),
    val query: String = "",
    val category: String = "",
    val cartCount: Int = 0,
    val isRefreshing: Boolean = false,
    val showNetworkError: Boolean = false
)

/** ViewModel меню: поиск, фильтр по категории, добавление в корзину, загрузка с сервера. */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModel(
    observeDishes: ObserveDishesUseCase,
    observeCart: ObserveCartUseCase,
    private val addToCart: AddToCartUseCase,
    private val refreshMenu: RefreshMenuUseCase
) : ViewModel() {

    private data class Status(val loading: Boolean = false, val error: Boolean = false)

    private val query = MutableStateFlow("")
    private val category = MutableStateFlow("")
    private val status = MutableStateFlow(Status())

    private val filters = combine(query, category) { q, c -> q to c }

    /** Единый поток состояния для UI. */
    val uiState: StateFlow<MenuUiState> = combine(
        filters.flatMapLatest { (q, c) -> observeDishes(q, c) },
        filters,
        observeCart().map { lines -> lines.sumOf { it.quantity } },
        status
    ) { dishes, (q, c), count, st ->
        MenuUiState(dishes, q, c, count, st.loading, st.error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MenuUiState())

    init {
        refresh()
    }

    /** Изменение поискового запроса. */
    fun onQueryChange(value: String) = query.update { value }

    /** Выбор категории (пустая строка — все). */
    fun onCategoryChange(value: String) = category.update { value }

    /** Добавляет порцию блюда [dish] в корзину. */
    fun onAddToCart(dish: Dish) {
        viewModelScope.launch { addToCart(dish.id) }
    }

    /** Загружает блюда с сервера; при ошибке выставляет [MenuUiState.showNetworkError]. */
    fun refresh() {
        if (status.value.loading) return
        viewModelScope.launch {
            status.value = Status(loading = true)
            status.value = Status(error = refreshMenu().isFailure)
        }
    }

    /** Сообщает, что ошибка уже показана. */
    fun onErrorShown() = status.update { it.copy(error = false) }

    companion object {
        /** Фабрика, получающая зависимости из `AppContainer`. */
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                MenuViewModel(c.observeDishes, c.observeCart, c.addToCart, c.refreshMenu)
            }
        }
    }
}
