package es.iessaladillo.rafamartinez.supermanzanares.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.AddAddressScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.CartScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.CategoryScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.EditAddressScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.EditEmailScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.EditNameScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.EditPasswordScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.EditProfileScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ForgotPasswordScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.HomeScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ListDetailScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ListScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.LoginScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.OrderConfirmationScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.OrderHistoryScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.OrderSuccessScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ProductDetailScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ProductsByCategoryScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.ProfileScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.RegisterScreen
import es.iessaladillo.rafamartinez.supermanzanares.ui.screens.SearchScreen
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.AuthViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CartViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.CategoryViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.MapboxViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.OrderViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ProductViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.ShoppingListViewModel
import es.iessaladillo.rafamartinez.supermanzanares.viewmodel.UserViewModel

@Composable
fun SuperManzanaresNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    shoppingListViewModel: ShoppingListViewModel,
    categoryViewModel: CategoryViewModel,
    mapboxViewModel: MapboxViewModel,
    bottomBarVisible: Boolean
) {
    val contentBottomPadding = if (bottomBarVisible) 80.dp else 0.dp

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(bottom = contentBottomPadding)
    ) {
        composable("home") {
            HomeScreen(productViewModel, cartViewModel, navController)
        }
        composable("categories") {
            CategoryScreen(
                viewModel = categoryViewModel, onCategoryClick = { categoryId ->
                    navController.navigate("productsByCategory/$categoryId")
                })
        }
        composable("productsByCategory/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull()

            if (categoryId != null) {
                ProductsByCategoryScreen(
                    categoryId = categoryId, productViewModel, cartViewModel, navController
                )
            }
        }
        composable("lists") { ListScreen(shoppingListViewModel, navController) }
        composable("list_detail/{listId}") { backStackEntry ->
            val listId = backStackEntry.arguments?.getString("listId")?.toIntOrNull()
            if (listId != null) {
                ListDetailScreen(listId, shoppingListViewModel, navController)
            }
        }
        composable("profile") {
            ProfileScreen(navController, userViewModel, authViewModel)
        }
        composable("cart") {
            CartScreen(navController, cartViewModel, userViewModel)
        }
        composable("order_confirmation") {
            OrderConfirmationScreen(navController, cartViewModel, orderViewModel, userViewModel)
        }
        composable("order_history") {
            OrderHistoryScreen(orderViewModel, userViewModel, cartViewModel, navController)
        }
        composable("product_detail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId, navController, productViewModel, cartViewModel, shoppingListViewModel
            )
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("search") {
            SearchScreen(navController, productViewModel, cartViewModel)
        }
        composable("order_success") {
            OrderSuccessScreen(navController, cartViewModel)
        }
        composable("edit_profile") {
            EditProfileScreen(navController, userViewModel)
        }
        composable("edit_name") {
            EditNameScreen(navController, userViewModel)
        }
        composable("edit_email") {
            EditEmailScreen(navController, userViewModel)
        }
        composable("edit_password") {
            EditPasswordScreen(navController)
        }
        composable("edit_address") {
            EditAddressScreen(navController, userViewModel)
        }
        composable("add_address_from_edit") {
            AddAddressScreen(
                navController, userViewModel, mapboxViewModel, origin = "edit"
            )
        }
        composable("add_address_from_register") {
            AddAddressScreen(
                navController,
                userViewModel,
                mapboxViewModel,
                origin = "register"
            )
        }
        composable("add_address_from_google") {
            AddAddressScreen(
                navController, userViewModel, mapboxViewModel, origin = "google"
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController = navController)
        }
    }
}
