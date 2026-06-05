package es.iessaladillo.rafamartinez.supermanzanares.ui

import BottomBarsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.iessaladillo.rafamartinez.supermanzanares.ui.navigation.SuperManzanaresNavGraph
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.*

private val routesWithBottomBar = setOf(
    "home", "categories", "productsByCategory/{categoryId}",
    "search", "lists", "list_detail/{listId}", "order_history"
)

private const val BOTTOM_NAV_HEIGHT_DP = 80
private const val CART_BAR_HEIGHT_DP = 64

@Composable
fun SuperManzanaresApp(navController: NavHostController = rememberNavController()) {
    val cartViewModel: CartViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val productViewModel: ProductViewModel = hiltViewModel()
    val userViewModel: UserViewModel = hiltViewModel()
    val orderViewModel: OrderViewModel = hiltViewModel()
    val shoppingListViewModel: ShoppingListViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()
    val mapboxViewModel: MapboxViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isCartEmpty by cartViewModel.isCartEmpty.collectAsStateWithLifecycle()

    val systemNavBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val showsBottomBar = currentRoute in routesWithBottomBar
    val showsCartBar = showsBottomBar && !isCartEmpty

    val targetBottomPadding = when {
        showsCartBar -> BOTTOM_NAV_HEIGHT_DP.dp + CART_BAR_HEIGHT_DP.dp + systemNavBarHeight
        showsBottomBar -> BOTTOM_NAV_HEIGHT_DP.dp + systemNavBarHeight
        else -> systemNavBarHeight
    }

    val contentBottomPadding by animateDpAsState(
        targetValue = targetBottomPadding,
        animationSpec = tween(durationMillis = 300),
        label = "contentBottomPadding"
    )

    Box {
        Scaffold(
            contentWindowInsets = WindowInsets(0)
        ) { innerPadding ->
            SuperManzanaresNavGraph(
                navController,
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = contentBottomPadding
                ),
                cartViewModel,
                authViewModel,
                productViewModel,
                userViewModel,
                orderViewModel,
                shoppingListViewModel,
                categoryViewModel,
                mapboxViewModel
            )
        }
        BottomBarsLayer(
            navController = navController,
            authViewModel = authViewModel,
            cartViewModel = cartViewModel,
        )
    }
}
