package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ShoppingListRepository
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

    val allItems: StateFlow<List<ShoppingListItemEntity>> =
        shoppingListRepository.getAllItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val itemsMap = mutableMapOf<Int, StateFlow<List<ShoppingListItemEntity>>>()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

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
            try {
                shoppingListRepository.createList(name)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al crear la lista"
            }
        }
    }

    suspend fun createListAndAddProduct(name: String, productId: String) {
        try {
            shoppingListRepository.createList(name)
            val newList = shoppingListRepository.getLocalLists().firstOrNull { it.name == name }
            newList?.let {
                shoppingListRepository.addItemToList(it.id, productId)
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Error al crear la lista"
        }
    }

    fun addProductToList(listId: Int, productId: String) {
        viewModelScope.launch {
            try {
                shoppingListRepository.addItemToList(listId, productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al añadir producto"
            }
        }
    }

    suspend fun isProductInList(listId: Int, productId: String): Boolean {
        return shoppingListRepository.isProductInList(listId, productId)
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            try {
                shoppingListRepository.deleteList(listId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar la lista"
            }
        }
    }

    fun removeItemFromList(listId: Int, productId: String) {
        viewModelScope.launch {
            try {
                shoppingListRepository.removeItemFromList(listId, productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al eliminar el producto"
            }
        }
    }

    fun increaseQuantityInList(listId: Int, productId: String) {
        viewModelScope.launch {
            try {
                shoppingListRepository.increaseQuantity(listId, productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar cantidad"
            }
        }
    }

    fun decreaseQuantityInList(listId: Int, productId: String) {
        viewModelScope.launch {
            try {
                shoppingListRepository.decreaseQuantity(listId, productId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al actualizar cantidad"
            }
        }
    }

    fun addToCart(productId: String, quantity: Int) {
        viewModelScope.launch {
            try {
                shoppingListRepository.addToCart(productId, quantity)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al añadir al carrito"
            }
        }
    }

    fun getShoppingListById(listId: Int): Flow<ShoppingListEntity?> {
        return shoppingListRepository.getShoppingListById(listId)
    }

    fun addAllToCart(listId: Int) {
        viewModelScope.launch {
            try {
                shoppingListRepository.addAllToCart(listId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error al añadir al carrito"
            }
        }
    }
}
