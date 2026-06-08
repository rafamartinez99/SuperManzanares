package es.iessaladillo.rafamartinez.supermanzanares.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.utils.formatPrice
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel

@Composable
fun CartItem(cartItem: CartItemEntity, product: ProductEntity?, cartViewModel: CartViewModel, navController: NavController) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val imageRequest = remember(product?.imageUrl) {
        ImageRequest.Builder(context)
            .data(product?.imageUrl)
            .size(150, 150)
            .precision(Precision.INEXACT)
            .crossfade(true)
            .build()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { product?.let { navController.navigate("product_detail/${it.id}") } },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = product?.name,
                modifier = Modifier.size(50.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product?.name ?: "Producto desconocido", fontWeight = FontWeight.Bold)
                if (product?.discountPrice != null && product.originalPrice != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatPrice(product.originalPrice),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = formatPrice(product.discountPrice),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "/ud",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        }

                    }
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatPrice(product?.price),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/ud",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (cartItem.quantity > 1) {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        cartViewModel.decreaseQuantity(cartItem.productId)
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad", tint = MaterialTheme.colorScheme.error)
                    }
                } else {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        cartViewModel.removeFromCart(cartItem)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar del carrito", tint = MaterialTheme.colorScheme.error)
                    }
                }
                Text(text = "${cartItem.quantity} uds.", fontWeight = FontWeight.Bold)
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    cartViewModel.addToCart(cartItem.productId)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
