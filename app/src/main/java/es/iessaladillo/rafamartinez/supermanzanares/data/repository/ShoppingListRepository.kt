package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.toDomain
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val firebaseService: FirebaseService,
    private val authRepository: AuthRepository
) {
    fun getAllLists(): Flow<List<ShoppingListEntity>> = shoppingListDao.getAllLists()

    suspend fun getLocalLists(): List<ShoppingListEntity> = shoppingListDao.getAllLists().first()

    suspend fun createList(name: String) {
        shoppingListDao.insertList(ShoppingListEntity(name = name))

        authRepository.getCurrentUser()?.uid?.let { userId ->
            getLocalLists().forEach { list ->
                val items = shoppingListDao.getItemsFromListSync(list.id).map { item ->
                    val product = productDao.getProductByIdSync(item.productId)?.toDomain()
                    mapOf(
                        "productId" to item.productId,
                        "productName" to (product?.name ?: "Desconocido"),
                        "quantity" to item.quantity
                    )
                }
                firebaseService.setShoppingListInFirestore(userId, list, items)
            }
        }
    }

    suspend fun addItemToList(listId: Int, productId: String) {
        shoppingListDao.addItemToList(
            ShoppingListItemEntity(
                listId = listId, productId = productId
            )
        )

        authRepository.getCurrentUser()?.uid?.let { userId ->
            getLocalLists().forEach { list ->
                val items = shoppingListDao.getItemsFromListSync(list.id).map { item ->
                    val product = productDao.getProductByIdSync(item.productId)?.toDomain()
                    mapOf(
                        "productId" to item.productId,
                        "productName" to (product?.name ?: "Desconocido"),
                        "quantity" to item.quantity
                    )
                }
                firebaseService.setShoppingListInFirestore(userId, list, items)
            }
        }
    }

    suspend fun removeItemFromList(listId: Int, productId: String) {
        shoppingListDao.removeItemFromList(listId, productId)

        authRepository.getCurrentUser()?.uid?.let { userId ->
            firebaseService.deleteProductListFromFirestore(userId, listId, productId)
        }
    }

    suspend fun deleteList(listId: Int) {
        shoppingListDao.deleteListById(listId)

        authRepository.getCurrentUser()?.uid?.let { userId ->
            firebaseService.deleteListFromFirestore(userId, listId)
        }
    }

    suspend fun isProductInList(listId: Int, productId: String): Boolean =
        shoppingListDao.getItemFromList(listId, productId) != null

    fun getItemsFromList(listId: Int): Flow<List<ShoppingListItemEntity>> =
        shoppingListDao.getItemsFromList(listId)

    fun getAllItems(): Flow<List<ShoppingListItemEntity>> =
        shoppingListDao.getAllItems()

    fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }

    suspend fun increaseQuantity(listId: Int, productId: String) {
        shoppingListDao.increaseQuantity(listId, productId)

        authRepository.getCurrentUser()?.uid?.let { userId ->
            val quantity = shoppingListDao.getItemFromList(listId, productId)?.quantity ?: 0
            firebaseService.updateProductQuantityInFirestore(userId, listId, productId, quantity)
        }
    }

    suspend fun decreaseQuantity(listId: Int, productId: String) {
        shoppingListDao.decreaseQuantity(listId, productId)

        authRepository.getCurrentUser()?.uid?.let { userId ->
            val quantity = shoppingListDao.getItemFromList(listId, productId)?.quantity ?: 0
            firebaseService.updateProductQuantityInFirestore(userId, listId, productId, quantity)
        }
    }

    suspend fun syncListsWithFirestore(userId: String) {
        getLocalLists().forEach { list ->
            val items = shoppingListDao.getItemsFromListSync(list.id).map { item ->
                val product = productDao.getProductByIdSync(item.productId)?.toDomain()
                mapOf(
                    "productId" to item.productId,
                    "productName" to (product?.name ?: "Desconocido"),
                    "quantity" to item.quantity
                )
            }
            firebaseService.setShoppingListInFirestore(userId, list, items)
        }
    }

    suspend fun addToCart(productId: String, quantity: Int) {
        val existing = cartDao.getCartItemById(productId).firstOrNull()?.quantity ?: 0
        cartDao.insertCartItem(CartItemEntity(productId, existing + quantity))

        authRepository.getCurrentUser()?.uid?.let { userId ->
            val localCart = cartDao.getCartItems().first()
            firebaseService.syncCartWithFirestore(userId, localCart, overwrite = true)
        }
    }

    fun getShoppingListById(listId: Int): Flow<ShoppingListEntity?> {
        return shoppingListDao.getShoppingListById(listId)
    }

    suspend fun addAllToCart(listId: Int) {
        val items = shoppingListDao.getItemsFromListSync(listId)
        val currentCart = cartDao.getCartItems().first().associateBy { it.productId }

        items.forEach { item ->
            val existingQty = currentCart[item.productId]?.quantity ?: 0
            cartDao.insertCartItem(
                CartItemEntity(
                    productId = item.productId, quantity = existingQty + item.quantity
                )
            )
        }

        authRepository.getCurrentUser()?.uid?.let { userId ->
            val localCart = cartDao.getCartItems().first()
            firebaseService.syncCartWithFirestore(userId, localCart, overwrite = true)
        }
    }
}
