package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rbgames_grupo1.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onGoProductos: () -> Unit
) {
    val juegosDemo = listOf(
        Juego(1, "Destiny 2", "Acción RPG en mundo abierto, desafiante y épico.", 44_990),
        Juego(2, "Gta 5", "Juego de acción y aventuras en mundo abierto.", 62_990),
        Juego(3, "Left 4 Dead 2", "Juego de disparos en primera persona.", 19_990),
        Juego(4, "Payday 2", "Juego de disparos en primera persona y asaltos.", 39_990),
    )

    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("RB Games", fontWeight = FontWeight.SemiBold) }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onGoProductos,
                icon = { Icon(Icons.Default.Add, "Añadir juego") },
                text = { Text("Añadir un juego") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                "Catálogo de juegos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(juegosDemo, key = { it.id }) { juego ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {

                            // --- IMAGEN (usa tu mapper) ---
                            val imgRes = imageResFor(juego.nombre)
                            if (imgRes != null) {
                                Image(
                                    painter = painterResource(imgRes),
                                    contentDescription = juego.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(10.dp))
                            }

                            Text(
                                juego.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(juego.descripcion, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

// Pega esto en el mismo archivo (o muévelo a un util). Requiere: import com.example.rbgames_grupo1.R
private fun imageResFor(nombre: String): Int? {
    val key = nombre
        .lowercase()
        .replace("é","e").replace("á","a").replace("í","i").replace("ó","o").replace("ú","u")
        .replace(Regex("[\\s_-]+"), "")

    return when (key) {
        "destiny2"                   -> R.drawable.destiny2
        "gta5", "gtav"               -> R.drawable.gta5
        "left4dead2", "left4death2"  -> R.drawable.left4dead2
        "payday2"                    -> R.drawable.payday2
        else                         -> null            // si no hay imagen, oculta el bloque
        // o usa un placeholder: R.drawable.placeholder_game
    }
}
