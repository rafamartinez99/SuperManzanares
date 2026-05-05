package es.iessaladillo.rafamartinez.supermanzanares.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import es.iessaladillo.rafamartinez.supermanzanares.ui.components.PasswordConfirmationDialog
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmailScreen(
    navController: NavController, userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    var email by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNotVerifiedDialog by remember { mutableStateOf(false) }
    var showGoogleUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            email = it.email
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Cambiar email", fontWeight = FontWeight.Bold)
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }, actions = {
            TextButton(onClick = {
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (email != user?.email) {
                    currentUser?.reload()?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (currentUser.isEmailVerified) {
                                val hasPassword = currentUser.providerData.any {
                                    it.providerId == EmailAuthProvider.PROVIDER_ID
                                }

                                if (hasPassword) {
                                    showPasswordDialog = true
                                } else {
                                    showGoogleUserDialog = true
                                }
                            } else {
                                showNotVerifiedDialog = true
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Error al comprobar el estado de verificación")
                            }
                        }
                    }
                } else {
                    navController.popBackStack()
                }
            }) {
                Text("Guardar", color = MaterialTheme.colorScheme.primary)
            }
        })
    }, snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text("Email", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu email es también el usuario de acceso a la cuenta.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showPasswordDialog) {
            PasswordConfirmationDialog(
                onDismiss = { showPasswordDialog = false },
                onConfirm = { password ->
                    showPasswordDialog = false
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val credential = EmailAuthProvider.getCredential(currentUser?.email ?: "", password)

                    currentUser?.reauthenticate(credential)?.addOnSuccessListener {
                        // Intentamos crear un usuario temporal con el nuevo email
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "123456fakePassword!")
                            .addOnSuccessListener { tempUser ->
                                // Si se ha podido crear, el email NO estaba en uso → lo eliminamos
                                tempUser.user?.delete()?.addOnCompleteListener {
                                    currentUser.verifyBeforeUpdateEmail(email)
                                        .addOnSuccessListener {
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Hemos enviado un correo a $email. Verifícalo para completar el cambio."
                                                )
                                                navController.popBackStack()
                                            }
                                        }
                                        .addOnFailureListener { updateEx ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Error: ${updateEx.message}")
                                            }
                                        }
                                }
                            }
                            .addOnFailureListener { ex ->
                                if (ex.message?.contains("email address is already in use") == true ||
                                    ex.message?.contains("EMAIL_ALREADY_IN_USE") == true) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Ese correo ya está registrado ❌")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error al comprobar el correo: ${ex.message}")
                                    }
                                }
                            }
                    }?.addOnFailureListener { reauthEx ->
                        reauthEx.printStackTrace()
                        scope.launch {
                            snackbarHostState.showSnackbar("Contraseña incorrecta o sesión caducada ❌")
                        }
                    }
                })
        }

        if (showNotVerifiedDialog) {
            AlertDialog(
                onDismissRequest = { showNotVerifiedDialog = false }, confirmButton = {
                TextButton(onClick = { showNotVerifiedDialog = false }) {
                    Text("Entendido")
                }
            }, title = { Text("Correo no verificado") }, text = {
                Text("Para cambiar tu correo electrónico primero debes verificar el actual. Revisa tu bandeja de entrada y sigue el enlace de verificación.")
            }, containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showGoogleUserDialog) {
            AlertDialog(
                onDismissRequest = { showGoogleUserDialog = false },
                confirmButton = {
                    TextButton(onClick = { showGoogleUserDialog = false }) {
                        Text("Entendido")
                    }
                },
                title = { Text("Cuenta de Google detectada") },
                text = {
                    Text("No puedes cambiar el correo de una cuenta iniciada con Google. Si necesitas usar otro correo, crea una cuenta nueva con email y contraseña.")
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
