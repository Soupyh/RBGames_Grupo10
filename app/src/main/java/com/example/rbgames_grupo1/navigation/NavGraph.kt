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
import com.example.rbgames_grupo1.ui.screen.ProductosRoute // <<< usamos la Route (no la Screen)
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
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path) {
            launchSingleTop = true
        }
    }
    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path) {
            launchSingleTop = true
        }
    }
    val goProductos: () -> Unit = {
        navController.navigate(Route.Productos.path) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // ---- Capa superior: Drawer lateral modal que envuelve al Scaffold ----
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null, // Podrías pasar: navController.currentBackStackEntry?.destination?.route
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onLogin = {
                        scope.launch { drawerState.close() }
                        goLogin()
                    },
                    onRegister = {
                        scope.launch { drawerState.close() }
                        goRegister()
                    }
                )
            )
        }
    ) {
        // ---- Layout base de pantalla con bottomBar y contenido ----
        Scaffold(
            bottomBar = {
                AppBottomBar(
                    navController = navController,
                    items = listOf(
                        BottomItem(
                            route = Route.Home.path,
                            label = "Inicio",
                            icon = Icons.Filled.Home
                        ),
                        BottomItem(
                            route = Route.Productos.path,
                            label = "Juegos",
                            icon = Icons.Filled.VideogameAsset
                        ),
                    )
                )
            }
        ) { innerPadding ->
            // ---- Contenedor de rutas ----
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                // ---- Destino: Home ----
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onGoProductos = goProductos
                    )
                }
                // ---- Destino: Login ----
                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                // ---- Destino: Registro ----
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                // ---- Destino: Productos (pestaña "Juegos") ----
                composable(Route.Productos.path) {
                    // Usamos la Route que internamente inyecta la lista demo (misma del Preview)
                    ProductosRoute(
                        onAgregarCarrito = { /* TODO: integrar lógica de carrito */ }
                    )
                }
            }
        }
    }
}
