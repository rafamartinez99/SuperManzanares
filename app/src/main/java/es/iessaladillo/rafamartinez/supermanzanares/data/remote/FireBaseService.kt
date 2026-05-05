package es.iessaladillo.rafamartinez.supermanzanares.data.remote

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.model.User
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Order
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val db = FirebaseFirestore.getInstance()
    private val productCollection = db.collection("products")

    suspend fun getProductsOnce(): List<Product> {
        val snapshot = productCollection.get().await()
        return snapshot.toObjects(Product::class.java)
    }

    fun getProducts(): Flow<List<Product>> = callbackFlow {
        val listener = productCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val products = snapshot.toObjects(Product::class.java)
                trySend(products).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProduct(product: Product) {
        productCollection.document(product.id).set(product).await()
    }

    fun getOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val orderCollection = db.collection("users").document(userId).collection("orders")
        val listener = orderCollection.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val orders = snapshot.toObjects(Order::class.java)
                trySend(orders).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun addOrder(userId: String, order: Order) {
        val userOrdersRef = db.collection("users").document(userId).collection("orders")
        userOrdersRef.document(order.id).set(order).await()
    }

    suspend fun syncCartWithFirestore(
        userId: String, localCart: List<CartItemEntity>, overwrite: Boolean = true
    ) {
        val cartRef = db.collection("users").document(userId).collection("cart")

        val firestoreProducts = cartRef.get().await().documents.mapNotNull { it.id }.toSet()
        val localProductIds = localCart.map { it.productId }.toSet()

        val itemsToDelete = firestoreProducts - localProductIds
        for (productId in itemsToDelete) {
            cartRef.document(productId).delete().await()
        }

        for (item in localCart) {
            val itemRef = cartRef.document(item.productId)
            val existingItem = itemRef.get().await()

            val newQuantity = if (overwrite || !existingItem.exists()) {
                item.quantity
            } else {
                val currentQuantity = existingItem.getLong("quantity") ?: 0L
                currentQuantity + item.quantity
            }

            itemRef.set(
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to newQuantity
                )
            ).await()
        }
    }

    suspend fun deleteCartItem(userId: String, productId: String) {
        db.collection("users").document(userId).collection("cart").document(productId).delete()
            .await()
    }

    suspend fun setShoppingListInFirestore(
        userId: String,
        list: ShoppingListEntity,
        items: List<Map<String, Any>>
    ) {
        val listRef = db.collection("users").document(userId).collection("shopping_lists")
            .document(list.id.toString())

        listRef.set(
            mapOf(
                "name" to list.name,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val itemsRef = listRef.collection("items")
        for (item in items) {
            itemsRef.document(item["productId"] as String).set(item).await()
        }
    }

    suspend fun saveUser(user: User) {
        db.collection("users").document(user.id).set(user).await()
    }

    suspend fun updateProductQuantityInFirestore(
        userId: String, listId: Int, productId: String, quantity: Int
    ) {
        val itemRef = db.collection("users").document(userId)
            .collection("shopping_lists").document(listId.toString())
            .collection("items").document(productId)

        if (quantity > 0) {
            itemRef.set(
                mapOf(
                    "productId" to productId,
                    "quantity" to quantity
                ),
                SetOptions.merge()
            ).await()
        } else {
            itemRef.delete().await()
        }
    }

    suspend fun deleteProductListFromFirestore(userId: String, listId: Int, productId: String) {
        db.collection("users").document(userId).collection("shopping_lists")
            .document(listId.toString()).collection("items").document(productId).delete().await()
    }

    suspend fun deleteListFromFirestore(userId: String, listId: Int) {
        db.collection("users").document(userId).collection("shopping_lists")
            .document(listId.toString()).delete().await()
    }

    suspend fun clearCartInFirestore(userId: String) {
        val cartRef = db.collection("users").document(userId).collection("cart")
        val batch = db.batch()

        cartRef.get().await().documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        batch.commit().await()
    }

    suspend fun getUserFromFirestore(userId: String): User? {
        val snapshot = db.collection("users").document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }
}