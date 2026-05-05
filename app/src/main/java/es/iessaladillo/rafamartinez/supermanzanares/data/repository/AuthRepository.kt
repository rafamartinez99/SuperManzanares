package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import com.google.firebase.auth.FirebaseAuth
import es.iessaladillo.rafamartinez.supermanzanares.data.model.User
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseService: FirebaseService,
) {

    fun getCurrentUser() = auth.currentUser

    suspend fun register(
        email: String,
        password: String,
        name: String,
        address: String,
        number: String,
        floor: String
    ): Result<String> {
        if (email.isBlank() || password.isBlank() || name.isBlank() || address.isBlank() || number.isBlank()) {
            return Result.failure(Exception("Por favor, completa todos los campos obligatorios"))
        }

        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
            if (user != null) {
                saveUserToFirestore(
                    User(
                        id = user.uid,
                        name = name,
                        email = user.email ?: "",
                        address = address,
                        number = number,
                        floor = floor
                    )
                )
            }
            Result.success("Usuario registrado con éxito")
        } catch (e: Exception) {
            Result.failure(Exception(interpretAuthError(e)))
        }
    }

    suspend fun login(email: String, password: String): Result<String> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Por favor, completa todos los campos"))
        }

        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success("Inicio de sesión exitoso")
        } catch (e: Exception) {
            Result.failure(Exception(interpretAuthError(e)))
        }
    }

    fun interpretAuthError(e: Exception): String = when {
        e.message?.contains("There is no user record") == true -> "No existe ningún usuario con ese correo"
        e.message?.contains("The password is invalid") == true -> "Contraseña incorrecta"
        e.message?.contains("The email address is badly formatted") == true -> "El correo no es válido"
        else -> "Comprueba que has ingresado bien todos los datos"
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun saveUserToFirestore(user: User) {
        firebaseService.saveUser(user)
    }

    suspend fun handleGoogleLogin(user: User, isNew: Boolean) {
        if (isNew) {
            saveUserToFirestore(user)
        }
    }

    fun getGoogleUser(): User? {
        val user = auth.currentUser ?: return null
        return User(
            id = user.uid,
            name = user.displayName ?: "Sin nombre",
            email = user.email ?: "",
            address = "",
            number = "",
            floor = ""
        )
    }
}
