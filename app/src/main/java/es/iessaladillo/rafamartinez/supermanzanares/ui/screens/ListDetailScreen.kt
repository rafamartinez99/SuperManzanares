package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ShoppingListViewModel
import es.iessaladillo.rafamartinez.supermanzanares.utils.normalize
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Int, shoppingListViewModel: ShoppingListViewModel, navController: NavController
) {
    val items by shoppingListViewModel.observeItemsForList(listId).collectAsState()
    val allProducts by shoppingListViewModel.allProducts.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val shoppingList =
        shoppingListViewModel.getShoppingListById(listId).collectAsState(initial = null).value
    val listName = shoppingList?.name ?: "Lista"
    val totalProducts = items.size

    val normalizedQuery = normalize(searchQuery)
    val filteredProducts = allProducts.filter { normalize(it.name).contains(normalizedQuery) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(
                listName, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Total de productos: $totalProducts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = if (items.isNotEmpty()) Arrangement.SpaceBetween else Arrangement.Center
            ) {
                if (items.isNotEmpty()) {
                    Column(
                        modifier = Modifier.clickable { shoppingListViewModel.addAllToCart(listId) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Añadir todos al carrito",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "Añadir todo al carrito", fontSize = 12.sp)
                    }
                }

                Column(
                    modifier = Modifier.clickable {
                        showDeleteDialog = true
                    }, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar lista",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = "Eliminar lista", fontSize = 12.sp)
                }
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar productos...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (searchQuery.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    items(filteredProducts) { product ->
                        val isInList = items.any { it.productId == product.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { navController.navigate("product_detail/${product.id}") },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(product.imageUrl).crossfade(true).build(),
                                    contentDescription = "Imagen del producto",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(modifier = Modifier.width(15.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, fontWeight = FontWeight.Bold)

                                    if (product.discountPrice != null && product.originalPrice != null) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${
                                                    String.format(
                                                        Locale.forLanguageTag("es-ES"),
                                                        "%.2f",
                                                        product.originalPrice
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
                                                        Locale.forLanguageTag("es-ES"), "%.2f",
                                                        product.price
                                                    )
                                                }€",
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

                                IconButton(onClick = {
                                    if (isInList) {
                                        shoppingListViewModel.removeItemFromList(listId, product.id)
                                    } else {
                                        shoppingListViewModel.addProductToList(listId, product.id)
                                        searchQuery = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isInList) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = if (isInList) "Producto añadido" else "Añadir",
                                        tint = if (isInList) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = "Lista vacía",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "No hay productos en esta lista.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(items) { item ->
                        val product = allProducts.find { it.id == item.productId }
                        val quantity = item.quantity

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate("product_detail/${item.productId}")
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .padding(start = 4.dp)
                                ) {
                                    AsyncImage(
                                        model = product?.imageUrl ?: "",
                                        contentDescription = "Imagen del producto",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Fit
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = product?.name ?: "Cargando...",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (product?.discountPrice != null && product.originalPrice != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${
                                                        String.format(
                                                            Locale.forLanguageTag("es-ES"),
                                                            "%.2f",
                                                            product.originalPrice
                                                        )
                                                    }€",
                                                    fontSize = 14.sp,
                                                    color = Color.Gray,
                                                    textDecoration = TextDecoration.LineThrough
                                                )

                                                Spacer(modifier = Modifier.width(2.dp))

                                                Row(verticalAlignment = Alignment.Bottom) {
                                                    Text(
                                                        text = "${
                                                            String.format(
                                                                Locale.forLanguageTag("es-ES"),
                                                                "%.2f",
                                                                product.discountPrice
                                                            )
                                                        }€",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Text(
                                                        text = "/ud",
                                                        fontSize = 14.sp,
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
                                                            Locale.forLanguageTag("es-ES"),
                                                            "%.2f",
                                                            product?.price
                                                        )
                                                    }€",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "/ud",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        if (quantity > 1) {
                                            IconButton(
                                                onClick = {
                                                    shoppingListViewModel.decreaseQuantityInList(
                                                        listId,
                                                        item.productId,
                                                    )
                                                }) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = "Disminuir cantidad",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Text(
                                            text = "$quantity",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        IconButton(onClick = {
                                            shoppingListViewModel.increaseQuantityInList(
                                                listId, item.productId
                                            )
                                        }) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Aumentar cantidad",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        shoppingListViewModel.removeItemFromList(
                                            listId, item.productId
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {
                                            shoppingListViewModel.addToCart(
                                                item.productId, quantity
                                            )
                                        }, modifier = Modifier.fillMaxWidth(0.9f)

                                    ) {
                                        Text("Añadir $quantity ud. al carrito")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar lista") },
            text = { Text("¿Estás seguro de que quieres eliminar esta lista? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        shoppingListViewModel.deleteList(listId)
                        navController.popBackStack()
                    }, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

