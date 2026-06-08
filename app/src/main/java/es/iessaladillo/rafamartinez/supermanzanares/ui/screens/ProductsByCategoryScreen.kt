package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.ProductCard
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsByCategoryScreen(
    categoryId: Int,
    productViewModel: ProductViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    navController: NavController,
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val products by productViewModel.products.collectAsStateWithLifecycle()
    val filteredProducts = remember(products, categoryId) {
        products.filter { it.categoryId == categoryId }
    }
    val cartItems by cartViewModel.cart.collectAsStateWithLifecycle()
    val cartQuantities = remember(cartItems) {
        cartItems.associate { (cartItem, _) -> cartItem.productId to cartItem.quantity }
    }
    val categories by categoryViewModel.categories.collectAsStateWithLifecycle()
    val categoryName = remember(categories, categoryId) {
        categories.find { it.id == categoryId }?.name ?: "Categoría"
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(categoryName, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                }
            }
        )
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredProducts,
                    key = { product -> product.id }
                ) { product ->
                    val quantity = cartQuantities[product.id] ?: 0
                    ProductCard(
                        product = product,
                        quantity = quantity,
                        onAdd = { cartViewModel.addToCart(product.id) },
                        onRemove = { cartViewModel.decreaseQuantity(product.id) },
                        navController = navController,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}
