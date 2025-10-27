package com.example.rbgames_grupo1.ui.screen

import android.icu.text.NumberFormat
import android.icu.util.Currency
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rbgames_grupo1.R
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

// ---------- Helper: placeholder visual ----------
@Composable
private fun PlaceholderBanner(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, null)
        }
    }
}

// ---------- Helper: busca drawable por nombre (seguro, no crashea) ----------
private fun drawableIdFor(
    packageName: String,
    context: android.content.Context,
    nombre: String
): Int {
    val normalized = nombre
        .lowercase()
        .replace("é", "e").replace("á", "a").replace("í", "i").replace("ó", "o").replace("ú", "u")
        .replace(Regex("[^a-z0-9]+"), "") // deja solo a-z0-9

    val alias = when (normalized) {
        "gtav" -> "gta5"
        "left4death2" -> "left4dead2"
        else -> normalized
    }

    val candidates = listOf(
        alias,
        alias.replace(" ", "_"),
        alias.replace("-", "_"),
    )

    for (key in candidates) {
        val id = context.resources.getIdentifier(key, "drawable", packageName)
        if (id != 0) return id
    }
    return 0
}

// --- UI: Tarjeta de juego ---
@Composable
fun JuegoCard(
    juego: Juego,
    onAgregar: (Juego) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val resId = remember(juego.nombre) { drawableIdFor(ctx.packageName, ctx, juego.nombre) }

    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---------- IMAGEN ----------
            if (resId != 0) {
                Image(
                    painterResource(resId),
                    juego.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                PlaceholderBanner(
                    icon = Icons.Default.VideogameAsset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // ---------- TEXTOS ----------
            Text(
                juego.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                juego.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // ---------- PRECIO + BOTÓN ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    clpFormatter.format(juego.precioCLP),
                    style = MaterialTheme.typography.titleSmall
                )
                FilledTonalButton(onClick = { onAgregar(juego) }) {
                    Icon(Icons.Default.AddShoppingCart, null)
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
        TopAppBar(title = { Text("Juegos en venta") })
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

// ---------- Route para usar en NavGraph ----------
@Composable
fun ProductosRoute(
    onAgregarCarrito: (Juego) -> Unit
) {
    // Por ahora reutilizamos los mismos datos del preview
    ProductosScreen(
        juegos = demoJuegos,
        onAgregarCarrito = onAgregarCarrito
    )
}

// --- Preview de los juegos ---
private val demoJuegos = listOf(
    Juego(1, "Destiny 2", "Acción RPG en mundo abierto, desafiante y épico.", 44990),
    Juego(2, "Gta 5", "Juego de acción y aventuras en mundo abierto.", 62990),
    Juego(3, "Left 4 Dead 2", "Juego de disparos en primera persona.", 19990),
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
