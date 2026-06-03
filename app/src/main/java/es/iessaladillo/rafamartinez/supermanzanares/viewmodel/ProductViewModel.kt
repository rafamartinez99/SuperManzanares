package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ofertas: StateFlow<List<Product>> = repository.getProductsWithDiscount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val novedades: StateFlow<List<Product>> = repository.getLatestProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        viewModelScope.launch {
            _isLoading.value = true
            repository.initProductsIfNeeded()
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.forceRefresh()
            } catch (_: Exception) {
            } finally {
                _isRefreshing.value = false
            }
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
