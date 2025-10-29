package com.example.rbgames_grupo1.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    navController: NavHostController,
    items: List<BottomItem>,
    badges: Map<String, Int> = emptyMap(),
    extraEndItem: BottomItem? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination: NavDestination? = backStackEntry?.destination
    val currentRoute = currentDestination?.route?.substringBefore("?")

    // Mezclamos items base + el opcional al final (si existe)
    val allItems = if (extraEndItem != null) items + extraEndItem else items

    NavigationBar {
        allItems.forEach { item ->
            val selected = currentRoute == item.route
            val count = badges[item.route] ?: 0

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (count > 0) {
                        BadgedBox(badge = { Badge { Text(count.toString()) } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) }
            )
        }
    }
}
