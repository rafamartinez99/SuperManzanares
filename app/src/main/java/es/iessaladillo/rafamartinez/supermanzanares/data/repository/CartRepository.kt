package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepository @Inject constructor(
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val firebaseService: FirebaseService,
    private val authRepository: AuthRepository
) {
    suspend fun getLocalCartItemsWithNames(): List<CartItemEntity> {
        return cartDao.getCartItems().first().map { item ->
            val name = productDao.getProductByIdSync(item.productId)?.name ?: "Desconocido"
            item.copy(productName = name)
        }
    }

    fun getCartItemsWithProducts(): Flow<List<Pair<CartItemEntity, ProductEntity?>>> =
        cartDao.getCartItems().map { cartItems ->
            cartItems.map { cartItem ->
                val product = productDao.getProductByIdSync(cartItem.productId)
                cartItem to product
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

        syncWithFirestoreIfLoggedIn()
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

        syncWithFirestoreIfLoggedIn()
    }

    suspend fun syncCartWithFirestore(userId: String, localCart: List<CartItemEntity>) {
        firebaseService.syncCartWithFirestore(userId, localCart, overwrite = true)
    }

    fun getCartItemById(productId: String): Flow<CartItemEntity?> = cartDao.getCartItemById(productId)


    suspend fun removeCartItemById(productId: String) {
        cartDao.removeCartItemByProductId(productId)
        syncWithFirestoreIfLoggedIn()
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

}
