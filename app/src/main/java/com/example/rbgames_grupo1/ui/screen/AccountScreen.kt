package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel

@Composable
fun AccountScreen(
    vm: AuthViewModel,
    onSaved: () -> Unit = {},    // opcional: volver atrás / mostrar toast
    onLogout: () -> Unit = {}    // navega a Home/ Login después de cerrar sesión
) {
    val session by vm.session.collectAsStateWithLifecycle()

    if (!session.isLoggedIn || session.user == null) {
        // No logueado: muestra fallback simple
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No has iniciado sesión.")
        }
        return
    }

    var nombre by remember(session.user) { mutableStateOf(session.user!!.nombre) }
    var email by remember(session.user) { mutableStateOf(session.user!!.email) }
    var telefono by remember(session.user) { mutableStateOf(session.user!!.telefono) }

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            vm.logout()
                            onLogout()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cerrar sesión") }

                    Button(
                        onClick = {
                            vm.updateProfile(nombre, email, telefono)
                            onSaved()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar cambios") }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Mi cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = telefono, onValueChange = { telefono = it },
                label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Estos datos son los que ingresaste al registrarte. Puedes modificarlos cuando quieras.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
