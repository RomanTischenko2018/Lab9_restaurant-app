package com.example.restaurant.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurant.R
import com.example.restaurant.domain.model.Categories
import com.example.restaurant.domain.model.Dish

/**
 * Главный экран: меню с поиском, фильтром по категориям и кнопкой «в корзину».
 *
 * @param onCartClick переход в корзину.
 * @param onOrdersClick переход к истории заказов.
 * @param onAddDishClick создание нового блюда.
 * @param onDishClick редактирование блюда по идентификатору.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onAddDishClick: () -> Unit,
    onDishClick: (Long) -> Unit,
    viewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorText = stringResource(R.string.error_network)

    LaunchedEffect(state.showNetworkError) {
        if (state.showNetworkError) {
            snackbarHostState.showSnackbar(errorText)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_title)) },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onOrdersClick) {
                        Icon(Icons.AutoMirrored.Filled.List, stringResource(R.string.open_orders))
                    }
                    IconButton(onClick = onCartClick) {
                        BadgedBox(badge = {
                            if (state.cartCount > 0) Badge { Text(state.cartCount.toString()) }
                        }) {
                            Icon(Icons.Default.ShoppingCart, stringResource(R.string.open_cart))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDishClick) {
                Icon(Icons.Default.Add, stringResource(R.string.add_dish))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            if (state.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            CategoryChips(selected = state.category, onSelect = viewModel::onCategoryChange)
            if (state.dishes.isEmpty() && !state.isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.empty_menu), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(state.dishes, key = { it.id }) { dish ->
                        DishRow(dish, onAdd = viewModel::onAddToCart, onClick = onDishClick)
                    }
                }
            }
        }
    }
}

/** Горизонтальный ряд чипов: «Все» и категории меню. */
@Composable
private fun CategoryChips(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected.isEmpty(),
                onClick = { onSelect("") },
                label = { Text(stringResource(R.string.category_all)) }
            )
        }
        items(Categories.all) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category) }
            )
        }
    }
}

/** Строка блюда: название, описание, цена и кнопка добавления в корзину. */
@Composable
private fun DishRow(dish: Dish, onAdd: (Dish) -> Unit, onClick: (Long) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(dish.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = dish.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (dish.description.isNotBlank()) {
                Text(
                    text = dish.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.price_format, dish.price),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        FilledTonalIconButton(onClick = { onAdd(dish) }) {
            Icon(Icons.Default.Add, stringResource(R.string.add_to_cart))
        }
    }
}
