package es.iessaladillo.rafamartinez.supermanzanares.data.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val weight: Double = 0.0,
    val originalPrice: Double? = null,
    val discountPrice: Double? = null,
    val description: String? = null,
    val categoryId: Int = 0,
    val ingredients: List<String> = emptyList(),
    val nutrition: NutritionInfo = NutritionInfo()
)

data class NutritionInfo(
    val energyKcal: Int = 0,
    val fats: Double = 0.0,
    val saturatedFats: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val sugars: Double = 0.0,
    val fiber: Double = 0.0,
    val protein: Double = 0.0,
    val salt: Double = 0.0
)


