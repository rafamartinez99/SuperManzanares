package es.iessaladillo.rafamartinez.supermanzanares.ui

import BottomBarsLayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import es.iessaladillo.rafamartinez.supermanzanares.ui.navigation.SuperManzanaresNavGraph
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.*


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
    val isCartEmpty by cartViewModel.isCartEmpty.collectAsState()
    val cartItemCount by cartViewModel.cartItemCount.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()

    val showBottomBar by remember(currentRoute) {
        derivedStateOf {
            currentRoute !in listOf(
                "product_detail/{productId}", "cart", "order_confirmation", "order_success"
            )
        }
    }

    val showCartBarState by remember(currentRoute, isCartEmpty) {
        derivedStateOf {
            !isCartEmpty && currentRoute !in listOf(
                "cart",
                "profile",
                "login",
                "register",
                "edit_profile",
                "edit_name",
                "edit_password",
                "edit_email",
                "edit_address",
                "add_address_from_edit",
                "add_address_from_register",
                "add_address_from_google",
                "product_detail/{productId}",
                "order_confirmation",
                "order_success",
                "forgot_password"
            )
        }
    }
    Box {
        Scaffold(
            contentWindowInsets = WindowInsets(0)   // importante aquí
        ) { innerPadding ->
            SuperManzanaresNavGraph(
                navController,
                innerPadding,
                cartViewModel,
                authViewModel,
                productViewModel,
                userViewModel,
                orderViewModel,
                shoppingListViewModel,
                categoryViewModel,
                mapboxViewModel,
                showBottomBar      // NUEVO

            )
        }
        BottomBarsLayer(
            navController = navController,
            authViewModel = authViewModel,
            cartItemCount = cartItemCount,
            totalPrice = totalPrice,
            showBottomBar = showBottomBar,
            showCartBarState = showCartBarState,
        )
    }
}






