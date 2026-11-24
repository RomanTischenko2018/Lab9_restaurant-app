package com.example.restaurant.ui

import com.example.restaurant.domain.model.CartLine
import com.example.restaurant.domain.model.Categories
import com.example.restaurant.domain.model.Dish
import com.example.restaurant.domain.repository.CartRepository
import com.example.restaurant.domain.repository.MenuRepository
import com.example.restaurant.domain.usecase.AddToCartUseCase
import com.example.restaurant.domain.usecase.ObserveCartUseCase
import com.example.restaurant.domain.usecase.ObserveDishesUseCase
import com.example.restaurant.domain.usecase.RefreshMenuUseCase
import com.example.restaurant.ui.menu.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** Заглушка меню: данные в памяти, сеть имитируется. */
private class FakeMenuRepository(
    initial: List<Dish>,
    private val refreshResult: Result<Int> = Result.success(0)
) : MenuRepository {
    private val items = MutableStateFlow(initial)

    override fun observeDishes(query: String, category: String): Flow<List<Dish>> =
        items.map { list ->
            list.filter {
                it.name.contains(query, ignoreCase = true) && (category.isEmpty() || it.category == category)
            }
        }

    override suspend fun getDish(id: Long) = items.value.firstOrNull { it.id == id }
    override suspend fun saveDish(dish: Dish) { items.value = items.value + dish }
    override suspend fun deleteDish(id: Long) { items.value = items.value.filterNot { it.id == id } }
    override suspend fun refreshFromNetwork(): Result<Int> = refreshResult
}

/** Заглушка корзины: хранит количество порций по id блюда. */
private class FakeCartRepository : CartRepository {
    val lines = MutableStateFlow<Map<Long, Int>>(emptyMap())

    override fun observeCart(): Flow<List<CartLine>> =
        lines.map { m -> m.map { (id, q) -> CartLine(id, "dish$id", 100, q) } }

    override suspend fun add(dishId: Long) { lines.value = lines.value + (dishId to (lines.value[dishId] ?: 0) + 1) }
    override suspend fun decrement(dishId: Long) {
        val q = lines.value[dishId] ?: return
        lines.value = if (q <= 1) lines.value - dishId else lines.value + (dishId to q - 1)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    private val borscht = Dish(id = 1, name = "Борщ", price = 320, category = Categories.MAINS)
    private val tiramisu = Dish(id = 2, name = "Тирамису", price = 290, category = Categories.DESSERTS)

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(menu: MenuRepository, cart: CartRepository = FakeCartRepository()) = MenuViewModel(
        ObserveDishesUseCase(menu), ObserveCartUseCase(cart), AddToCartUseCase(cart), RefreshMenuUseCase(menu)
    )

    @Test
    fun `поиск фильтрует блюда по названию`() = runTest {
        val vm = viewModel(FakeMenuRepository(listOf(borscht, tiramisu)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onQueryChange("бор")

        assertEquals(listOf(1L), vm.uiState.value.dishes.map { it.id })
    }

    @Test
    fun `фильтр по категории оставляет только нужные блюда`() = runTest {
        val vm = viewModel(FakeMenuRepository(listOf(borscht, tiramisu)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onCategoryChange(Categories.DESSERTS)

        assertEquals(listOf(2L), vm.uiState.value.dishes.map { it.id })
    }

    @Test
    fun `добавление в корзину увеличивает счётчик на значке`() = runTest {
        val vm = viewModel(FakeMenuRepository(listOf(borscht)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        vm.onAddToCart(borscht)
        vm.onAddToCart(borscht)

        assertEquals(2, vm.uiState.value.cartCount)
    }

    @Test
    fun `ошибка сети выставляет флаг и сбрасывается`() = runTest {
        val vm = viewModel(FakeMenuRepository(listOf(borscht), Result.failure(IOException("offline"))))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        assertTrue(vm.uiState.value.showNetworkError)
        vm.onErrorShown()
        assertFalse(vm.uiState.value.showNetworkError)
    }
}
