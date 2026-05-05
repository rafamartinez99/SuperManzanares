package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ShoppingListRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(private val shoppingListRepository: ShoppingListRepository) :
    ViewModel() {

    private val _shoppingLists = MutableStateFlow<List<ShoppingListEntity>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingListEntity>> = _shoppingLists

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts: StateFlow<List<Product>> = _allProducts
    val itemsMap = mutableMapOf<Int, StateFlow<List<ShoppingListItemEntity>>>()

    init {
        fetchShoppingLists()
        fetchAllProducts()
    }

    private fun fetchShoppingLists() {
        viewModelScope.launch {
            shoppingListRepository.getAllLists().collectLatest { _shoppingLists.value = it }
        }
    }

    private fun fetchAllProducts() {
        viewModelScope.launch {
            shoppingListRepository.getAllProducts().collectLatest { _allProducts.value = it }

        }
    }

    fun observeItemsForList(listId: Int): StateFlow<List<ShoppingListItemEntity>> {
        return itemsMap.getOrPut(listId) {
            shoppingListRepository.getItemsFromList(listId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    fun createShoppingList(name: String) {
        viewModelScope.launch {
            shoppingListRepository.createList(name)
        }
    }

    suspend fun createListAndAddProduct(name: String, productId: String) {
            shoppingListRepository.createList(name)
            val newList = shoppingListRepository.getLocalLists().firstOrNull { it.name == name }
            newList?.let {
                shoppingListRepository.addItemToList(it.id, productId)
            }
    }

    fun addProductToList(listId: Int, productId: String) {
        viewModelScope.launch {
                shoppingListRepository.addItemToList(listId, productId)
        }
    }

    suspend fun isProductInList(listId: Int, productId: String): Boolean {
        return shoppingListRepository.isProductInList(listId, productId)
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            shoppingListRepository.deleteList(listId)
        }
    }

    fun removeItemFromList(listId: Int, productId: String) {
        viewModelScope.launch {
            shoppingListRepository.removeItemFromList(listId, productId)
        }
    }

    fun increaseQuantityInList(listId: Int, productId: String) {
        viewModelScope.launch {
            shoppingListRepository.increaseQuantity(listId, productId)
        }
    }

    fun decreaseQuantityInList(listId: Int, productId: String) {
        viewModelScope.launch {
            shoppingListRepository.decreaseQuantity(listId, productId)
        }
    }

    fun addToCart(productId: String, quantity: Int) {
        viewModelScope.launch {
            shoppingListRepository.addToCart(productId, quantity)
        }
    }

    fun getShoppingListById(listId: Int): Flow<ShoppingListEntity?> {
        return shoppingListRepository.getShoppingListById(listId)
    }

    fun addAllToCart(listId: Int) {
        viewModelScope.launch {
            shoppingListRepository.addAllToCart(listId)
        }
    }
}
