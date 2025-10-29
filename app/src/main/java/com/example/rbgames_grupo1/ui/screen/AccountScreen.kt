package com.example.rbgames_grupo1.ui.screen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun AccountScreen(
    vm: AuthViewModel,
    onSaved: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // --- Sesión ---
    val session by vm.session.collectAsStateWithLifecycle()
    val user = session.user
    if (!session.isLoggedIn || user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No has iniciado sesión.")
        }
        return
    }

    val context = LocalContext.current

    // --- Estado UI guardable ---
    var nombre by rememberSaveable { mutableStateOf(user.nombre) }
    var email by rememberSaveable { mutableStateOf(user.email) }
    var telefono by rememberSaveable { mutableStateOf(user.telefono) }

    // Guardamos la foto como String (saveable) y la convertimos a Uri cuando se usa
    var photoUriStr by rememberSaveable { mutableStateOf(user.photoUri.orEmpty()) }
    var photoUri by remember { mutableStateOf(photoUriStr.takeIf { it.isNotBlank() }?.let(Uri::parse)) }

    // Si cambia el usuario logeado, restablecemos los campos al perfil del nuevo user
    LaunchedEffect(user.email) {
        nombre = user.nombre
        email = user.email
        telefono = user.telefono
        photoUriStr = user.photoUri.orEmpty()
        photoUri = photoUriStr.takeIf { it.isNotBlank() }?.let(Uri::parse)
    }

    // --- Cámara ---
    var nextPhotoUri: Uri? by remember { mutableStateOf(null) }

    val takePicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && nextPhotoUri != null) {
            photoUri = nextPhotoUri
            photoUriStr = nextPhotoUri.toString()
            vm.updatePhoto(photoUriStr)
        }
        nextPhotoUri = null
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val out = createImageUri(context)
            nextPhotoUri = out
            if (out != null) takePicture.launch(out)
        } else {
        }
    }

    fun openCamera() {
        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    // --- Reportes ---
    val db = remember { AppDatabase.getInstance(context) }
    val supportVm = remember { SupportViewModel(ReportRepository(db.reportDao())) }

    val role = user.role
    val myEmail = user.email

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
        // Único scroll
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ----- Título -----
            item {
                Text(
                    "Mi cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // ----- Avatar + botón cámara -----
            item {
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
            }

            // ----- Campos de perfil -----
            item {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Text(
                    "Puedes actualizar tus datos y tu foto de perfil tomada con la cámara.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ----- Sistema de Reportes -----
            item { Divider() }
            item {
                Text("Soporte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                OutlinedTextField(
                    value = form.subject,
                    onValueChange = supportVm::setSubject,
                    label = { Text("Asunto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = form.message,
                    onValueChange = supportVm::setMessage,
                    label = { Text("Mensaje") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                )
            }
            item {
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
            }


            // espacio final por sobre la bottom bar
            item { Spacer(modifier = Modifier.heightIn(min = 96.dp)) }
        }
    }
}
