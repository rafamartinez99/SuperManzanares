package es.iessaladillo.rafamartinez.supermanzanares.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartItemEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Order
import es.iessaladillo.rafamartinez.supermanzanares.data.model.OrderItem
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.OrderRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@HiltViewModel
class OrderViewModel @Inject constructor(private val repository: OrderRepository) : ViewModel() {

    fun getOrders(userId: String): StateFlow<List<Order>> {
        return repository.getOrders(userId).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun placeOrder(userId: String, order: Order) {
        viewModelScope.launch {
            repository.placeOrder(userId, order)
        }
    }

    fun createOrder(cart: List<Pair<CartItemEntity, ProductEntity?>>): Order {
        val total = cart.sumOf { (item, product) -> (product?.price ?: 0.0) * item.quantity }
        return Order(
            id = UUID.randomUUID().toString(),
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            status = "Finalizado",
            products = cart.map { (item, product) ->
                OrderItem(item.productId, product?.name ?: "", item.quantity)
            },
            total = total
        )
    }

}