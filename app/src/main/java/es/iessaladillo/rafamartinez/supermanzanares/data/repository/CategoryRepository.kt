package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import es.iessaladillo.rafamartinez.supermanzanares.data.local.CategoryDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CategoryEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    private val defaultCategories = listOf(
        CategoryEntity(id = 1, name = "Lácteos y Huevos"),
        CategoryEntity(id = 2, name = "Panadería y Bollería"),
        CategoryEntity(id = 3, name = "Carnes y Embutidos"),
        CategoryEntity(id = 4, name = "Pescados y Mariscos"),
        CategoryEntity(id = 5, name = "Frutas y Verduras"),
        CategoryEntity(id = 6, name = "Conservas y Despensa"),
        CategoryEntity(id = 7, name = "Pastas y Cereales"),
        CategoryEntity(id = 8, name = "Aceites, Salsas y Condimentos"),
        CategoryEntity(id = 9, name = "Postres y Repostería"),
        CategoryEntity(id = 10, name = "Bebidas")
    )

    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun initializeDefaultCategoriesIfNeeded() {
        val currentCategories = categoryDao.getAllCategories().first()

        val missing = defaultCategories.filter { default ->
            currentCategories.none { it.id == default.id && it.name == default.name }
        }

        if (missing.isNotEmpty() || currentCategories.size != defaultCategories.size) {
            categoryDao.insertCategories(defaultCategories)
        }
    }

}
