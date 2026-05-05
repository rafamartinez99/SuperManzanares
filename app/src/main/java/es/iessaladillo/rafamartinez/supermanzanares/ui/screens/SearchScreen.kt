package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import es.iessaladillo.rafamartinez.supermanzanares.utils.normalize
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController, viewModel: ProductViewModel, cartViewModel: CartViewModel
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val allProducts by viewModel.products.collectAsState()
    val cartItems by cartViewModel.cart.collectAsState()
    val filteredProducts by remember(query, allProducts) {
        derivedStateOf {
            if (query.length >= 2) {
                val normalizedQuery = normalize(query)
                allProducts.filter { normalize(it.name).contains(normalizedQuery) }
            } else emptyList()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar productos...", fontSize = 16.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredProducts) { product ->
                val cartItem = cartItems.firstOrNull { it.first.productId == product.id }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { navController.navigate("product_detail/${product.id}") },
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold)

                            if (product.discountPrice != null && product.originalPrice != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${
                                            String.format(
                                                Locale.forLanguageTag("es-ES"), "%.2f", product.originalPrice
                                            )
                                        }€",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))

                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${
                                                String.format(
                                                    Locale.forLanguageTag("es-ES"),
                                                    "%.2f",
                                                    product.discountPrice
                                                )
                                            }€",
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
                                        text = "${
                                            String.format(
                                                Locale.forLanguageTag("es-ES"), "%.2f", product.price
                                            )
                                        }€", fontSize = 16.sp, fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "/ud",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier.height(32.dp)) {
                                if (cartItem == null) {
                                    Button(
                                        onClick = { cartViewModel.addToCart(product.id) },
                                        modifier = Modifier
                                            .fillMaxWidth(0.45f)
                                            .height(32.dp),
                                        contentPadding = PaddingValues(5.dp),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Añadir", fontSize = 14.sp)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            cartViewModel.decreaseQuantity(
                                                product.id
                                            )
                                        }) {
                                            Icon(
                                                imageVector = if (cartItem.first.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                                contentDescription = if (cartItem.first.quantity == 1) "Eliminar" else "Disminuir",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Text(
                                            text = "${cartItem.first.quantity} uds.",
                                            fontSize = 16.sp
                                        )
                                        IconButton(onClick = { cartViewModel.addToCart(product.id) }) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Aumentar",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                }
            }

            if (filteredProducts.isEmpty() && query.length >= 2) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Sin resultados",
                            modifier = Modifier.size(50.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No se encontraron productos.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

