package es.iessaladillo.rafamartinez.supermanzanares.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.R
import es.iessaladillo.rafamartinez.supermanzanares.utils.formatPrice


@Composable
fun ProductCard(
    product: Product, cartViewModel: CartViewModel, navController: NavController
) {
    val cartItems by cartViewModel.cart.collectAsState()
    val cartItem = cartItems.firstOrNull { it.first.productId == product.id }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .height(250.dp)
            .width(200.dp)
            .clickable { navController.navigate("product_detail/${product.id}") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.placeholder),
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Zona media: precio
            if (product.discountPrice != null && product.originalPrice != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
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
                        text = formatPrice(product.price),
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

            // Zona inferior: botón o cantidad
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.height(32.dp)) {
                if (cartItem == null) {
                    Button(
                        onClick = { cartViewModel.addToCart(product.id) },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(32.dp),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text("Añadir", fontSize = 14.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { cartViewModel.decreaseQuantity(product.id) }) {
                            Icon(
                                imageVector = if (cartItem.first.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                contentDescription = if (cartItem.first.quantity == 1) "Eliminar" else "Disminuir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = "${cartItem.first.quantity} uds.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        IconButton(
                            onClick = { cartViewModel.addToCart(product.id) }) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
