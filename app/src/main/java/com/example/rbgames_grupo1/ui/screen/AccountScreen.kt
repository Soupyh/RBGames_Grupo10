package com.example.rbgames_grupo1.ui.screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rbgames_grupo1.ui.util.createImageUri
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel
import com.example.rbgames_grupo1.ui.viewmodel.SupportViewModel
import com.example.rbgames_grupo1.ui.viewmodel.ReportFormUi
import com.example.rbgames_grupo1.ui.viewmodel.Role
import com.example.rbgames_grupo1.data.local.database.AppDatabase
import com.example.rbgames_grupo1.data.repository.ReportRepository
import androidx.compose.runtime.collectAsState


@Composable
fun AccountScreen(
    vm: AuthViewModel,
    onSaved: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val session by vm.session.collectAsState()
    if (!session.isLoggedIn || session.user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No has iniciado sesión.")
        }
        return
    }

    // ---------------- Cámara (igual que tu versión) ----------------
    val context = LocalContext.current
    var nombre by remember(session.user) { mutableStateOf(session.user!!.nombre) }
    var email by remember(session.user) { mutableStateOf(session.user!!.email) }
    var telefono by remember(session.user) { mutableStateOf(session.user!!.telefono) }
    var photoUri by remember(session.user?.photoUri) { mutableStateOf(session.user?.photoUri?.let(Uri::parse)) }

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingPhotoUri != null) {
            photoUri = pendingPhotoUri
            vm.updatePhoto(photoUri.toString())
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val out = createImageUri(context)
            pendingPhotoUri = out
            if (out != null) takePicture.launch(out)
        } else {
            // opcional: snackbar/toast
        }
    }

    fun openCamera() {
        val out = createImageUri(context)
        pendingPhotoUri = out
        if (out != null) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
            // o: takePicture.launch(out)
        }
    }

    // ---------------- Reportes (VM sin Hilt) ----------------
    val db = remember { AppDatabase.getInstance(context) }
    val supportVm = remember { SupportViewModel(ReportRepository(db.reportDao())) }

    val role = session.user!!.role
    val myEmail = session.user!!.email

    LaunchedEffect(role, myEmail) {
        supportVm.loadInbox(role, myEmail)
    }

    val form by supportVm.form.collectAsStateWithLifecycle(initialValue = ReportFormUi())
    val inbox by supportVm.inbox.collectAsStateWithLifecycle(initialValue = emptyList())

    // Limpia el formulario tras envío exitoso
    LaunchedEffect(form.successId) {
        if (form.successId != null) {
            supportVm.setSubject("")
            supportVm.setMessage("")
        }
    }

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
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // ⬅️ hace scroll
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mi cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            // Avatar + botón cámara
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            (nombre.takeIf { it.isNotBlank() }?.firstOrNull()?.uppercase() ?: "U"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(onClick = { openCamera() }) {
                    Text("Cambiar foto")
                }
            }

            // Campos de perfil
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
            Text(
                "Puedes actualizar tus datos y tu foto de perfil tomada con la cámara.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ---------------- Sistema de Reportes ----------------
            Divider()
            Text("Soporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = form.subject,
                onValueChange = supportVm::setSubject,
                label = { Text("Asunto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = form.message,
                onValueChange = supportVm::setMessage,
                label = { Text("Mensaje") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            )

            val canSend = form.subject.isNotBlank() && form.message.isNotBlank() && !form.sending

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { supportVm.send(myEmail) },
                    enabled = canSend
                ) {
                    Text(
                        when {
                            form.sending -> "Enviando..."
                            !canSend -> "Completa el formulario"
                            else -> "Enviar ticket"
                        }
                    )
                }

                if (form.successId != null) {
                    Text("Enviado (#${form.successId})", color = MaterialTheme.colorScheme.primary)
                }
                if (form.error != null) {
                    Text(form.error!!, color = MaterialTheme.colorScheme.error)
                }
            }

            // Bandeja solo para ADMIN y SOPORTE
            if (role == Role.ADMIN || role == Role.SOPORTE) {
                Divider()
                Text("Bandeja de tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (inbox.isEmpty()) {
                    Text("No hay tickets.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(inbox, key = { it.id }) { t ->
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

            // margen para que nada quede bajo la bottom bar
            Spacer(modifier = Modifier.heightIn(min = 96.dp))
        }
    }
}
