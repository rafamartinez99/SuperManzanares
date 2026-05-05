package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    navController: NavController, authViewModel: AuthViewModel
) {
    val name = remember { mutableStateOf("") }
    val surname = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val number = remember { mutableStateOf("") }
    val floor = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val repeatPassword = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val repeatPasswordVisible = remember { mutableStateOf(false) }

    val message by authViewModel.message.collectAsState()
    val isAuthenticated by authViewModel.authState.collectAsState()

    var passwordError by remember { mutableStateOf(false) }
    var repeatPasswordError by remember { mutableStateOf(false) }

    var showVerificationDialog by remember { mutableStateOf(false) }

    val surNameFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val repeatPasswordFocus = remember { FocusRequester() }
    val registerButtonFocus = remember { FocusRequester() }
    val isFormValid by remember {
        derivedStateOf {
            name.value.isNotBlank() && surname.value.isNotBlank() && email.value.isNotBlank() && address.value.isNotBlank() && password.value.length >= 6 && password.value == repeatPassword.value
        }
    }


    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            showVerificationDialog = true
        }
    }

    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry?.savedStateHandle?.let { handle ->

            handle.get<String>("prefill_name")?.let { name.value = it }
            handle.get<String>("prefill_surname")?.let { surname.value = it }
            handle.get<String>("prefill_email")?.let { email.value = it }
            handle.get<String>("prefill_password")?.let { password.value = it }
            handle.get<String>("prefill_repeat_password")?.let { repeatPassword.value = it }

            handle.get<String>("register_address")?.let { address.value = it }
            handle.get<String>("register_number")?.let { number.value = it }
            handle.get<String>("register_floor")?.let { floor.value = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it.replaceFirstChar { char -> char.uppercase() } },
            label = { Text("Nombre") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onNext = { surNameFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = surname.value,
            onValueChange = { surname.value = it.replaceFirstChar { char -> char.uppercase() } },
            label = { Text("Apellidos") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(surNameFocus),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            ),
            keyboardActions = KeyboardActions(
                onNext = { emailFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Correo electrónico") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "email")
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocus),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next, keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
                passwordError = it.length < 6
            },
            label = { Text("Contraseña") },
            isError = passwordError,
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        imageVector = if (passwordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocus),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false
            ),
            keyboardActions = KeyboardActions(
                onNext = { repeatPasswordFocus.requestFocus() }),
        )
        if (passwordError) {
            Text(
                "La contraseña debe tener al menos 6 caracteres",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = repeatPassword.value,
            onValueChange = {
                repeatPassword.value = it
                repeatPasswordError = it != password.value
            },
            label = { Text("Repetir contraseña") },
            isError = repeatPasswordError,
            visualTransformation = if (repeatPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = {
                    repeatPasswordVisible.value = !repeatPasswordVisible.value
                }) {
                    Icon(
                        imageVector = if (repeatPasswordVisible.value) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Mostrar/ocultar contraseña"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(repeatPasswordFocus),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                autoCorrectEnabled = false
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (address.value.isBlank()) {
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("prefill_name", name.value)
                            set("prefill_surname", surname.value)
                            set("prefill_email", email.value)
                            set("prefill_password", password.value)
                            set("prefill_repeat_password", repeatPassword.value)
                            set("prefill_address", address.value)
                            set("prefill_number", number.value)
                            set("prefill_floor", floor.value)
                        }
                        navController.navigate("add_address_from_register")
                    } else {
                        registerButtonFocus.requestFocus()
                    }
                })
        )

        if (repeatPasswordError) {
            Text("Las contraseñas no coinciden", color = Color.Red, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = if (address.value.isBlank()) "Seleccionar dirección" else "${address.value}, Nº ${number.value}${if (floor.value.isNotBlank()) ", ${floor.value}" else ""}",
                onValueChange = {},
                label = { Text("Dirección de envío") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        navController.currentBackStackEntry?.savedStateHandle?.apply {
                            set("prefill_name", name.value)
                            set("prefill_surname", surname.value)
                            set("prefill_email", email.value)
                            set("prefill_password", password.value)
                            set("prefill_repeat_password", repeatPassword.value)

                            set("prefill_address", address.value)
                            set("prefill_number", number.value)
                            set("prefill_floor", floor.value)
                        }
                        navController.navigate("add_address_from_register")

                    })
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val fullName = "${name.value.trim()} ${surname.value.trim()}"
                authViewModel.register(
                    email.value, password.value, fullName, address.value, number.value, floor.value
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(registerButtonFocus),
            enabled = isFormValid
        ) {
            Text("Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Ya tengo cuenta")
        }

        Spacer(modifier = Modifier.height(16.dp))

        message?.let {
            Text(it, color = Color.Red, fontSize = 14.sp)

            LaunchedEffect(it) {
                delay(5000)
                authViewModel.clearMessage()
            }
        }
    }

    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = {}, confirmButton = {
            TextButton(onClick = {
                showVerificationDialog = false
                navController.navigate("profile") {
                    popUpTo("register") { inclusive = true }
                }
            }) {
                Text("Entendido")
            }
        }, icon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }, title = {
            Text("Verifica tu email")
        }, text = {
            Text("Te hemos enviado un correo para verificar tu cuenta. Recomendamos revisar tu bandeja de entrada antes de continuar.")
        }, containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
