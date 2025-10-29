package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
fun AdminReportesScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Reportes") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } })
    }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text("Aquí llegan los reportes de los usuarios.")
        }
    }
}
