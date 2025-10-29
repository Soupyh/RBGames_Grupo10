package com.example.rbgames_grupo1.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

import com.example.rbgames_grupo1.data.memory.ProductsStore
import com.example.rbgames_grupo1.data.memory.AdminProductUi
import kotlinx.coroutines.flow.collectLatest

// ======================================================
// ===============  ADMIN > USUARIOS  ===================
// ======================================================

private data class AdminUserUi(
    val id: Long,
    val nombre: String,
    val email: String,
    val role: Role,
    val activo: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(onBack: () -> Unit) {
    // Estado en memoria (DEMO). Luego lo conectas a tu VM/DAO real.
    var users by remember {
        mutableStateOf(
            listOf(
                AdminUserUi(1, "Admin", "a@a.cl", Role.ADMIN, true),
                AdminUserUi(2, "Soporte", "soporte@a.cl", Role.SOPORTE, true),
                AdminUserUi(3, "Benjamin Leal", "benjamin@a.cl", Role.USUARIO, true),
            )
        )
    }
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<Role?>(null) }

    val filtered = remember(users, query, roleFilter) {
        users
            .filter {
                val t = "${it.nombre} ${it.email}".lowercase()
                t.contains(query.lowercase())
            }
            .filter { roleFilter == null || it.role == roleFilter }
            .sortedBy { it.id }
    }

    fun addQuick() {
        val nextId = (users.maxOfOrNull { it.id } ?: 0L) + 1
        users = users + AdminUserUi(nextId, "Nuevo $nextId", "nuevo$nextId@a.cl", Role.USUARIO, true)
    }
    fun toggleActivo(u: AdminUserUi) {
        users = users.map { if (it.id == u.id) it.copy(activo = !it.activo) else it }
    }
    fun changeRole(u: AdminUserUi, role: Role) {
        users = users.map { if (it.id == u.id) it.copy(role = role) else it }
    }
    fun deleteUser(u: AdminUserUi) {
        users = users.filterNot { it.id == u.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
                actions = {
                    IconButton(onClick = { addQuick() }) {
                        Icon(Icons.Filled.Add, contentDescription = "Agregar")
                    }
                }
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
            // Buscar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Buscar por nombre o email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Filtro de rol
            RolesFilterRow(selected = roleFilter, onSelect = { roleFilter = it })

            Divider()

            if (filtered.isEmpty()) {
                Text("No hay usuarios.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.id }) { u ->
                        UsuarioCard(
                            user = u,
                            onToggleActivo = { toggleActivo(u) },
                            onChangeRole = { r -> changeRole(u, r) },
                            onDelete = { deleteUser(u) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RolesFilterRow(selected: Role?, onSelect: (Role?) -> Unit) {
    val opciones = listOf<Role?>(null, Role.USUARIO, Role.SOPORTE, Role.ADMIN)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { r ->
            FilterChip(
                selected = selected == r,
                onClick = { onSelect(r) },
                label = { Text((r?.name ?: "Todos").lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

// ======================================================
//
// ==============  ADMIN > USUARIOS (placeholder) ======
//
// ======================================================
@Composable
private fun UsuarioCard(
    user: AdminUserUi,
    onToggleActivo: () -> Unit,
    onChangeRole: (Role) -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(user.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rol:", fontWeight = FontWeight.Medium)
                var expanded by remember { mutableStateOf(false) }
                AssistChip(onClick = { expanded = true }, label = { Text(user.role.name) })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Role.values().forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.name) },
                            onClick = {
                                expanded = false
                                onChangeRole(r)
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val activoTxt = if (user.activo) "Desactivar" else "Activar"
                OutlinedButton(onClick = onToggleActivo) { Text(activoTxt) }
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            }
        }
    }
}

// ======================================================
//
// ==============  ADMIN > PRODUCTOS (placeholder) ======
//
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen(onBack: () -> Unit) {
    // Importa el store


    // Observa productos compartidos
    val productos by ProductsStore.productos.collectAsStateWithLifecycle()

    fun clp(n: Int) = "$" + "%,d".format(n).replace(',', '.')

    // -------- Búsqueda y filtros --------
    var query by remember { mutableStateOf("") }
    val categorias = remember(productos) { productos.map { it.categoria }.toSet().toList().sorted() }
    var categoriaSel by remember { mutableStateOf<String?>(null) }
    var verSoloActivos by remember { mutableStateOf(false) }

    val filtrados = remember(productos, query, categoriaSel, verSoloActivos) {
        productos
            .asSequence()
            .filter { if (verSoloActivos) it.activo else true }
            .filter { categoriaSel == null || it.categoria == categoriaSel }
            .filter {
                val t = "${it.nombre} ${it.categoria}".lowercase()
                t.contains(query.lowercase())
            }
            .sortedBy { it.id }
            .toList()
    }

    // -------- Acciones (todas escriben al store) --------
    fun actualizar(p: AdminProductUi) = ProductsStore.update(p)
    fun eliminar(p: AdminProductUi) = ProductsStore.delete(p.id)
    fun cambiarStock(p: AdminProductUi, delta: Int) {
        ProductsStore.changeStock(p.id, delta)
    }
    fun toggleActivo(p: AdminProductUi) = ProductsStore.update(p.copy(activo = !p.activo))

    // -------- Diálogos (agregar/editar) --------
    var editarProducto by remember { mutableStateOf<AdminProductUi?>(null) }
    var mostrarAgregar by remember { mutableStateOf(false) }

    @Composable
    fun ProductoDialog(
        titulo: String,
        inicial: AdminProductUi,
        onDismiss: () -> Unit,
        onSave: (AdminProductUi) -> Unit
    ) {
        var nombre by remember(inicial) { mutableStateOf(inicial.nombre) }
        var categoria by remember(inicial) { mutableStateOf(inicial.categoria) }
        var precioTxt by remember(inicial) { mutableStateOf(inicial.precioCLP.toString()) }
        var stockTxt by remember(inicial) { mutableStateOf(inicial.stock.toString()) }
        var activo by remember(inicial) { mutableStateOf(inicial.activo) }

        val precioVal = precioTxt.toIntOrNull()
        val stockVal = stockTxt.toIntOrNull()
        val valido = nombre.isNotBlank() && categoria.isNotBlank() &&
                precioVal != null && precioVal >= 0 &&
                stockVal != null && stockVal >= 0

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titulo) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, singleLine = true)
                    OutlinedTextField(categoria, { categoria = it }, label = { Text("Categoría") }, singleLine = true)
                    OutlinedTextField(
                        precioTxt, { precioTxt = it.filter(Char::isDigit) },
                        label = { Text("Precio CLP") }, singleLine = true
                    )
                    OutlinedTextField(
                        stockTxt, { stockTxt = it.filter(Char::isDigit) },
                        label = { Text("Stock") }, singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = activo, onCheckedChange = { activo = it })
                        Text("Activo")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSave(
                            inicial.copy(
                                nombre = nombre.trim(),
                                categoria = categoria.trim(),
                                precioCLP = precioVal ?: 0,
                                stock = stockVal ?: 0,
                                activo = activo
                            )
                        )
                    },
                    enabled = valido
                ) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
        )
    }

    // -------- UI --------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } },
                actions = { TextButton(onClick = { mostrarAgregar = true }) { Text("Agregar") } }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text("Buscar por nombre o categoría") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = categoriaSel == null, onClick = { categoriaSel = null }, label = { Text("Todas") })
                categorias.forEach { cat ->
                    FilterChip(selected = categoriaSel == cat, onClick = { categoriaSel = cat }, label = { Text(cat) })
                }
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = { verSoloActivos = !verSoloActivos },
                    label = { Text(if (verSoloActivos) "Solo activos" else "Todos") })
            }

            Divider()

            if (filtrados.isEmpty()) {
                Text("No hay productos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtrados, key = { it.id }) { p ->
                        ElevatedCard {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(p.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("Categoría: ${p.categoria}", color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Precio: ${clp(p.precioCLP)}", fontWeight = FontWeight.Medium)
                                    Text(
                                        text = "Stock: ${p.stock}",
                                        color = if (p.stock > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { ProductsStore.changeStock(p.id, +1) }) { Text("+1 stock") }
                                    OutlinedButton(onClick = { ProductsStore.changeStock(p.id, -1) }, enabled = p.stock > 0) { Text("-1 stock") }
                                    OutlinedButton(onClick = { editarProducto = p }) { Text("Editar") }
                                    OutlinedButton(onClick = { toggleActivo(p) }) { Text(if (p.activo) "Desactivar" else "Activar") }
                                    OutlinedButton(
                                        onClick = { eliminar(p) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Eliminar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo Agregar
    if (mostrarAgregar) {
        val nextId = (productos.maxOfOrNull { it.id } ?: 0L) + 1
        ProductoDialog(
            titulo = "Agregar producto",
            inicial = AdminProductUi(nextId, "", "", 0, 0, true),
            onDismiss = { mostrarAgregar = false },
            onSave = { nuevo -> ProductsStore.add(nuevo); mostrarAgregar = false }
        )
    }

    // Diálogo Editar
    editarProducto?.let { prod ->
        ProductoDialog(
            titulo = "Editar producto",
            inicial = prod,
            onDismiss = { editarProducto = null },
            onSave = { actualizado -> ProductsStore.update(actualizado); editarProducto = null }
        )
    }
}



// ======================================================
//
// ==============  ADMIN > REPORTES (tickets) ===========
//
// ======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportesScreen(
    role: Role,   // rol actual logueado
    email: String,
    onBack: () -> Unit
) {
    // Seguridad básica (solo ADMIN o SOPORTE)
    if (role != Role.ADMIN && role != Role.SOPORTE) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Reportes") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } })
        }) { inner ->
            Box(
                Modifier.padding(inner).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
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
        supportVm.loadInbox(role, email) // si tienes loadAll() úsalo aquí para admin
    }

    val inbox by supportVm.inbox.collectAsStateWithLifecycle(initialValue = emptyList())

    // Filtro simple de estado
    var filtro by remember { mutableStateOf("TODOS") }
    val estados = listOf("TODOS", "ABIERTO", "EN_PROCESO", "CERRADO")
    val listaFiltrada = remember(inbox, filtro) {
        if (filtro == "TODOS") inbox
        else inbox.filter { it.status.equals(filtro, ignoreCase = true) }
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
