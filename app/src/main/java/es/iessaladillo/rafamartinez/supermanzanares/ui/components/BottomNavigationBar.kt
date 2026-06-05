package es.iessaladillo.rafamartinez.supermanzanares.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import es.iessaladillo.rafamartinez.supermanzanares.ui.navigation.NavigationEvents
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.AuthViewModel

@Composable
private fun defaultNavBarColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer
)

@Composable
fun BottomNavigationBar(
    navController: NavController, authViewModel: AuthViewModel, modifier: Modifier = Modifier
) {
    val isAuthenticated by authViewModel.authState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profileRoutes = listOf(
        "profile", "login", "register", "edit_profile", "edit_name",
        "edit_password", "edit_email", "edit_address", "add_address_from_edit",
        "add_address_from_register", "order_history", "forgot_password", "add_address_from_google"
    )

    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.heightIn(min = 80.dp),
            windowInsets = WindowInsets(0)
        ) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                label = { Text("Inicio") },
                selected = currentRoute == "home",
                onClick = {
                    if (currentRoute == "home") {
                        NavigationEvents.scrollToTop.tryEmit("home")
                    } else {
                        navController.navigate("home")
                    }
                },
                colors = defaultNavBarColors()
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Categorías") },
                label = { Text("Categorías") },
                selected = currentRoute == "categories" || currentRoute == "productsByCategory/{categoryId}",
                onClick = {
                    if (currentRoute == "categories") {
                        NavigationEvents.scrollToTop.tryEmit("categories")
                    } else {
                        navController.navigate("categories")
                    }
                },
                colors = defaultNavBarColors()
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Listas") },
                label = { Text("Listas") },
                selected = currentRoute == "lists" || currentRoute == "list_detail/{listId}",
                onClick = {
                    if (currentRoute == "lists") {
                        NavigationEvents.scrollToTop.tryEmit("lists")
                    } else {
                        navController.navigate("lists")
                    }
                },
                colors = defaultNavBarColors()
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Cuenta") },
                label = { Text("Cuenta") },
                selected = currentRoute in profileRoutes,
                onClick = {
                    if (isAuthenticated) {
                        navController.navigate("profile") {
                            popUpTo("profile") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                colors = defaultNavBarColors()
            )
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
