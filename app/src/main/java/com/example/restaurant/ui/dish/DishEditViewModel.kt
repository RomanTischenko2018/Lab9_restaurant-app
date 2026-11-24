package com.example.restaurant.ui.dish

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.restaurant.domain.model.Categories
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.model.SaveDishResult
import com.example.restaurant.domain.usecase.DeleteDishUseCase
import com.example.restaurant.domain.usecase.GetDishUseCase
import com.example.restaurant.domain.usecase.SaveDishUseCase
import com.example.restaurant.ui.appContainer
import com.example.restaurant.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние формы блюда.
 *
 * @property price цена как текст поля ввода.
 * @property isNew `true` для нового блюда.
 * @property isFinished `true` после сохранения или удаления — сигнал вернуться назад.
 */
data class DishEditUiState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = Categories.MAINS,
    val isNew: Boolean = true,
    val nameError: Boolean = false,
    val priceError: Boolean = false,
    val isFinished: Boolean = false
)

/** ViewModel формы блюда (создание, редактирование, удаление). */
class DishEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDish: GetDishUseCase,
    private val saveDish: SaveDishUseCase,
    private val deleteDish: DeleteDishUseCase
) : ViewModel() {

    private val dishId: Long = savedStateHandle.get<Long>(Routes.ARG_DISH_ID) ?: NEW_DISH_ID
    private var original: Dish? = null

    private val _uiState = MutableStateFlow(DishEditUiState())

    /** Состояние формы. */
    val uiState: StateFlow<DishEditUiState> = _uiState.asStateFlow()

    init {
        if (dishId != NEW_DISH_ID) {
            viewModelScope.launch {
                getDish(dishId)?.let { dish ->
                    original = dish
                    _uiState.update {
                        it.copy(
                            name = dish.name,
                            description = dish.description,
                            price = dish.price.toString(),
                            category = dish.category,
                            isNew = false
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, nameError = false) }

    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }

    /** Принимаются только цифры. */
    fun onPriceChange(value: String) =
        _uiState.update { it.copy(price = value.filter(Char::isDigit), priceError = false) }

    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }

    /** Сохраняет блюдо; при ошибке валидации подсвечивает поле и остаётся на экране. */
    fun onSave() {
        val s = _uiState.value
        viewModelScope.launch {
            val base = original ?: Dish(name = "", price = 0, category = s.category)
            val result = saveDish(
                base.copy(
                    name = s.name,
                    description = s.description,
                    price = s.price.toIntOrNull() ?: 0,
                    category = s.category
                )
            )
            _uiState.update {
                when (result) {
                    SaveDishResult.SUCCESS -> it.copy(isFinished = true)
                    SaveDishResult.INVALID_NAME -> it.copy(nameError = true)
                    SaveDishResult.INVALID_PRICE -> it.copy(priceError = true)
                }
            }
        }
    }

    /** Удаляет редактируемое блюдо. */
    fun onDelete() {
        viewModelScope.launch {
            if (dishId != NEW_DISH_ID) deleteDish(dishId)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    companion object {
        private const val NEW_DISH_ID = -1L

        /** Фабрика, получающая зависимости из `AppContainer` и `SavedStateHandle`. */
        val Factory = viewModelFactory {
            initializer {
                val c = appContainer()
                DishEditViewModel(createSavedStateHandle(), c.getDish, c.saveDish, c.deleteDish)
            }
        }
    }
}
