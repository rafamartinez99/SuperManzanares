package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val ofertas: StateFlow<List<Product>> = repository.getProductsWithDiscount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val novedades: StateFlow<List<Product>> = repository.getLatestProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initProductsIfNeeded()
        }

        viewModelScope.launch {
            repository.observeFirestoreAndSyncToRoom()
        }
    }

    fun getProductById(productId: String): StateFlow<Product?> {
        return repository.getProductById(productId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }

    fun getRelatedProducts(productId: String): StateFlow<List<Product>> {
        return repository.getRelatedProducts(productId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }
}