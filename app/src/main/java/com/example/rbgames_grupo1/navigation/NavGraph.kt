package com.example.rbgames_grupo1.navigation // Paquete donde vive este NavGraph

// -------- IMPORTS: UI y navegación --------
import androidx.compose.foundation.layout.padding // Para aplicar el padding del Scaffold al contenido
import androidx.compose.material.icons.Icons // Contenedor de íconos por defecto
import androidx.compose.material.icons.filled.Home // Ícono de "Inicio"
import androidx.compose.material.icons.filled.VideogameAsset // Ícono de "Juegos"
import androidx.compose.material3.ModalNavigationDrawer // Componente de Drawer lateral modal
import androidx.compose.material3.DrawerValue // Estados posibles del Drawer (Opened/Closed)
import androidx.compose.material3.rememberDrawerState // Creador/remember del estado del Drawer
import androidx.compose.material3.Scaffold // Layout base con slots (topBar, bottomBar, content, etc.)
import androidx.compose.runtime.Composable // Anotación para funciones composables
import androidx.compose.runtime.rememberCoroutineScope // Alcance de corrutinas para abrir/cerrar Drawer
import androidx.compose.ui.Modifier // Modificadores de Compose
import androidx.navigation.NavHostController // Controlador de navegación
import androidx.navigation.compose.NavHost // Contenedor de destinos de navegación
import androidx.navigation.compose.composable // Declaración de cada destino del NavHost
import kotlinx.coroutines.launch // Para lanzar corrutinas al abrir/cerrar Drawer

// -------- IMPORTS: Componentes propios de tu app --------
import com.example.rbgames_grupo1.ui.components.AppBottomBar // Tu barra inferior reutilizable
import com.example.rbgames_grupo1.ui.components.AppDrawer // Tu Drawer personalizado
import com.example.rbgames_grupo1.ui.components.BottomItem // Data class para items de la bottom bar
import com.example.rbgames_grupo1.ui.components.defaultDrawerItems // Helper para ítems del Drawer
import com.example.rbgames_grupo1.ui.screen.HomeScreen // Pantalla de inicio
import com.example.rbgames_grupo1.ui.screen.ProductosScreen // Pantalla de productos (la usamos como "Juegos")
import com.example.rbgames_grupo1.ui.screen.LoginScreenVm // Pantalla de login conectada a ViewModel
import com.example.rbgames_grupo1.ui.screen.RegisterScreenVm // Pantalla de registro conectada a ViewModel
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel // ViewModel compartido para Login/Registro

@Composable // Indicamos que esta función construye UI declarativa
fun AppNavGraph(
    navController: NavHostController, // Controlador que permite navegar entre destinos
    authViewModel: AuthViewModel      // ViewModel inyectado desde MainActivity para auth
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Estado del Drawer (inicia cerrado)
    val scope = rememberCoroutineScope() // Alcance de corrutinas para animar abrir/cerrar el Drawer

    // ---- Helpers de navegación: centralizamos la lógica de navegar a cada ruta ----
    val goHome: () -> Unit = { // Navegar a Home preservando estado de la pestaña
        navController.navigate(Route.Home.path) { // Usamos la ruta definida en Route.Home
            popUpTo(navController.graph.startDestinationId) { saveState = true } // Evita duplicados y guarda estado
            launchSingleTop = true // Evita apilar múltiples copias del mismo destino
            restoreState = true // Restaura el estado guardado de la pestaña si existe
        }
    }
    val goLogin: () -> Unit = { // Navegar a Login (sin restaurar estado porque es flujo simple)
        navController.navigate(Route.Login.path) {
            launchSingleTop = true // Evita duplicados
        }
    }
    val goRegister: () -> Unit = { // Navegar a Registro
        navController.navigate(Route.Register.path) {
            launchSingleTop = true // Evita duplicados
        }
    }
    val goProductos: () -> Unit = { // Navegar a Productos (la pestaña "Juegos")
        navController.navigate(Route.Productos.path) {
            popUpTo(navController.graph.startDestinationId) { saveState = true } // Mantiene estado por pestaña
            launchSingleTop = true // Evita duplicados
            restoreState = true // Restaura el estado de la pestaña
        }
    }

    // ---- Capa superior: Drawer lateral modal que envuelve al Scaffold ----
    ModalNavigationDrawer(
        drawerState = drawerState, // Conectamos el estado del Drawer
        drawerContent = { // Contenido del Drawer (menú lateral)
            AppDrawer( // Tu componente Drawer reutilizable
                currentRoute = null, // Podrías pasar: navController.currentBackStackEntry?.destination?.route
                items = defaultDrawerItems( // Construye la lista de ítems del Drawer
                    onHome = { // Acción al tocar "Home" en el Drawer
                        scope.launch { drawerState.close() } // Cerramos el Drawer con corrutina
                        goHome() // Navegamos a Home
                    },
                    onLogin = { // Acción al tocar "Login" en el Drawer
                        scope.launch { drawerState.close() } // Cerramos el Drawer
                        goLogin() // Navegamos a Login
                    },
                    onRegister = { // Acción al tocar "Registro" en el Drawer
                        scope.launch { drawerState.close() } // Cerramos el Drawer
                        goRegister() // Navegamos a Registro
                    }
                )
            )
        }
    ) {
        // ---- Layout base de pantalla con bottomBar y contenido ----
        Scaffold(
            bottomBar = { // Definimos la barra inferior (reemplaza a la antigua TopAppBar)
                AppBottomBar( // Reutilizamos tu componente de bottom bar
                    navController = navController, // Se usa para marcar item seleccionado y navegar
                    items = listOf( // Ítems visibles en la barra
                        BottomItem( // Item "Inicio"
                            route = Route.Home.path, // La ruta debe coincidir con la definida en el NavHost
                            label = "Inicio", // Texto bajo el ícono
                            icon = Icons.Filled.Home // Ícono a mostrar
                        ),
                        BottomItem( // Item "Juegos" (apunta a ProductosScreen)
                            route = Route.Productos.path, // Usamos Productos como pestaña de Juegos
                            label = "Juegos", // Texto bajo el ícono
                            icon = Icons.Filled.VideogameAsset // Ícono a mostrar
                        ),
                    )
                )
            }
        ) { innerPadding -> // Padding que aporta el Scaffold para no tapar contenido con la bottom bar
            // ---- Contenedor de rutas: aquí se declaran y renderizan las pantallas según la ruta actual ----
            NavHost(
                navController = navController, // Controlador que gestiona la pila de destinos
                startDestination = Route.Home.path, // Ruta inicial cuando abre la app
                modifier = Modifier.padding(innerPadding) // Aplicamos el padding para evitar solapamiento
            ) {
                // ---- Destino: Home ----
                composable(Route.Home.path) { // Cuando la ruta activa coincide con Home
                    HomeScreen( // Renderizamos la pantalla Home
                        onGoLogin = goLogin, // Callback para ir a Login
                        onGoRegister = goRegister, // Callback para ir a Registro
                        onGoProductos = goProductos // Callback para ir a Productos (Juegos)
                    )
                }
                // ---- Destino: Login ----
                composable(Route.Login.path) { // Cuando la ruta activa es Login
                    LoginScreenVm( // Pantalla de Login con ViewModel
                        vm = authViewModel, // Inyectamos el ViewModel compartido
                        onLoginOkNavigateHome = goHome, // Si el login es OK, volvemos a Home
                        onGoRegister = goRegister // Link para ir a Registro
                    )
                }
                // ---- Destino: Registro ----
                composable(Route.Register.path) { // Cuando la ruta activa es Registro
                    RegisterScreenVm( // Pantalla de Registro con ViewModel
                        vm = authViewModel, // Inyectamos el mismo ViewModel
                        onRegisteredNavigateLogin = goLogin, // Tras registrarse, ir a Login
                        onGoLogin = goLogin // Link alternativo para ir a Login
                    )
                }
                // ---- Destino: Productos (lo usamos como pestaña "Juegos") ----
                composable(Route.Productos.path) { // Cuando la ruta activa es Productos
                    ProductosScreen( // Renderizamos la pantalla de productos
                        juegos = emptyList(), // Puedes cargar aquí tu lista desde VM/Repo; vacío por ahora
                        onAgregarCarrito = { /* TODO: integrar lógica de carrito */ } // Acción al agregar
                    )
                }
            }
        }
    }
}
