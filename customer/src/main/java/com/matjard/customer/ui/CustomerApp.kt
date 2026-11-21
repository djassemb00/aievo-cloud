package com.matjard.customer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingCart
import com.matjard.core.MatjardClient
import com.matjard.core.StoreSettings
import com.matjard.customer.R
import com.matjard.customer.cart.CartStore
import com.matjard.customer.ui.screens.AboutScreen
import com.matjard.customer.ui.screens.CartScreen
import com.matjard.customer.ui.screens.CategoryScreen
import com.matjard.customer.ui.screens.CheckoutScreen
import com.matjard.customer.ui.screens.HomeScreen
import com.matjard.customer.ui.screens.ProductDetailScreen
import com.matjard.customer.ui.screens.SuccessScreen
import com.matjard.customer.ui.screens.TrackScreen

sealed interface Screen {
    data object Home : Screen
    data class Category(val categoryId: String, val name: String) : Screen
    data class Product(val productId: String) : Screen
    data object Cart : Screen
    data object Checkout : Screen
    data class Success(val orderNumber: Long, val total: Double) : Screen
    data object Track : Screen
    data object About : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerApp() {
    val context = LocalContext.current
    val client = remember { MatjardClient(context.applicationContext) }
    val cartStore = remember { CartStore(context.applicationContext) }
    val lang = client.language.lang()

    val backStack = remember { androidx.compose.runtime.mutableStateListOf<Screen>(Screen.Home) }
    val current = backStack.last()
    var cartVersion by remember { mutableIntStateOf(0) }
    val cartCount = remember(cartVersion) { cartStore.count() }

    var settings by remember { mutableStateOf<StoreSettings?>(null) }
    LaunchedEffect(Unit) {
        settings = runCatching { client.settings() }.getOrNull()
    }

    fun navigate(screen: Screen) = backStack.add(screen)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    BackHandler(enabled = backStack.size > 1) { back() }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.White,
                    actionIconContentColor = androidx.compose.ui.graphics.Color.White,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("D", fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White, fontSize = 17.sp)
                        }
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = settings?.storeName(lang) ?: stringResource(R.string.app_name),
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                        )
                    }
                },
                navigationIcon = {
                    if (backStack.size > 1 && current !is Screen.Success) {
                        IconButton(onClick = ::back) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    if (current !is Screen.About) {
                        IconButton(onClick = { navigate(Screen.About) }) {
                            Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.about_title))
                        }
                    }
                    if (current !is Screen.Track && current !is Screen.Success) {
                        IconButton(onClick = { navigate(Screen.Track) }) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = stringResource(R.string.track_title))
                        }
                    }
                    if (current !is Screen.Cart && current !is Screen.Checkout && current !is Screen.Success) {
                        IconButton(onClick = { navigate(Screen.Cart) }) {
                            BadgedBox(badge = {
                                if (cartCount > 0) Badge(containerColor = MaterialTheme.colorScheme.secondary) { Text(cartCount.toString()) }
                            }) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.cart_title))
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        val body: @Composable () -> Unit = {
            when (val screen = current) {
                is Screen.Home -> HomeScreen(
                    client = client,
                    lang = lang,
                    settings = settings,
                    onProductClick = { navigate(Screen.Product(it)) },
                    onCategoryClick = { id, name -> navigate(Screen.Category(id, name)) },
                )
                is Screen.Category -> CategoryScreen(
                    client = client,
                    categoryId = screen.categoryId,
                    title = screen.name,
                    lang = lang,
                    onProductClick = { navigate(Screen.Product(it)) },
                )
                is Screen.Product -> ProductDetailScreen(
                    client = client,
                    productId = screen.productId,
                    lang = lang,
                    settings = settings,
                    onAddToCart = { product, qty ->
                        cartStore.add(product, qty)
                        cartVersion++
                    },
                    onGoToCart = { cartVersion++; navigate(Screen.Cart) },
                )
                is Screen.Cart -> CartScreen(
                    cartStore = cartStore,
                    lang = lang,
                    settings = settings,
                    cartVersion = cartVersion,
                    onBump = { cartVersion++ },
                    onCheckout = { navigate(Screen.Checkout) },
                    onBrowse = { backStack.clear(); backStack.add(Screen.Home) },
                )
                is Screen.Checkout -> CheckoutScreen(
                    client = client,
                    cartStore = cartStore,
                    lang = lang,
                    settings = settings,
                    onOrderPlaced = { orderNumber, total ->
                        cartVersion++
                        backStack.clear()
                        backStack.add(Screen.Home)
                        backStack.add(Screen.Success(orderNumber, total))
                    },
                )
                is Screen.Success -> SuccessScreen(
                    orderNumber = screen.orderNumber,
                    total = screen.total,
                    onTrack = {
                        backStack.add(Screen.Track)
                    },
                    onContinueShopping = {
                        backStack.clear()
                        backStack.add(Screen.Home)
                    },
                )
                is Screen.Track -> TrackScreen(client = client, cartStore = cartStore)
                is Screen.About -> AboutScreen(client = client, settings = settings, lang = lang)
            }
        }
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(padding)) { body() }
    }
}
