package es.iessaladillo.rafamartinez.supermanzanares.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.toDomain
import es.iessaladillo.rafamartinez.supermanzanares.data.local.toEntity
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import es.iessaladillo.rafamartinez.supermanzanares.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val firebaseService: FirebaseService,
    @ApplicationContext private val context: Context
) {
    private val defaultProducts: List<Product> by lazy {
        val json = context.assets.open("default_products.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Product>>() {}.type
        Gson().fromJson(json, type)
    }

    private val allProducts: Flow<List<Product>> =
        productDao.getAllProducts().map { list -> list.map { it.toDomain() } }


    suspend fun initProductsIfNeeded() {
        val localProducts =
            productDao.getAllProducts().firstOrNull().orEmpty().map { it.toDomain() }

        val remoteProducts = firebaseService.getProductsOnce()
        val remoteIds = remoteProducts.map { it.id }

        if (localProducts.isEmpty()) {
            when {
                remoteProducts.isNotEmpty() -> {
                    productDao.insertProducts(remoteProducts.map { it.toEntity() })
                }

                else -> {
                    productDao.insertProducts(defaultProducts.map { it.toEntity() })
                    defaultProducts.forEach { firebaseService.addProduct(it) }
                }
            }
            return
        }

        val newFromCode = defaultProducts.filter { it.id !in remoteIds }
        if (newFromCode.isNotEmpty()) {
            newFromCode.forEach { firebaseService.addProduct(it) }
            productDao.insertProducts(newFromCode.map { it.toEntity() })
        }

        val hasRemoteChanges = remoteProducts.any { remote ->
            val local = localProducts.find { it.id == remote.id }
            local == null || local != remote
        }
        if (hasRemoteChanges) {
            productDao.insertProducts(remoteProducts.map { it.toEntity() })
        }
    }

    suspend fun observeFirestoreAndSyncToRoom() {
        firebaseService.getProducts().distinctUntilChanged().collect { remoteProducts ->
            val updatedEntities = remoteProducts.map { it.toEntity() }
            productDao.insertProducts(updatedEntities)
        }
    }

    fun getProducts(): Flow<List<Product>> = allProducts

    fun getProductById(productId: String): Flow<Product?> =
        productDao.getProductById(productId).map { it?.toDomain() }

    fun getProductsWithDiscount(): Flow<List<Product>> =
        allProducts.map { list -> list.filter { it.discountPrice != null } }

    fun getLatestProducts(): Flow<List<Product>> =
        allProducts.map { list -> list.takeLast(20) }

    suspend fun forceRefresh() {
        val remoteProducts = firebaseService.getProductsOnce()
        if (remoteProducts.isNotEmpty()) {
            productDao.insertProducts(remoteProducts.map { it.toEntity() })
        }
    }

    fun getRelatedProducts(productId: String): Flow<List<Product>> =
        allProducts.map { list ->
            val current = list.find { it.id == productId }
            list.filter { it.id != productId && it.categoryId == current?.categoryId }
        }
}
