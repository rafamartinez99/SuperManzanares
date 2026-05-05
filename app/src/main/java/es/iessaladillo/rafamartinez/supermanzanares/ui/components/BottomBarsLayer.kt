import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemGesturesPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.BottomNavigationBar
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.CartBar
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.AuthViewModel

@Composable
fun BottomBarsLayer(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    cartItemCount: Int,
    totalPrice: Double,
    showBottomBar: Boolean,
    showCartBarState: Boolean
) {
    val navBarHeight = 80.dp
    val travel = 500.dp
    val hideOffset = navBarHeight + 40.dp

    // Animación vertical de la bottom bar
    val bottomBarOffset by animateDpAsState(
        targetValue = if (showBottomBar) 0.dp else hideOffset,
        animationSpec = tween(durationMillis = 400),
        label = "bottomBarOffsetY"
    )

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
                wasVisible = false
            }
        }, label = "cartBarOffsetX"
    )

    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        BottomNavigationBar(
            navController = navController,
            authViewModel = authViewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = bottomBarOffset)
        )

        if (showCartBarAnim || showCartBarState) {
            CartBar(
                cartItemCount = cartItemCount,
                totalPrice = totalPrice,
                onCartClick = { navController.navigate("cart") },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(
                        x = horizontalOffset, y = -navBarHeight
                    )
            )
        }
    }
}
