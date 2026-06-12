package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.FullScreenZoomImage
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductCard
import es.iessaladillo.rafamartinez.supermanzanares.utils.formatPrice
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ShoppingListViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    shoppingListViewModel: ShoppingListViewModel
) {
    val productFlow =
        remember(productViewModel, productId) { productViewModel.getProductById(productId) }
    val cartItemFlow =
        remember(cartViewModel, productId) { cartViewModel.getCartItemById(productId) }
    val relatedProductsFlow =
        remember(productViewModel, productId) { productViewModel.getRelatedProducts(productId) }

    val product by productFlow.collectAsStateWithLifecycle(initialValue = null)
    val cartItem by cartItemFlow.collectAsStateWithLifecycle(initialValue = null)
    val relatedProducts by relatedProductsFlow.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.cart.collectAsStateWithLifecycle()
    val cartQuantities = remember(cartItems) {
        cartItems.associate { (cartItem, _) -> cartItem.productId to cartItem.quantity }
    }


    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }
    var savedListName by remember { mutableStateOf("") }
    var newListName by remember { mutableStateOf("") }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    product?.let { selectedProduct ->
        val productImageRequest = remember(selectedProduct.imageUrl) {
            ImageRequest.Builder(context)
                .data(selectedProduct.imageUrl)
                .size(900, 900)
                .precision(Precision.INEXACT)
                .crossfade(true)
                .build()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(title = {
                Text(
                    selectedProduct.name, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
            })
            LazyColumn(
                modifier = Modifier.fillMaxSize(), state = listState
            ) {
                item {
                    AsyncImage(
                        model = productImageRequest,
                        contentDescription = selectedProduct.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 300.dp)
                            .clickable { showFullScreenImage = true })
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                selectedProduct.name, fontWeight = FontWeight.Bold, fontSize = 22.sp
                            )

                            Text(
                                "${
                                    String.format(
                                        Locale.forLanguageTag("es-ES"),
                                        "%.2f",
                                        selectedProduct.weight
                                    )
                                } kg | ${
                                    String.format(
                                        Locale.forLanguageTag("es-ES"),
                                        "%.2f",
                                        selectedProduct.price / selectedProduct.weight
                                    )
                                } €/kg", fontSize = 16.sp, color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedProduct.discountPrice != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatPrice(selectedProduct.originalPrice),
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = formatPrice(selectedProduct.discountPrice),
                                            fontSize = 18.sp,
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
                                        text = formatPrice(selectedProduct.price),
                                        fontSize = 18.sp,
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

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        showSaveDialog = true
                                    },
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = "Guardar en listas"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Guardar en listas")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            AnimatedContent(
                                targetState = cartItem == null,
                                transitionSpec = {
                                    (fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.85f)) togetherWith
                                            (fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.85f))
                                },
                                label = "cart_action"
                            ) { isNull ->
                                if (isNull) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            cartViewModel.addToCart(selectedProduct.id)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Añadir al carrito")
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "En carrito",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        IconButton(onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (cartItem?.quantity == 1) {
                                                cartViewModel.removeFromCart(selectedProduct.id)
                                            } else {
                                                cartViewModel.decreaseQuantity(selectedProduct.id)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (cartItem?.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                                contentDescription = if (cartItem?.quantity == 1) "Eliminar" else "Disminuir",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        Text(
                                            "${cartItem?.quantity ?: 0} uds.",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            cartViewModel.addToCart(selectedProduct.id)
                                        }) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Añadir",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            HorizontalDivider()

                            Text(
                                text = "Información",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )

                            Text(
                                text = selectedProduct.description ?: "Sin descripción disponible",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AnimatedVisibility(visible = expanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    HorizontalDivider()

                                    Text(
                                        "Ingredientes", style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        selectedProduct.ingredients.joinToString(", ")
                                            .ifEmpty { "No disponible" },
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        "Valores nutricionales por 100g",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 12.dp)
                                    )

                                    val n = selectedProduct.nutrition
                                    Column {
                                        Text("Energía: ${n.energyKcal} kcal")
                                        Text("Grasas: ${n.fats} g (saturadas: ${n.saturatedFats} g)")
                                        Text("Hidratos: ${n.carbohydrates} g (azúcares: ${n.sugars} g)")
                                        Text("Fibra: ${n.fiber} g")
                                        Text("Proteínas: ${n.protein} g")
                                        Text("Sal: ${n.salt} g")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                TextButton(onClick = {
                                    expanded = !expanded
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(index = if (expanded) listState.layoutInfo.totalItemsCount - 1 else 0)
                                    }
                                }) {
                                    Icon(
                                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (expanded) "Mostrar menos" else "Mostrar más")
                                }
                            }

                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Productos relacionados:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(relatedProducts, key = { it.id }) { relatedProduct ->
                            ProductCard(
                                product = relatedProduct,
                                quantity = cartQuantities[relatedProduct.id] ?: 0,
                                onAdd = { cartViewModel.addToCart(relatedProduct.id) },
                                onRemove = { cartViewModel.decreaseQuantity(relatedProduct.id) },
                                navController = navController
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showSaveDialog) {
            ModalBottomSheet(
                onDismissRequest = { showSaveDialog = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .imePadding()
                ) {
                    Text("Guardar en una lista", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Selecciona una lista en la que quieres guardar este producto.")

                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn {
                        items(shoppingLists, key = { it.id }) { list ->
                            var isInList by remember { mutableStateOf(false) }

                            LaunchedEffect(list.id) {
                                isInList = shoppingListViewModel.isProductInList(list.id, productId)
                            }

                            Row(modifier = Modifier.animateItem()
                                .fillMaxWidth()
                                .clickable {
                                    if (!isInList) {
                                        shoppingListViewModel.addProductToList(
                                            list.id, productId
                                        )
                                        savedListName = list.name
                                        showSaveDialog = false
                                        showConfirmationDialog = true
                                    }
                                }
                                .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    list.name,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isInList) {
                                    Text(
                                        "✔ En la lista",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text("Nueva lista") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newListName.isNotBlank()) {
                                coroutineScope.launch {
                                    shoppingListViewModel.createListAndAddProduct(
                                        newListName, productId
                                    )
                                    savedListName = newListName
                                    newListName = ""
                                    showSaveDialog = false
                                    showConfirmationDialog = true
                                }
                            }

                        })
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newListName.isNotBlank()) {
                                coroutineScope.launch {
                                    shoppingListViewModel.createListAndAddProduct(
                                        newListName, productId
                                    )
                                    savedListName = newListName
                                    newListName = ""
                                    showSaveDialog = false
                                    showConfirmationDialog = true
                                }
                            }

                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear lista")
                    }
                }
            }
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = { Text("Guardado en $savedListName") },
                text = { Text("El producto se ha guardado correctamente.") },
                confirmButton = {
                    Button(onClick = { showConfirmationDialog = false }) {
                        Text("Aceptar")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface  // <-- este es el cambio
            )
        }

        if (showFullScreenImage) {
            FullScreenZoomImage(
                imageUrl = selectedProduct.imageUrl, onDismiss = { showFullScreenImage = false })
        }


    }
}
