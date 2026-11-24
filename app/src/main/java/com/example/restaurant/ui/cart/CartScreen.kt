package com.example.restaurant.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurant.R
import com.example.restaurant.domain.model.CartLine

/**
 * Экран корзины: изменение количества порций, ввод столика и оформление заказа.
 *
 * @param onBack возврат в меню.
 * @param onOrderPlaced вызывается после успешного оформления.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit,
    viewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.orderPlaced) {
        if (state.orderPlaced) onOrderPlaced()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cart_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            if (state.lines.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.cart_empty), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.lines, key = { it.dishId }) { line ->
                        CartLineRow(line, viewModel::onIncrease, viewModel::onDecrease)
                    }
                }
                HorizontalDivider()
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.cart_total, state.total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = state.tableNumber,
                        onValueChange = viewModel::onTableChange,
                        label = { Text(stringResource(R.string.table_number)) },
                        isError = state.tableError,
                        supportingText = {
                            if (state.tableError) Text(stringResource(R.string.table_invalid))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    Button(onClick = viewModel::onPlaceOrder, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.place_order))
                    }
                }
            }
        }
    }
}

/** Строка корзины: название, цена, кнопки «−» / «+» и количество. */
@Composable
private fun CartLineRow(
    line: CartLine,
    onIncrease: (CartLine) -> Unit,
    onDecrease: (CartLine) -> Unit
) {
    val decreaseLabel = stringResource(R.string.decrease)
    val increaseLabel = stringResource(R.string.increase)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(line.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.price_format, line.sum),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = { onDecrease(line) },
            modifier = Modifier.semantics { contentDescription = decreaseLabel }
        ) { Text("−", style = MaterialTheme.typography.titleLarge) }
        Text(line.quantity.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(
            onClick = { onIncrease(line) },
            modifier = Modifier.semantics { contentDescription = increaseLabel }
        ) { Text("+", style = MaterialTheme.typography.titleLarge) }
    }
}
