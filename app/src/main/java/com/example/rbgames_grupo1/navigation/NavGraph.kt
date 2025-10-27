package com.example.rbgames_grupo1.navigation

// UI y navegación
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

// Componentes propios
import com.example.rbgames_grupo1.ui.components.AppBottomBar
import com.example.rbgames_grupo1.ui.components.AppDrawer
import com.example.rbgames_grupo1.ui.components.BottomItem
import com.example.rbgames_grupo1.ui.components.defaultDrawerItems

// Pantallas
import com.example.rbgames_grupo1.ui.screen.LoginScreenVm
import com.example.rbgames_grupo1.ui.screen.RegisterScreenVm
import com.example.rbgames_grupo1.ui.screen.HomeScreen
import com.example.rbgames_grupo1.ui.screen.ProductosScreen
import com.example.rbgames_grupo1.ui.screen.Juego // data class usada para la lista de productos
import com.example.rbgames_grupo1.ui.screen.CarritoScreen // ⬅️ NUEVO

// ViewModel
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel

// Rutas
object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Productos = "productos"
    const val Carrito = "carrito" // ⬅️ NUEVO
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Items del BottomBar (solo en secciones principales)
    val bottomItems = listOf(
        BottomItem(route = Routes.Home,      label = "Inicio",  icon = Icons.Filled.Home),
        BottomItem(route = Routes.Productos, label = "Juegos",  icon = Icons.Filled.VideogameAsset),
        BottomItem(route = Routes.Carrito,   label = "Carrito", icon = Icons.Filled.ShoppingCart), // ⬅️ NUEVO
        BottomItem(route = Routes.Login,     label = "Login",   icon = Icons.Filled.Login)
    )

    // Ruta actual (null-safe y sin args)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = backStackEntry?.destination?.route?.substringBefore("?")

    // Mostrar BottomBar solo en estas pantallas
    val showBottomBar = currentRoute in setOf(Routes.Home, Routes.Productos, Routes.Carrito) // ⬅️ incluye Carrito

    // Helper para navegar conservando estado y marcando correctamente la pestaña
    fun navigateSingleTop(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                items = defaultDrawerItems(
                    onHome = {
                        navigateSingleTop(Routes.Home)
                        scope.launch { drawerState.close() }
                    },
                    onLogin = {
                        navController.navigate(Routes.Login) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    onRegister = {
                        navController.navigate(Routes.Register) {
                            popUpTo(Routes.Register) { inclusive = true }
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                    // Si quieres añadir Carrito al Drawer, puedo extender defaultDrawerItems.
                )
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomBar(
                        navController = navController,
                        items = bottomItems
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.Home, // Arranca en Home
                modifier = Modifier.padding(innerPadding)
            ) {
                // ---------- LOGIN ----------
                composable(Routes.Login) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onGoRegister = {
                            navController.navigate(Routes.Register) {
                                launchSingleTop = true
                            }
                        },
                        onLoginOkNavigateHome = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Login) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // ---------- REGISTER ----------
                composable(Routes.Register) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onGoLogin = {
                            navController.navigate(Routes.Login) { launchSingleTop = true }
                        },
                        onRegisteredNavigateLogin = {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Register) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // ---------- HOME (con BottomBar visible) ----------
                composable(Routes.Home) {
                    HomeScreen(
                        onGoLogin = {
                            navController.navigate(Routes.Login) { launchSingleTop = true }
                        },
                        onGoRegister = {
                            navController.navigate(Routes.Register) { launchSingleTop = true }
                        },
                        onGoProductos = {
                            navigateSingleTop(Routes.Productos)
                        }
                    )
                }

                // ---------- PRODUCTOS (con BottomBar visible) ----------
                composable(Routes.Productos) {
                    // Lista local (demo)
                    val juegos = listOf(
                        Juego(1, "Destiny 2", "Acción RPG en mundo abierto, desafiante y épico.", 44990),
                        Juego(2, "Gta 5", "Juego de acción y aventuras en mundo abierto.", 62990),
                        Juego(3, "Left 4 Death 2", "Juego de disparos en primera persona.", 19990),
                        Juego(4, "Payday 2", "Juego de disparos en primera persona y asaltos.", 39990),
                    )

                    ProductosScreen(
                        juegos = juegos,
                        onAgregarCarrito = {
                            // Al añadir, vamos al carrito
                            navigateSingleTop(Routes.Carrito) // ⬅️ NUEVO
                        }
                    )
                }

                // ---------- CARRITO (con BottomBar visible) ----------
                composable(Routes.Carrito) {
                    CarritoScreen(
                        onCheckout = { total ->
                            // TODO: Navegar a pantalla de pago o mostrar diálogo de confirmación
                            // navController.navigate("pago?monto=$total")
                        }
                    )
                }
            }
        }
    }
}
