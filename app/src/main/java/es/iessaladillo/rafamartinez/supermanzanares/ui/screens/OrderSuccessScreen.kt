package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import kotlinx.coroutines.delay

@Composable
fun OrderSuccessScreen(navController: NavController, cartViewModel: CartViewModel) {
    var checkVisible by remember { mutableStateOf(false) }
    var titleVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var buttonsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        cartViewModel.clearCart()
        delay(150)
        checkVisible = true
        delay(450)
        titleVisible = true
        delay(250)
        subtitleVisible = true
        delay(200)
        buttonsVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = checkVisible,
            enter = fadeIn(tween(400)) + scaleIn(
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Pedido exitoso",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                tween(400), initialOffsetY = { it / 2 }
            )
        ) {
            Text(
                text = "¡Pedido realizado!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = subtitleVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                tween(400), initialOffsetY = { it / 2 }
            )
        ) {
            Text(
                text = "Tu pedido se está procesando.\nTe llegará en breve.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        AnimatedVisibility(
            visible = buttonsVisible,
            enter = fadeIn(tween(400)) + slideInVertically(
                tween(400), initialOffsetY = { it / 2 }
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        navController.navigate("home") {
                            popUpTo("order_success") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver al inicio")
                }

                OutlinedButton(
                    onClick = {
                        navController.navigate("order_history") {
                            popUpTo("order_success") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver historial de pedidos")
                }
            }
        }
    }
}
