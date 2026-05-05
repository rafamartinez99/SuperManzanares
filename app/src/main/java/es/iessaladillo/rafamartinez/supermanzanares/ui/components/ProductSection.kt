package es.iessaladillo.rafamartinez.supermanzanares.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel

@Composable
fun ProductSection(
    title: String,
    products: List<Product>,
    cartViewModel: CartViewModel,
    navController: NavController
) {
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    cartViewModel = cartViewModel,
                    navController = navController
                )
            }

        }
    }
}



