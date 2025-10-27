package com.example.rbgames_grupo1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rbgames_grupo1.ui.viewmodel.CarritoViewModel
import com.example.rbgames_grupo1.ui.viewmodel.CartItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CarritoScreen(
    vm: CarritoViewModel = viewModel(),
    onCheckout: (totalCLP: Int) -> Unit = {},
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle(initialValue = 0)

    Scaffold(
        bottomBar = {
            Summary(
                total = total,
                onPagar = { if (total > 0) onCheckout(total) },
                enabled = total > 0
            )
        }
    ) { inner ->
        if (items.isEmpty()) {
            EmptyCartStateNoTop(Modifier.padding(inner))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Carrito",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(items, key = { it.id }) { item ->
                    CartItem(
                        item = item,
                        onIncrement = { vm.increment(item.id) },
                        onDecrement = { vm.decrement(item.id) },
                        onRemove = { vm.remove(item.id) }
                    )
                }

                item { Spacer(Modifier.height(84.dp)) } // espacio para no tapar con bottom bar
            }
        }
    }
}

@Composable
private fun CartItem(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.imagenRes != null) {
                    Image(
                        painter = painterResource(id = item.imagenRes),
                        contentDescription = item.nombre,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nombre,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = clpNoTop(item.precio),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedIconButton(
                            onClick = onDecrement,
                            enabled = item.cantidad > 1
                        ) { Icon(Icons.Filled.Remove, contentDescription = "Disminuir") }

                        Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)

                        OutlinedIconButton(onClick = onIncrement) {
                            Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                        }

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = clpNoTop(item.subtotal),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                IconButton(onClick = {
                    visible = false
                    onRemove()
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
private fun Summary(
    total: Int,
    onPagar: () -> Unit,
    enabled: Boolean
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Total", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    clpNoTop(total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onPagar,
                enabled = enabled,
                shape = RoundedCornerShape(12.dp)
            ) { Text(if (enabled) "Pagar" else "Carrito vacío") }
        }
    }
}

@Composable
private fun EmptyCartStateNoTop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "Agrega juegos desde la tienda para verlos aquí.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun clpNoTop(value: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(value)
