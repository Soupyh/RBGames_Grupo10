package com.example.rbgames_grupo1.navigation

// UI y navegación
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.rbgames_grupo1.ui.screen.Juego
import com.example.rbgames_grupo1.ui.screen.CarritoScreen
import com.example.rbgames_grupo1.ui.screen.AccountScreen

// ViewModels
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel
import com.example.rbgames_grupo1.ui.viewmodel.CarritoViewModel   // ⬅️ NUEVO
import com.example.rbgames_grupo1.ui.viewmodel.CartItem          // ⬅️ NUEVO



// Rutas
object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Productos = "productos"
    const val Carrito = "carrito"
    const val Cuenta = "cuenta"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ⬇️ ViewModel de carrito compartido para todo el grafo
    val carritoVm: CarritoViewModel = viewModel()

    // Observa la sesión para decidir qué mostrar en BottomBar
    val sessionState by authViewModel.session.collectAsState()

    // BottomBar dinámica: Login ↔ Cuenta
    val bottomItems: List<BottomItem> =
        if (sessionState.isLoggedIn) {
            listOf(
                BottomItem(route = Routes.Home,      label = "Inicio",  icon = Icons.Filled.Home),
                BottomItem(route = Routes.Productos, label = "Juegos",  icon = Icons.Filled.VideogameAsset),
                BottomItem(route = Routes.Carrito,   label = "Carrito", icon = Icons.Filled.ShoppingCart),
                BottomItem(route = Routes.Cuenta,    label = "Cuenta",  icon = Icons.Filled.AccountCircle)
            )
        } else {
            listOf(
                BottomItem(route = Routes.Home,      label = "Inicio",  icon = Icons.Filled.Home),
                BottomItem(route = Routes.Productos, label = "Juegos",  icon = Icons.Filled.VideogameAsset),
                BottomItem(route = Routes.Carrito,   label = "Carrito", icon = Icons.Filled.ShoppingCart),
                BottomItem(route = Routes.Login,     label = "Login",   icon = Icons.Filled.Login)
            )
        }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String? = backStackEntry?.destination?.route?.substringBefore("?")
    val showBottomBar = currentRoute in setOf(Routes.Home, Routes.Productos, Routes.Carrito, Routes.Cuenta)

    fun navigateSingleTop(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                startDestination = Routes.Home,
                modifier = Modifier.padding(innerPadding)
            ) {
                // ---------- LOGIN ----------
                composable(Routes.Login) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onGoRegister = {
                            navController.navigate(Routes.Register) { launchSingleTop = true }
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

                // ---------- HOME ----------
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

                // ---------- PRODUCTOS ----------
                composable(Routes.Productos) {
                    val juegos = listOf(
                        Juego(1, "Destiny 2", "Acción RPG en mundo abierto, desafiante y épico.", 44990),
                        Juego(2, "Gta 5", "Juego de acción y aventuras en mundo abierto.", 62990),
                        Juego(3, "Left 4 Death 2", "Juego de disparos en primera persona.", 19990),
                        Juego(4, "Payday 2", "Juego de disparos en primera persona y asaltos.", 39990),
                    )

                    ProductosScreen(
                        juegos = juegos,
                        onAgregarCarrito = { juego ->
                            // ⬅️ AQUI SE AGREGA AL CARRITO
                            carritoVm.addOrIncrement(
                                CartItem(
                                    id = juego.id.toString(),
                                    nombre = juego.nombre,
                                    precio = juego.precioCLP,
                                    imagenRes = null, // usa drawable si tienes
                                    cantidad = 1
                                )
                            )
                            // y navegamos al carrito (opcional)
                            navigateSingleTop(Routes.Carrito)
                        }
                    )
                }

                // ---------- CARRITO ----------
                composable(Routes.Carrito) {
                    // Usa el MISMO VM compartido para ver lo que se agregó
                    CarritoScreen(
                        vm = carritoVm, // ⬅️ importante compartir instancia
                        onCheckout = { total ->
                            // navController.navigate("pago?monto=$total")
                        }
                    )
                }

                // ---------- CUENTA ----------
                composable(Routes.Cuenta) {
                    AccountScreen(
                        vm = authViewModel,
                        onSaved = { /* opcional */ },
                        onLogout = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                // ---------- PAGOS ----------
                composable(Routes.Carrito) {
                    CarritoScreen(
                        vm = carritoVm,
                        onCheckout = { total ->
                            // guardar venta si quieres
                        },
                        onGoHome = {
                            navController.navigate(Routes.Home) {
                                popUpTo(Routes.Home) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
