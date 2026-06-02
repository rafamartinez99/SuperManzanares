package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.model.User
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> get() = _user
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> get() = _userMessage

    fun setUserMessage(message: String) {
        _userMessage.value = message
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun refreshUser() {
        viewModelScope.launch {
            val result = repository.loadCurrentUser()
            result.onSuccess { loadedUser ->
                _user.value = loadedUser
                if (loadedUser != null && FirebaseAuth.getInstance().currentUser?.email != loadedUser.email) {
                    setUserMessage("Tu correo ha sido modificado correctamente.")
                }
            }.onFailure { e ->
                _user.value = null
                _userMessage.value = e.message ?: "Error al cargar el perfil"
            }
        }
    }

    fun clearUser() {
        _user.value = null
    }

    fun updateUserProfile(name: String, address: String, number: String, floor: String) {
        viewModelScope.launch {
            val updated = repository.updateUserProfile(name, address, number, floor)
            if (updated != null) _user.value = updated
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
            _user.value = user
        }
    }

}