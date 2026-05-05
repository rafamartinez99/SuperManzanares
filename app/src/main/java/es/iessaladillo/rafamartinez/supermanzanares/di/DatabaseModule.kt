package es.iessaladillo.rafamartinez.supermanzanares.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.iessaladillo.rafamartinez.supermanzanares.data.local.AppDatabase
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CartDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.CategoryDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ProductDao
import es.iessaladillo.rafamartinez.supermanzanares.data.local.ShoppingListDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getDatabase(context)

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao = db.cartDao()

    @Provides
    fun provideShoppingListDao(db:AppDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
}