package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.OrderViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    userViewModel: UserViewModel
) {
    val cartItemsWithProducts by cartViewModel.cart.collectAsState(initial = emptyList())
    val total = cartItemsWithProducts.sumOf { (cartItem, product) ->
        (product?.price ?: 0.0) * cartItem.quantity
    }
    var deliveryOption by remember { mutableStateOf("Envío a domicilio") }
    var paymentMethod by remember { mutableStateOf("Tarjeta de crédito") }
    val user by userViewModel.user.collectAsState()
    val userId = user?.id ?: ""
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Pedido", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Selecciona el método de entrega:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = deliveryOption == "Envío a domicilio", onClick = {
                    deliveryOption = "Envío a domicilio"
                })
                Text("Envío a domicilio")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = deliveryOption == "Recoger en tienda", onClick = {
                    deliveryOption = "Recoger en tienda"
                })
                Text("Recoger en tienda")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Método de pago:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = paymentMethod == "Tarjeta de crédito", onClick = {
                    paymentMethod = "Tarjeta de crédito"
                })
                Text("Tarjeta de crédito")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = paymentMethod == "Efectivo", onClick = {
                    paymentMethod = "Efectivo"
                })
                Text("Efectivo")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen del pedido:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nombre: ${user?.name}")
                    Text("Dirección: ${user?.address}, Nº ${user?.number}${if (!user?.floor.isNullOrBlank()) ", Piso ${user?.floor}" else ""}")
                    Text("Método de entrega: $deliveryOption")
                    Text("Método de pago: $paymentMethod")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total: ${String.format(Locale.forLanguageTag("es-ES"), "%.2f", total)} €", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "💡 El pago es simulado. No se requieren datos reales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        val order = orderViewModel.createOrder(cartItemsWithProducts)
                        orderViewModel.placeOrder(userId, order)
                        delay(1500)
                        isLoading = false
                        navController.navigate("order_success")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Procesando..." else "Finalizar compra")
            }
        }
    }

    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {},
            title = { Text("Procesando pago...") },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            })
    }
}