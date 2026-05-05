package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductCard
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductSection
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.SearchBar
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    productViewModel: ProductViewModel, cartViewModel: CartViewModel, navController: NavController
) {
    val products by productViewModel.products.collectAsState()
    val ofertas by productViewModel.ofertas.collectAsState()
    val novedades by productViewModel.novedades.collectAsState()

    val activity = LocalActivity.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val now = System.currentTimeMillis()
        if (now - lastBackPressTime < 2000) {
            activity?.finish()
        } else {
            lastBackPressTime = now
            Toast.makeText(activity, "Pulsa atrás otra vez para salir", Toast.LENGTH_SHORT).show()
        }
    }


    Column(modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(title = {
            Text(
                "Supermercado Manzanares",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        })

        SearchBar(onClick = {
            navController.navigate("search")
        })

        if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    ProductSection(
                        title = "Ofertas",
                        products = ofertas,
                        cartViewModel = cartViewModel,
                        navController = navController
                    )
                }

                item {
                    ProductSection(
                        title = "Novedades",
                        products = novedades,
                        cartViewModel = cartViewModel,
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

                items(products.chunked(2)) { rowProducts ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null,
                                placementSpec = spring(stiffness = Spring.StiffnessLow)
                            ), horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (product in rowProducts) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            ) {
                                ProductCard(
                                    product = product,
                                    cartViewModel = cartViewModel,
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


