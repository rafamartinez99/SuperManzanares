package es.iessaladillo.rafamartinez.supermanzanares.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_lists")
    fun getAllLists(): Flow<List<ShoppingListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(shoppingList:ShoppingListEntity): Long

    @Query("SELECT * FROM shopping_lists_items WHERE listId = :listId")
    fun getItemsFromList(listId: Int): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItemToList(shoppingListItem: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_lists_items WHERE listId = :listId AND productId = :productId")
    suspend fun removeItemFromList(listId: Int, productId: String)

    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun deleteListById(listId: Int)

    @Query("SELECT * FROM shopping_lists_items WHERE listId = :listId AND productId = :productId LIMIT 1")
    suspend fun getItemFromList(listId: Int, productId: String) : ShoppingListItemEntity?

    @Query("UPDATE shopping_lists_items SET quantity = quantity + 1 WHERE listId = :listId AND productId = :productId")
    suspend fun increaseQuantity(listId: Int, productId: String)

    @Query("UPDATE shopping_lists_items SET quantity = quantity - 1 WHERE listId = :listId AND productId = :productId AND quantity > 1")
    suspend fun decreaseQuantity(listId: Int, productId: String)

    @Query("SELECT * FROM shopping_lists_items WHERE listId = :listId")
    suspend fun getItemsFromListSync(listId: Int): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_lists WHERE id = :listId LIMIT 1")
    fun getShoppingListById(listId: Int): Flow<ShoppingListEntity?>
}
