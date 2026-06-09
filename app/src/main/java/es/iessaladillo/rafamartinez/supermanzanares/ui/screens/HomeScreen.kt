package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductCard
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductCardSkeleton
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductSection
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.SearchBar
import es.iessaladillo.rafamartinez.supermanzanares.ui.navigation.NavigationEvents
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    productViewModel: ProductViewModel, cartViewModel: CartViewModel, navController: NavController
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val ofertas by productViewModel.ofertas.collectAsStateWithLifecycle()
    val novedades by productViewModel.novedades.collectAsStateWithLifecycle()
    val isLoading by productViewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by productViewModel.isRefreshing.collectAsStateWithLifecycle()
    val cartItems by cartViewModel.cart.collectAsStateWithLifecycle()
    val cartQuantities = remember(cartItems) {
        cartItems.associate { (cartItem, _) -> cartItem.productId to cartItem.quantity }
    }
    val productRows = remember(products) { products.chunked(2) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        NavigationEvents.scrollToTop.collect { route ->
            if (route == "home") scope.launch { listState.animateScrollToItem(0) }
        }
    }

    val activity = LocalActivity.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000) {
            activity?.finish()
        } else {
            Toast.makeText(activity, "Pulsa atrás otra vez para salir", Toast.LENGTH_SHORT).show()
            lastBackPressTime = now
        }
    }

    val pullRefreshState = rememberPullToRefreshState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(
                "Supermercado Manzanares",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        })

        SearchBar(onClick = { navController.navigate("search") })

        when {
            isLoading -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            "Ofertas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                        )
                        LazyRow {
                            items(4) { ProductCardSkeleton() }
                        }
                    }
                    item {
                        Text(
                            "Novedades",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                        )
                        LazyRow {
                            items(4) { ProductCardSkeleton() }
                        }
                    }
                    item {
                        Text(
                            "Todos los productos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(4) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(Modifier.weight(1f)) { ProductCardSkeleton() }
                            Box(Modifier.weight(1f)) { ProductCardSkeleton() }
                        }
                    }
                }
            }

            products.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text("No hay productos disponibles", style = MaterialTheme.typography.titleMedium)
            }

            else -> {
                PullToRefreshBox(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = { productViewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                        item {
                            ProductSection(
                                title = "Ofertas",
                                products = ofertas,
                                cartQuantities = cartQuantities,
                                onAdd = { productId -> cartViewModel.addToCart(productId) },
                                onRemove = { productId -> cartViewModel.decreaseQuantity(productId) },
                                navController = navController
                            )
                        }
                        item {
                            ProductSection(
                                title = "Novedades",
                                products = novedades,
                                cartQuantities = cartQuantities,
                                onAdd = { productId -> cartViewModel.addToCart(productId) },
                                onRemove = { productId -> cartViewModel.decreaseQuantity(productId) },
                                navController = navController
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Todos los productos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(
                            items = productRows,
                            key = { rowProducts -> rowProducts.joinToString("-") { it.id } }
                        ) { rowProducts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null,
                                        placementSpec = spring(stiffness = Spring.StiffnessLow)
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (product in rowProducts) {
                                    val quantity = cartQuantities[product.id] ?: 0
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp)
                                    ) {
                                        ProductCard(
                                            product = product,
                                            quantity = quantity,
                                            onAdd = { cartViewModel.addToCart(product.id) },
                                            onRemove = { cartViewModel.decreaseQuantity(product.id) },
                                            navController = navController
                                        )
                                    }
                                }
                                if (rowProducts.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
