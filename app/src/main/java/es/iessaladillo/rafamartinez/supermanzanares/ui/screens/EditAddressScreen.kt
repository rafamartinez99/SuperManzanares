package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    navController: NavController, userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.let { handle ->
            handle.get<String>("edit_address")?.let { address ->
                handle.get<String>("edit_number")?.let { number ->
                    handle.get<String>("edit_floor")?.let { floor ->
                        userViewModel.updateUserProfile(
                            name = user?.name.orEmpty(),
                            address = address,
                            number = number,
                            floor = floor
                        )
                    }
                }
            }
        }
    }


    Scaffold(topBar = {
        TopAppBar(title = { Text("Direcciones", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }, actions = {
            TextButton(onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("prefill_address", user?.address)
                    set("prefill_number", user?.number)
                    set("prefill_floor", user?.floor)
                }
                navController.navigate("add_address_from_edit")
            }) {
                Text("Editar")
            }

        })
    }, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (user?.address.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOff,
                        contentDescription = "Sin dirección",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(bottom = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("No hay dirección", fontWeight = FontWeight.Bold)
                    Text("Añade dónde quieres recibir tu pedido.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        navController.navigate("add_address_from_edit")
                    }) {
                        Text("Añadir dirección")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Dirección guardada", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Dirección: ${user?.address}")
                        Text("Número: ${user?.number}")
                        if (!user?.floor.isNullOrBlank()) {
                            Text("Piso / Letra: ${user?.floor}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        userViewModel.updateUserProfile(
                            name = user?.name.orEmpty(), address = "", number = "", floor = ""
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar("Dirección eliminada")
                        }
                    }, modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar dirección")
                }
            }
        }
    }
}
