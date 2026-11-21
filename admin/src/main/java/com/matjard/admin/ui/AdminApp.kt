package com.matjard.admin.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.admin.R
import com.matjard.admin.ui.screens.BannersScreen
import com.matjard.admin.ui.screens.CategoriesScreen
import com.matjard.admin.ui.screens.DashboardScreen
import com.matjard.admin.ui.screens.LoginScreen
import com.matjard.admin.ui.screens.MoreScreen
import com.matjard.admin.ui.screens.OrdersScreen
import com.matjard.admin.ui.screens.ProductsScreen
import com.matjard.admin.ui.screens.ProductEditScreen
import com.matjard.admin.ui.screens.SettingsScreen
import com.matjard.admin.ui.screens.SignUpScreen
import com.matjard.admin.ui.screens.EmployeesScreen

private enum class Tab { Dashboard, Orders, Products, Categories, More }

private sealed interface Overlay {
    data class ProductEdit(val product: Product?) : Overlay
    data object Banners : Overlay
    data object Settings : Overlay
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp() {
    val context = LocalContext.current
    val client = remember { MatjardClient(context.applicationContext) }

    var signedIn by remember { mutableStateOf(client.signedIn()) }
    var sessionChecked by remember { mutableStateOf(false) }
    var showingSignUp by remember { mutableStateOf(false) }
    var showingEmployees by remember { mutableStateOf(false) }
    // زر التسجيل الذاتي يظهر فقط إذا لم يكن هناك أي مسؤول بعد (أول responsible يسجل يصبح مالكاً)
    var showSignUp by remember { mutableStateOf(true) }

    // إعادة التحقق من وجود مسؤولين كلما تغيرت حالة تسجيل الدخول،
    // ليختفي زر التسجيل من جميع نسخ التطبيق فوراً بعد تسجيل أي مسؤول
    LaunchedEffect(signedIn) {
        showSignUp = try {
            !client.anyAdminExists()
        } catch (e: Exception) {
            true
        }
        if (!signedIn && client.signedIn()) {
            signedIn = client.ensureFreshToken()
            // أول مستخدم registrado يصبح مالكاً تلقائياً
            client.ensureOwnerIfFirst()
        }
        sessionChecked = true
    }

    if (!sessionChecked) {
        LoadingBox()
        return
    }

    if (!signedIn) {
        if (showingSignUp) {
            SignUpScreen(
                client = client,
                onSignedIn = { signedIn = true },
                onBackToLogin = { showingSignUp = false },
            )
        } else {
            LoginScreen(
                client = client,
                onSignedIn = { signedIn = true },
                onSignUp = { showingSignUp = true },
                showSignUp = showSignUp,
            )
        }
        return
    }

    if (showingEmployees) {
        EmployeesScreen(
            client = client,
            onClose = { showingEmployees = false },
        )
        return
    }

    AdminScaffold(
        client = client,
        onSignOut = { client.signOut(); signedIn = false },
        onOpenEmployees = { showingEmployees = true },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminScaffold(
    client: MatjardClient,
    onSignOut: () -> Unit,
    onOpenEmployees: () -> Unit,
) {
    val lang = client.language.lang()

    var tab by remember { mutableStateOf(Tab.Dashboard) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    fun closeOverlay() { overlay = null }
    BackHandler(enabled = overlay != null) { closeOverlay() }

    // Fullscreen overlays render on top of everything
    when (val current = overlay) {
        is Overlay.ProductEdit -> {
            ProductEditScreen(client = client, product = current.product, lang = lang, onClose = { closeOverlay() })
            return
        }
        Overlay.Banners -> {
            BannersScreen(client = client, lang = lang, onClose = { closeOverlay() })
            return
        }
        Overlay.Settings -> {
            SettingsScreen(client = client, onClose = { closeOverlay() })
            return
        }
        null -> Unit
    }

    // All tabs visible to every logged-in user (simple experience for seller)
    val items = listOf(
        Tab.Dashboard to (Icons.Filled.Dashboard to R.string.tab_dashboard),
        Tab.Orders to (Icons.Filled.ReceiptLong to R.string.tab_orders),
        Tab.Products to (Icons.Filled.Inventory2 to R.string.tab_products),
        Tab.Categories to (Icons.Filled.Category to R.string.tab_categories),
        Tab.More to (Icons.Filled.MoreHoriz to R.string.tab_more),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = { Text(stringResource(R.string.app_name), fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Filled.Logout, contentDescription = stringResource(R.string.sign_out))
                    }
                    IconButton(onClick = onOpenEmployees) {
                        Icon(Icons.Filled.People, contentDescription = stringResource(R.string.employees_title))
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEach { (entry, pair) ->
                    val (icon, labelRes) = pair
                    NavigationBarItem(
                        selected = tab == entry,
                        onClick = { tab = entry },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                Tab.Dashboard -> DashboardScreen(
                    client = client,
                    onViewAllOrders = { tab = Tab.Orders },
                )
                Tab.Orders -> OrdersScreen(client = client)
                Tab.Products -> ProductsScreen(
                    client = client,
                    lang = lang,
                    onAdd = { overlay = Overlay.ProductEdit(null) },
                    onEdit = { overlay = Overlay.ProductEdit(it) },
                )
                Tab.Categories -> CategoriesScreen(client = client, lang = lang)
                Tab.More -> MoreScreen(
                    client = client,
                    lang = lang,
                    onOpenSettings = { overlay = Overlay.Settings },
                    onOpenBanners = { overlay = Overlay.Banners },
                    isManager = true,
                )
            }
        }
    }
}
