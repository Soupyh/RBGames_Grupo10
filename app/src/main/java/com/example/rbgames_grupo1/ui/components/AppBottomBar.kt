package com.example.rbgames_grupo1.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomItem(
    val route: String, // ruta para navegar  (debe coincidir con la del navhost)
    val label: String, // texto que se muestra debajo del icono
    val icon: ImageVector // icono a mostrar en el menú
)
//composable para la barra inferior
@Composable
fun AppBottomBar(
    navController: NavHostController, // Controlador que usaremos para navegar/calc. selección
    items: List<BottomItem> // Lista de pestañas que debe renderizar la barra
) {
    // Obtenemos la entrada actual del back stack (ruta activa) como estado de Compose
    //backstackentry representa una entrada de pila de retroceso de un fragmentManager
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Del entry actual, tomamos el destino
    val currentDestination: NavDestination? = backStackEntry?.destination

    //contenedor de la barra inferior que contiene los items
    NavigationBar {
        // Iteramos por cada item declarado por el caller
        items.forEach { item ->
            // Calculamos si este item debe pintarse como "seleccionado".
            // Usamos 'hierarchy' para que funcione incluso si hay grafos anidados.
            val selected = currentDestination?.route == item.route
            // declaracion de items
            NavigationBarItem(
                selected = selected, // pinta el estado activo si corresponde
                onClick = {
                    //al hacer click navegamos a la ruta del item
                    navController.navigate(item.route) {
                        // popUpto al destino inicial del grafo para evitar apilar duplicados
                        //es para captar atencion de las personas
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // Guardamos estado del destino anterior
                        }
                        launchSingleTop = true // Evita múltiples copias del mismo destino en la cima
                        restoreState = true // Si existía estado guardado de esa pestaña, lo restaura
                    }
                },
                // Ícono del item
                icon = { Icon(item.icon, contentDescription = item.label) },
                // Texto debajo del ícono
                label = { Text(item.label) }
            )
        }
    }
}
