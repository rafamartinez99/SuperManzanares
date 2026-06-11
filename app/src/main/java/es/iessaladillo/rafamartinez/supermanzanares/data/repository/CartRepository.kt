package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val CART_SYNC_DELAY_MS = 600L

class CartRepository @Inject constructor(
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val firebaseService: FirebaseService,
    private val authRepository: AuthRepository
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var syncJob: Job? = null

    suspend fun getLocalCartItemsWithNames(): List<CartItemEntity> {
        return cartDao.getCartItems().first().map { item ->
            val name = productDao.getProductByIdSync(item.productId)?.name ?: "Desconocido"
            item.copy(productName = name)
        }
    }

    fun getCartItemsWithProducts(): Flow<List<Pair<CartItemEntity, ProductEntity?>>> = 
        cartDao.getCartItemsWithProducts().map { cartItems ->
            cartItems.map { cartItemWithProduct ->
                cartItemWithProduct.cartItem to cartItemWithProduct.product
            }
        }

    suspend fun insertOrUpdateCartItem(productId: String, quantity: Int) {
        val currentQuantity = cartDao.getCartItemById(productId).firstOrNull()?.quantity ?: 0
        if (currentQuantity == 0) {
            val productName = productDao.getProductByIdSync(productId)?.name ?: "Desconocido"
            cartDao.insertCartItem(CartItemEntity(productId = productId, quantity = quantity, productName = productName))
        } else {
            cartDao.updateCartItemQuantity(productId, currentQuantity + quantity)
        }

        scheduleSyncWithFirestoreIfLoggedIn()
    }

    suspend fun decreaseCartItemQuantity(productId: String) {
        val existingItem = cartDao.getCartItemById(productId).firstOrNull()
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                cartDao.updateCartItemQuantity(productId, existingItem.quantity - 1)
            } else {
                cartDao.removeCartItemByProductId(productId)
            }
        }

        scheduleSyncWithFirestoreIfLoggedIn()
    }

    suspend fun syncCartWithFirestore(userId: String, localCart: List<CartItemEntity>) {
        firebaseService.syncCartWithFirestore(userId, localCart, overwrite = true)
    }

    fun getCartItemById(productId: String): Flow<CartItemEntity?> = cartDao.getCartItemById(productId)


    suspend fun removeCartItemById(productId: String) {
        cartDao.removeCartItemByProductId(productId)
        scheduleSyncWithFirestoreIfLoggedIn()
    }

    suspend fun removeCartItemEntity(cartItem: CartItemEntity) {
        cartDao.removeCartItem(cartItem)
        authRepository.getCurrentUser()?.let {
            firebaseService.deleteCartItem(it.uid, cartItem.productId)
        }
    }

    suspend fun clearCart() {
        cartDao.clearCart()

        authRepository.getCurrentUser()?.uid?.let { userId ->
            firebaseService.clearCartInFirestore(userId)
        }
    }

    private suspend fun syncWithFirestoreIfLoggedIn() {
        authRepository.getCurrentUser()?.uid?.let { userId ->
            val localCart = cartDao.getCartItems().first().map { item ->
                val name = productDao.getProductByIdSync(item.productId)?.name ?: "Desconocido"
                CartItemEntity(
                    productId = item.productId,
                    quantity = item.quantity,
                    productName = name
                )
            }
            firebaseService.syncCartWithFirestore(userId, localCart, overwrite = true)
        }
    }

    private fun scheduleSyncWithFirestoreIfLoggedIn() {
        syncJob?.cancel()
        syncJob = syncScope.launch {
            delay(CART_SYNC_DELAY_MS)
            syncWithFirestoreIfLoggedIn()
        }
    }

}
