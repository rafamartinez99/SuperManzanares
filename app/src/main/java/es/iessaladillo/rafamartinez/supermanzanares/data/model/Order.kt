package es.iessaladillo.rafamartinez.supermanzanares.data.model

data class Order(
    val id: String = "",
    val date: String = "",
    val status: String = "",
    val products: List<OrderItem> = emptyList(),
    val total: Double = 0.0

)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0
)
