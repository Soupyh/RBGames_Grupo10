package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel
import com.example.rbgames_grupo1.ui.viewmodel.Role
import com.example.rbgames_grupo1.ui.viewmodel.SupportViewModel
import com.example.rbgames_grupo1.data.local.database.AppDatabase
import com.example.rbgames_grupo1.data.repository.ReportRepository
import androidx.compose.ui.platform.LocalContext

@Composable
fun AccountScreen(
    vm: AuthViewModel,
    onSaved: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val session by vm.session.collectAsStateWithLifecycle()
    if (!session.isLoggedIn || session.user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No has iniciado sesión.") }
        return
    }

    // ====== WIRE-UP simple del SupportViewModel (sin Hilt) ======
    val ctx = LocalContext.current
    val db = remember { AppDatabase.getInstance(ctx) }
    val supportVm = remember { SupportViewModel(ReportRepository(db.reportDao())) }

    val role = session.user!!.role
    val myEmail = session.user!!.email

    // Carga inbox según rol (Admin/Soporte ven todo)
    LaunchedEffect(role, myEmail) {
        supportVm.loadInbox(role, myEmail)
    }

    var nombre by remember(session.user) { mutableStateOf(session.user!!.nombre) }
    var email by remember(session.user) { mutableStateOf(session.user!!.email) }
    var telefono by remember(session.user) { mutableStateOf(session.user!!.telefono) }

    val form by supportVm.form.collectAsStateWithLifecycle()
    val inbox by supportVm.inbox.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.logout(); onLogout() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cerrar sesión") }

                    Button(
                        onClick = { vm.updateProfile(nombre, email, telefono); onSaved() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar cambios") }
                }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mi cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // ====== Datos básicos ======
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth())

            // ====== SOPORTE: enviar ticket ======
            Divider()
            Text("Soporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = form.subject, onValueChange = supportVm::setSubject,
                label = { Text("Asunto") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.message, onValueChange = supportVm::setMessage,
                label = { Text("Mensaje") }, modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { supportVm.send(myEmail) },
                    enabled = !form.sending
                ) { Text(if (form.sending) "Enviando..." else "Enviar ticket") }

                if (form.successId != null) {
                    Text("Enviado (#${form.successId})", color = MaterialTheme.colorScheme.primary)
                }
                if (form.error != null) {
                    Text(form.error!!, color = MaterialTheme.colorScheme.error)
                }
            }

            // ====== Bandeja (solo Soporte/Admin) ======
            if (role == Role.ADMIN || role == Role.SOPORTE) {
                Divider()
                Text("Bandeja de tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (inbox.isEmpty()) {
                    Text("No hay tickets.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(inbox, key = { it.id }) { t ->
                            ElevatedCard {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("#${t.id} • ${t.status}", fontWeight = FontWeight.SemiBold)
                                    Text("De: ${t.userEmail}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Asunto: ${t.subject}", fontWeight = FontWeight.Medium)
                                    Text(t.message)

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { supportVm.changeStatus(t.id, "EN_PROCESO") }) { Text("En proceso") }
                                        OutlinedButton(onClick = { supportVm.changeStatus(t.id, "CERRADO") }) { Text("Cerrar") }
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
