package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import com.google.firebase.auth.FirebaseAuth
import es.iessaladillo.rafamartinez.supermanzanares.data.model.User
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import javax.inject.Inject

class UserRepository @Inject constructor(private val firebaseService: FirebaseService) {

    suspend fun getUser(): User? {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return firebaseService.getUserFromFirestore(userId)
    }

    suspend fun saveUser(user: User) {
        firebaseService.saveUser(user)
    }

    suspend fun loadCurrentUser(): Result<User?> {
        val authUser = FirebaseAuth.getInstance().currentUser ?: return Result.success(null)

        return try {
            val firestoreUser = getUser() ?: return Result.success(null)
            val authEmail = authUser.email ?: return Result.success(null)

            val finalUser = if (authEmail != firestoreUser.email) {
                val updatedUser = firestoreUser.copy(email = authEmail)
                saveUser(updatedUser)
                updatedUser
            } else {
                firestoreUser
            }

            Result.success(finalUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(name: String, address: String, number: String, floor: String): User? {
        val current = getUser() ?: return null
        val updated = current.copy(name = name, address = address, number = number, floor = floor)
        firebaseService.saveUser(updated)
        return updated
    }
}