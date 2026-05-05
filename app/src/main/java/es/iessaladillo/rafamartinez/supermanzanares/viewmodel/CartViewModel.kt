package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            repository.insertOrUpdateCartItem(productId, quantity)
        }
    }

    fun removeFromCart(cartItem: CartItemEntity) {
        viewModelScope.launch {
            repository.removeCartItemEntity(cartItem)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun getCartItemById(productId: String): Flow<CartItemEntity?> {
        return repository.getCartItemById(productId)
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeCartItemById(productId)
        }
    }

    fun decreaseQuantity(productId: String) {
        viewModelScope.launch {
            repository.decreaseCartItemQuantity(productId)
        }
    }

    fun getTotalDifferentProducts() : Flow<Int> {
        return cart.map { cartItems->
            cartItems.distinctBy { (cartItem, product) ->
                product?.id
            }.size
        }
    }
}
