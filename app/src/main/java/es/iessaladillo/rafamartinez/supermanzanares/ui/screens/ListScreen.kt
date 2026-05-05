package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ShoppingListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(shoppingListViewModel: ShoppingListViewModel, navController: NavController) {
    val shoppingLists by shoppingListViewModel.shoppingLists.collectAsState(initial = emptyList())
    val allProducts by shoppingListViewModel.allProducts.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(
                "Listas", fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
        }, actions = {
            Text(
                text = "Crear lista",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { showDialog = true })
        })

        Box(modifier = Modifier.weight(1f)) {
            if (shoppingLists.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlaylistRemove,
                        contentDescription = "Sin listas de compra",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No tienes listas de compras aún",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn {
                    items(shoppingLists) { list ->
                        val items by shoppingListViewModel.observeItemsForList(list.id).collectAsState()
                        val productImages =
                            items.mapNotNull { item -> allProducts.find { it.id == item.productId }?.imageUrl }
                                .take(4)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { navController.navigate("list_detail/${list.id}") },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.2f)
                                ) {
                                    repeat(2) { row ->
                                        Row {
                                            repeat(2) { col ->
                                                val index = row * 2 + col
                                                if (index < productImages.size) {
                                                    AsyncImage(
                                                        model = productImages[index],
                                                        contentDescription = "Producto",
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(2.dp)
                                                            .aspectRatio(1f),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(2.dp)
                                                            .aspectRatio(1f)
                                                            .background(MaterialTheme.colorScheme.background)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = list.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Productos: ${items.size}",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Lista") },
            text = {
                TextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nombre de la lista") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newListName.isNotBlank()) {
                                shoppingListViewModel.createShoppingList(newListName)
                                newListName = ""
                                showDialog = false
                            }
                        }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

            },
            confirmButton = {
                Button(onClick = {
                    if (newListName.isNotEmpty()) {
                        shoppingListViewModel.createShoppingList(newListName)
                        newListName = ""
                        showDialog = false
                    }
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
