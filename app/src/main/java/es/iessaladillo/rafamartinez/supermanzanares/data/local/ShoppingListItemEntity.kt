package es.iessaladillo.rafamartinez.supermanzanares.data.local

import androidx.room.Entity

@Entity(
    tableName = "shopping_lists_items",
    primaryKeys = ["listId", "productId"]
)
data class ShoppingListItemEntity(
    val listId: Int,
    val productId: String,
    val quantity: Int = 1
)


