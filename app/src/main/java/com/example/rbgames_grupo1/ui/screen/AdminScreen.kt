package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


 // Pantalla principal de Administración.

@Composable
fun AdminScreen(
    usuariosCount: Int = 0,
    categoriasCount: Int = 0,
    productosCount: Int = 0,
    reportesCount: Int = 0,
    onOpenUsuarios: () -> Unit = {},
    onOpenProductos: () -> Unit = {},
    onOpenReportes: () -> Unit = {}
) {
    Scaffold { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Panel de administración",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // ---------- Estadísticas rápidas ----------
            AdminQuickStats(
                usuariosCount = usuariosCount,
                categoriasCount = categoriasCount,
                productosCount = productosCount
            )

            // ---------- Acciones ----------
            Text(
                "Acciones",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val actions = listOf(
                AdminAction(
                    title = "Usuarios",
                    subtitle = "Gestión de cuentas y permisos",
                    icon = Icons.Filled.Group,
                    onClick = onOpenUsuarios,
                    highlightValue = usuariosCount,
                    openLabel = "Abrir usuarios"
                ),
                AdminAction(
                    title = "Productos",
                    subtitle = "Alta, stock y precios",
                    icon = Icons.Filled.Inventory2,
                    onClick = onOpenProductos,
                    highlightValue = productosCount,
                    openLabel = "Abrir productos"
                ),
                AdminAction(
                    title = "Reportes",
                    subtitle = "Reportes de usuarios",
                    icon = Icons.Filled.Assessment,
                    onClick = onOpenReportes,
                    highlightValue = reportesCount,
                    openLabel = "Abrir reportes"
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(actions) { action ->
                    AdminActionCard(action)
                }
            }
        }
    }
}

// ---------------------- Componentes internos ----------------------

@Composable
private fun AdminQuickStats(
    usuariosCount: Int,
    categoriasCount: Int,
    productosCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Usuarios",
            value = usuariosCount.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Categorías",
            value = categoriasCount.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Productos",
            value = productosCount.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class AdminAction(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit,
    val highlightValue: Int? = null,
    val openLabel: String? = null
)

@Composable
private fun AdminActionCard(action: AdminAction) {
    Card(
        onClick = action.onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .heightIn(min = 120.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(tonalElevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(28.dp)
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(action.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        action.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            // --- Pie de tarjeta ---
            when {
                action.openLabel != null -> {
                    OutlinedButton(
                        onClick = action.onClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(action.openLabel)
                    }
                }
                action.highlightValue != null -> {
                    // Si alguna tarjeta quiere seguir mostrando conteo
                    AssistChip(
                        onClick = action.onClick,
                        label = { Text("${action.highlightValue} ítems") },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                else -> {
                    Text(
                        "Abrir",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
