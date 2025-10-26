package com.example.rbgames_grupo1.ui.screen

import android.icu.text.NumberFormat
import android.icu.util.Currency
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Locale

// --- Modelo simple ---
data class Juego(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precioCLP: Int
)

// --- Formateo CLP (Chile) ---
private val clpFormatter by lazy {
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        currency = Currency.getInstance("CLP")
        maximumFractionDigits = 0
    }
}

// --- UI: Tarjeta de juego ---
@Composable
fun JuegoCard(
    juego: Juego,
    onAgregar: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // (Opcional) Aquí podrías poner una imagen con AsyncImage si ya tienes URLs

            Text(
                text = juego.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = juego.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = clpFormatter.format(juego.precioCLP),
                    style = MaterialTheme.typography.titleSmall
                )
                FilledTonalButton(
                    onClick = { onAgregar(juego) }
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar")
                }
            }
        }
    }
}

// --- Pantalla: grilla de productos ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    juegos: List<Juego>,
    onAgregarCarrito: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Juegos en venta") }
        )
        if (juegos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay juegos disponibles.")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(juegos, key = { it.id }) { juego ->
                    JuegoCard(
                        juego = juego,
                        onAgregar = onAgregarCarrito,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ---Preview de los juego---
private val demoJuegos = listOf(
    Juego(1, "Destiny 2", "Acción RPG en mundo abierto, desafiante y épico.", 44990),
    Juego(2, "Gta 5", "Juego de acción y aventuras en mundo abierto.", 62990),
    Juego(3, "Left 4 Death 2", "Juego de disparos en primera persona.", 19990),
    Juego(4, "Payday 2", "Juego de disparos en primera persona y asaltos.", 39990),
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ProductosScreenPreview() {
    MaterialTheme {
        ProductosScreen(
            juegos = demoJuegos,
            onAgregarCarrito = {}
        )
    }
}