package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(private val repository: CartRepository) : ViewModel() {

    val cart: StateFlow<List<Pair<CartItemEntity, ProductEntity?>>> =
        repository.getCartItemsWithProducts()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val cartItemCount: StateFlow<Int> = cart.map { cartItems ->
        cartItems.sumOf { it.first.quantity }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val isCartEmpty: StateFlow<Boolean> = cartItemCount.map { it == 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val totalPrice: StateFlow<Double> = cart.map { cartItems ->
        cartItems.sumOf { (cartItem, product) ->
            (product?.price ?: 0.0) * cartItem.quantity
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdateCartItem(productId, quantity)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al añadir al carrito"
            }
        }
    }

    fun removeFromCart(cartItem: CartItemEntity) {
        viewModelScope.launch {
            try {
                repository.removeCartItemEntity(cartItem)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar del carrito"
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                repository.clearCart()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al vaciar el carrito"
            }
        }
    }

    fun getCartItemById(productId: String): Flow<CartItemEntity?> {
        return repository.getCartItemById(productId)
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            try {
                repository.removeCartItemById(productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar del carrito"
            }
        }
    }

    fun decreaseQuantity(productId: String) {
        viewModelScope.launch {
            try {
                repository.decreaseCartItemQuantity(productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar cantidad"
            }
        }
    }

    fun getTotalDifferentProducts(): Flow<Int> {
        return cart.map { cartItems ->
            cartItems.distinctBy { (_, product) -> product?.id }.size
        }
    }
}
