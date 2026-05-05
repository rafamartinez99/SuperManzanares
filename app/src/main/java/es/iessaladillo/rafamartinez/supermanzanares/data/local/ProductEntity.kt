package es.iessaladillo.rafamartinez.supermanzanares.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import es.iessaladillo.rafamartinez.supermanzanares.data.model.NutritionInfo
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val weight: Double,
    val originalPrice: Double? = null,
    val discountPrice: Double? = null,
    val description: String? = null,
    val categoryId: Int,
    val ingredients: List<String> = emptyList(),
    @Embedded val nutrition: NutritionInfo = NutritionInfo()
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    price = price,
    imageUrl = imageUrl,
    weight = weight,
    originalPrice = originalPrice,
    discountPrice = discountPrice,
    description = description,
    categoryId = categoryId,
    ingredients = ingredients,
    nutrition = nutrition
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    price = price,
    imageUrl = imageUrl,
    weight = weight,
    originalPrice = originalPrice,
    discountPrice = discountPrice,
    description = description,
    categoryId = categoryId,
    ingredients = ingredients,
    nutrition = nutrition
)

