package com.example.rbgames_grupo1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    vm: CarritoViewModel = viewModel(),
    onCheckout: (totalCLP: Int) -> Unit = {},
    onGoHome: () -> Unit = {}
) {
    val items by vm.items.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle(initialValue = 0)

    var showResumen by remember { mutableStateOf(false) }  // hoja de detalle
    var showExito  by remember { mutableStateOf(false) }   // diálogo de “Compra exitosa”

    Scaffold(
        bottomBar = {
            SummaryBar(
                total = total,
                onPagar = { if (total > 0) showResumen = true },
                enabled = total > 0
            )
        }
    ) { inner ->
        if (items.isEmpty()) {
            EmptyCartState(Modifier.padding(inner))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrement = { vm.increment(item.id) },
                        onDecrement = { vm.decrement(item.id) },
                        onRemove   = { vm.remove(item.id) }
                    )
                }
                item { Spacer(Modifier.height(84.dp)) }
            }
        }
    }

    // --------- DETALLE DE VENTA ----------
    if (showResumen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showResumen = false },
            sheetState = sheetState
        ) {
            ResumenVentaContent(
                items = items,
                total = total,
                onConfirmar = {
                    showResumen = false
                    onCheckout(total)   // guardar/navegar si quieres
                    vm.clear()          //  VACÍA EL CARRITO
                    showExito = true    // muestra diálogo de éxito
                },
                onCancelar = { showResumen = false }
            )
        }
    }

    // --------- DIÁLOGO “COMPRA EXITOSA” ----------
    if (showExito) {
        CompraExitosaDialog(
            onOk = {
                showExito = false
                onGoHome()            // NAVEGA A HOME
            }
        )
    }
}

// ------------------ UI: Fila de item ------------------

@Composable
private fun CartItemRow(
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

                Column(Modifier.weight(1f)) {
                    Text(
                        item.nombre,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = clp(item.precio),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedIconButton(onClick = onDecrement, enabled = item.cantidad > 1) {
                            Icon(Icons.Filled.Remove, contentDescription = "Disminuir")
                        }
                        Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
                        OutlinedIconButton(onClick = onIncrement) {
                            Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                        }
                        Spacer(Modifier.weight(1f))
                        Text(clp(item.subtotal), style = MaterialTheme.typography.titleMedium)
                    }
                }

                IconButton(onClick = { visible = false; onRemove() }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

// ------------------ UI: Barra inferior ------------------

@Composable
private fun SummaryBar(
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
                    clp(total),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(onClick = onPagar, enabled = enabled, shape = RoundedCornerShape(12.dp)) {
                Text(if (enabled) "Pagar" else "Carrito vacío")
            }
        }
    }
}

// ------------------ UI: Estado vacío ------------------

@Composable
private fun EmptyCartState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

// ------------------ UI: Contenido del Detalle de Venta ------------------

@Composable
private fun ResumenVentaContent(
    items: List<CartItem>,
    total: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    val iva = (total * 0.19).toInt()         // IVA 19% (opcional)
    val neto = total - iva                   // Neto (opcional)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Detalle de la venta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        items.forEach { itx ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${itx.cantidad}× ${itx.nombre}", modifier = Modifier.weight(1f))
                Text(clp(itx.subtotal), fontWeight = FontWeight.SemiBold)
            }
        }

        Divider()

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Neto", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(clp(neto))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("IVA (19%)", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(clp(iva))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(clp(total), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar") }

            Button(
                onClick = onConfirmar,
                modifier = Modifier.weight(1f)
            ) { Text("Confirmar compra") }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ------------------ Diálogo de Éxito ------------------

@Composable
private fun CompraExitosaDialog(onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onOk, // al tocar fuera también va a Home
        icon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                "¡Compra exitosa!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = { Text("TU COMPRA HA SIDO REGISTRADA CORRECTAMENTE.") },
        confirmButton = { Button(onClick = onOk) { Text("OK") } },
        shape = RoundedCornerShape(16.dp)
    )
}

// ------------------ Util ------------------

private fun clp(value: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(value)
