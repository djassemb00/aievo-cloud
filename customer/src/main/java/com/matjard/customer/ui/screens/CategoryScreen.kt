package com.matjard.customer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.customer.R
import com.matjard.customer.ui.ErrorRetry
import com.matjard.customer.ui.LoadingBox
import com.matjard.customer.ui.ProductCard

@Composable
fun CategoryScreen(
    client: MatjardClient,
    categoryId: String,
    title: String,
    lang: String,
    onProductClick: (String) -> Unit,
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(categoryId, reloadKey) {
        loading = true
        error = false
        try {
            products = client.products(categoryId = categoryId)
        } catch (e: Exception) {
            error = true
        } finally {
            loading = false
        }
    }

    when {
        loading -> LoadingBox()
        error -> ErrorRetry(message = stringResource(R.string.error_generic), onRetry = { reloadKey++ })
        products.isEmpty() -> Text(
            text = stringResource(R.string.no_results),
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(product = product, lang = lang, onClick = { onProductClick(product.id) })
            }
        }
    }
}
