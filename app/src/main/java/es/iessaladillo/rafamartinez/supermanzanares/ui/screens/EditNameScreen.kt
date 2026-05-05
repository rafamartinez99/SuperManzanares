package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import es.iessaladillo.rafamartinez.supermanzanares.utils.formatName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNameScreen(
    navController: NavController, userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val apellidoFocus = remember { FocusRequester() }
    val camposValidos = nombre.isNotBlank() && apellidos.isNotBlank()


    LaunchedEffect(user) {
        user?.let {
            val parts = it.name.split(" ", limit = 2)
            nombre = parts.getOrElse(0) { "" }
            apellidos = parts.getOrElse(1) { "" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar nombre", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            userViewModel.updateUserProfile(
                                formatName("$nombre $apellidos"),
                                user?.address ?: "",
                                user?.number ?: "",
                                user?.floor ?: ""
                            )
                            navController.popBackStack()
                        }, enabled = camposValidos
                    ) {
                        Text(
                            "Guardar", color = if (camposValidos) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                })
        }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Nombre", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Indícanos tu nombre y apellidos para localizarte en relación a tu pedido.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it.replaceFirstChar { c -> c.uppercase() }
                },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { apellidoFocus.requestFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = apellidos,
                onValueChange = {
                    apellidos = it.replaceFirstChar { c -> c.uppercase() }
                },
                label = { Text("Apellidos") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(apellidoFocus),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (camposValidos) {
                            userViewModel.updateUserProfile(
                                formatName("$nombre $apellidos"),
                                user?.address ?: "",
                                user?.number ?: "",
                                user?.floor ?: ""
                            )
                            navController.popBackStack()
                        }
                    })
            )
        }
    }
}
