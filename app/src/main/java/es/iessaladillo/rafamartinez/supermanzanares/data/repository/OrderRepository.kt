package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.model.Order
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class OrderRepository @Inject constructor(private val firebaseService: FirebaseService) {

    fun getOrders(userId: String): Flow<List<Order>> = flow {
        firebaseService.getOrders(userId).collect { orders ->
            emit(orders)
        }
    }

    suspend fun placeOrder(userId: String, order: Order) {
        firebaseService.addOrder(userId, order)
    }


}