package es.iessaladillo.rafamartinez.supermanzanares.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CategoryDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListDao
import es.iessaladillo.rafamartinez.supermanzanares.data.remote.FirebaseService
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.AuthRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.CartRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.CategoryRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.OrderRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ProductRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.ShoppingListRepository
import es.iessaladillo.rafamartinez.supermanzanares.data.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideFirebaseService(): FirebaseService = FirebaseService()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth, firebaseService: FirebaseService
    ): AuthRepository = AuthRepository(firebaseAuth, firebaseService)


    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao, firebaseService: FirebaseService
    ): ProductRepository = ProductRepository(productDao, firebaseService)

    @Provides
    @Singleton
    fun provideCartRepository(
        cartDao: CartDao,
        productDao: ProductDao,
        firebaseService: FirebaseService,
        authRepository: AuthRepository
    ): CartRepository = CartRepository(cartDao, productDao, firebaseService, authRepository)


    @Provides
    @Singleton
    fun provideOrderRepository(firebaseService: FirebaseService): OrderRepository =
        OrderRepository(firebaseService)


    @Provides
    @Singleton
    fun provideUserRepository(firebaseService: FirebaseService): UserRepository =
        UserRepository(firebaseService)


    @Provides
    @Singleton
    fun provideShoppingListRepository(
        shoppingListDao: ShoppingListDao,
        productDao: ProductDao,
        cartDao: CartDao,
        firebaseService: FirebaseService,
        authRepository: AuthRepository
    ): ShoppingListRepository =
        ShoppingListRepository(shoppingListDao, productDao, cartDao, firebaseService, authRepository)

    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository =
        CategoryRepository(categoryDao)
}
