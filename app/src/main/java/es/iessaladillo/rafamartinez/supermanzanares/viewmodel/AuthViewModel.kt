package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.R
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.AuthRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.CartRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ShoppingListRepository
import es.iessaladillo.rafamartinez.supermanzanares.utils.GoogleIdentityHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val shoppingListRepository: ShoppingListRepository
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow(authRepository.getCurrentUser() != null)
    val authState: StateFlow<Boolean> get() = _authState

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> get() = _message

    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            _authState.value = auth.currentUser != null
        }
    }

    fun register(
        email: String,
        password: String,
        name: String,
        address: String,
        number: String,
        floor: String
    ) {
        viewModelScope.launch {
            val result = authRepository.register(email, password, name, address, number, floor)
            val userId = authRepository.getCurrentUser()?.uid
            if (result.isSuccess && userId != null) {
                syncLocalData(userId)
            }
            _authState.value = authRepository.getCurrentUser() != null
            _message.value = result.getOrNull() ?: result.exceptionOrNull()?.message
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            val userId = authRepository.getCurrentUser()?.uid
            if (result.isSuccess && userId != null) {
                syncLocalData(userId)
            }
            _authState.value = authRepository.getCurrentUser() != null
            _message.value = result.getOrNull() ?: result.exceptionOrNull()?.message
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun signInWithGoogle(
        activity: Activity,
        onResult: (success: Boolean, isNewUser: Boolean) -> Unit
    ) {
        val webClientId = getApplication<Application>()
            .getString(R.string.default_web_client_id)

        GoogleIdentityHelper.launchSignIn(
            activity = activity,
            webClientId = webClientId
        ) { success, isNewUser, errorMsg ->
            if (!success) {
                _message.value = errorMsg
            } else {
                _authState.value = true
                authRepository.getGoogleUser()?.let { user ->
                    viewModelScope.launch {
                        authRepository.handleGoogleLogin(user, isNew = isNewUser)
                        syncLocalData(user.id)
                    }
                }
            }
            onResult(success, isNewUser)
        }
    }

    private suspend fun syncLocalData(userId: String) {
        val localCart = cartRepository.getLocalCartItemsWithNames()
        cartRepository.syncCartWithFirestore(userId, localCart)
        shoppingListRepository.syncListsWithFirestore(userId)
    }
}
