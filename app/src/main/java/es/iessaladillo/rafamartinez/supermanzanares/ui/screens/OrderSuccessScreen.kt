package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel

@Composable
fun OrderSuccessScreen(navController: NavController, cartViewModel: CartViewModel) {
    val animationSpec: FiniteAnimationSpec<Float> = remember { tween(durationMillis = 300) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cartViewModel.clearCart()
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = animationSpec) + scaleIn(animationSpec = animationSpec)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Pedido Exitoso",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = visible, enter = fadeIn(animationSpec = animationSpec)
        ) {
            Text(
                text = "¡Pedido realizado con éxito!",
                fontSize = 26.sp,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedVisibility(
            visible = visible, enter = fadeIn(animationSpec = animationSpec)
        ) {
            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo("order_success") { inclusive = true }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al inicio")
            }
        }
    }
}

