package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rbgames_grupo1.data.local.database.AppDatabase
import com.example.rbgames_grupo1.data.repository.ReportRepository
import com.example.rbgames_grupo1.ui.viewmodel.Role
import com.example.rbgames_grupo1.ui.viewmodel.SupportViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Usuarios") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } })
    }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text("Aquí va el CRUD de usuarios.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Productos") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } })
    }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text("Aquí va alta/stock/precios de productos.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportesScreen(
    role: Role,                 // 👈 recibimos el rol del usuario actual
    email: String,              // 👈 y su email (por si tu loadInbox lo usa)
    onBack: () -> Unit
) {
    // Seguridad básica (solo ADMIN o SOPORTE)
    if (role != Role.ADMIN && role != Role.SOPORTE) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Reportes") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } })
        }) { inner ->
            Box(Modifier.padding(inner).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No autorizado")
            }
        }
        return
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val supportVm = remember { SupportViewModel(ReportRepository(db.reportDao())) }

    // Carga inicial de la bandeja
    LaunchedEffect(role, email) {
        // Si tu repos reconoce ADMIN para listar todo, basta con pasar (role, email)
        // Si necesitas listar TODO como admin, puedes usar: supportVm.loadAll()
        supportVm.loadInbox(role, email)
    }

    val inbox by supportVm.inbox.collectAsStateWithLifecycle(initialValue = emptyList())

    // Filtro simple de estado
    var filtro by remember { mutableStateOf("TODOS") }
    val estados = listOf("TODOS", "ABIERTO", "EN_PROCESO", "CERRADO")
    val listaFiltrada = remember(inbox, filtro) {
        if (filtro == "TODOS") inbox else inbox.filter { it.status.equals(filtro, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes • Tickets") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bandeja de tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Filtros de estado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                estados.forEach { estado ->
                    FilterChip(
                        selected = filtro == estado,
                        onClick = { filtro = estado },
                        label = { Text(estado.replace('_', ' ')) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (listaFiltrada.isEmpty()) {
                Text("No hay tickets.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listaFiltrada, key = { it.id }) { t ->
                        ElevatedCard {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("#${t.id} • ${t.status}", fontWeight = FontWeight.SemiBold)
                                Text("De: ${t.userEmail}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Asunto: ${t.subject}", fontWeight = FontWeight.Medium)
                                Text(t.message)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { supportVm.changeStatus(t.id, "EN_PROCESO") }) {
                                        Text("En proceso")
                                    }
                                    OutlinedButton(onClick = { supportVm.changeStatus(t.id, "CERRADO") }) {
                                        Text("Cerrar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}