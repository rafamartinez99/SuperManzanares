import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.BottomNavigationBar
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.CartBar
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.AuthViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import kotlin.collections.contains

@Composable
fun BottomBarsLayer(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cartViewModel: CartViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isCartEmpty by cartViewModel.isCartEmpty.collectAsStateWithLifecycle()
    val cartItemCount by cartViewModel.cartItemCount.collectAsStateWithLifecycle()
    val totalPrice by cartViewModel.totalPrice.collectAsStateWithLifecycle()

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
    val navBarHeight = 80.dp
    val travel = 500.dp

    // Estado animación CartBar (igual que tenías)
    var showCartBarAnim by remember { mutableStateOf(false) }
    var wasVisible by remember { mutableStateOf(false) }

    LaunchedEffect(showCartBarState) {
        if (showCartBarState) {
            showCartBarAnim = true
        } else {
            wasVisible = showCartBarAnim
        }
    }

    val horizontalOffset by animateDpAsState(
        targetValue = when {
            showCartBarState -> 0.dp
            wasVisible -> travel
            else -> -travel
        }, animationSpec = tween(durationMillis = 400), finishedListener = { final ->
            if (!showCartBarState && final == travel) {
                showCartBarAnim = false
            }
        }, label = "cartBarOffsetX"
    )

    Box(
        Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = showBottomBar,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 180),
                initialOffsetY = { it }
            ),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 180),
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(
                navController = navController,
                authViewModel = authViewModel,
                modifier = Modifier
            )
        }

        if (showCartBarAnim || showCartBarState) {
            CartBar(
                cartItemCount = cartItemCount,
                totalPrice = totalPrice,
                onCartClick = { navController.navigate("cart") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .offset(
                        x = horizontalOffset, y = -navBarHeight
                    )
            )
        }
    }
}
