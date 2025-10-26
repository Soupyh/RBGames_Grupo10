package com.example.rbgames_grupo1.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch

import com.example.rbgames_grupo1.ui.components.AppBottomBar
import com.example.rbgames_grupo1.ui.components.AppDrawer
import com.example.rbgames_grupo1.ui.components.BottomItem
import com.example.rbgames_grupo1.ui.components.defaultDrawerItems
import com.example.rbgames_grupo1.ui.screen.HomeScreen
import com.example.rbgames_grupo1.ui.screen.ProductosScreen
import com.example.rbgames_grupo1.ui.screen.LoginScreenVm
import com.example.rbgames_grupo1.ui.screen.RegisterScreenVm
import com.example.rbgames_grupo1.ui.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Helpers de navegación (usan el mismo route del NavHost)
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = null, // opcional: navController.currentBackStackEntry?.destination?.route
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
        Scaffold(
            bottomBar = {
                // 👇 Usamos EXACTAMENTE las rutas del NavHost
                AppBottomBar(
                    navController = navController,
                    items = listOf(
                        BottomItem(
                            route = Route.Home.path,     // "home" (ejemplo)
                            label = "Inicio",
                            icon = Icons.Filled.Home
                        ),
                        BottomItem(
                            route = Route.Productos.path, // Aquí va la pestaña "Juegos"
                            label = "Juegos",
                            icon = Icons.Filled.VideogameAsset
                        ),
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onGoProductos = goProductos
                    )
                }
                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                // Pestaña "Juegos" -> usamos tu ProductosScreen
                composable(Route.Productos.path) {
                    // Si necesitas un demo local, define aquí tu lista de productos/juegos.
                    // Evitamos usar un nombre 'Juego' que choque con Composables.
                    ProductosScreen(
                        juegos = emptyList(), // Cárgalos desde tu VM o repo
                        onAgregarCarrito = { /* TODO integrar carrito */ }
                    )
                }
            }
        }
    }
}
