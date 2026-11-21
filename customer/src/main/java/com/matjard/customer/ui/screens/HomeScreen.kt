package com.matjard.customer.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import com.matjard.core.Banner
import com.matjard.core.Category
import com.matjard.core.MatjardClient
import com.matjard.core.Product
import com.matjard.core.StoreSettings
import com.matjard.customer.R
import com.matjard.customer.ui.ErrorRetry
import com.matjard.customer.ui.LoadingBox
import com.matjard.customer.ui.ProductCard
import com.matjard.customer.ui.ProductImage
import com.matjard.customer.ui.SectionHeader
import com.matjard.customer.ui.categoryEmoji
import androidx.compose.foundation.lazy.items as rowItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    client: MatjardClient,
    lang: String,
    settings: StoreSettings?,
    onProductClick: (String) -> Unit,
    onCategoryClick: (String, String) -> Unit,
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var banners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var refreshKey by remember { mutableIntStateOf(0) }

    fun searching() = query.trim().isNotEmpty()

    LaunchedEffect(refreshKey) {
        loading = true
        error = null
        try {
            categories = client.categories()
            banners = client.banners()
            products = client.products()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    LaunchedEffect(query) {
        delay(300)
        products = runCatching { client.products(search = query.trim().ifBlank { null }) }
            .getOrElse { return@LaunchedEffect }
    }

    Column(modifier = Modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
        ) {
            // Search bar
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
                    ),
                )
            }

            when {
                loading && products.isEmpty() ->
                    item(span = { GridItemSpan(maxLineSpan) }) { LoadingBox() }

                error != null && products.isEmpty() ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorRetry(
                            message = stringResource(R.string.error_generic),
                            onRetry = { refreshKey++ },
                        )
                    }

                else -> {
                    // Banners
                    if (!searching() && banners.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            BannerPager(banners = banners, lang = lang)
                        }
                    }

                    // Categories
                    if (!searching() && categories.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader(stringResource(R.string.categories_title))
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                            ) {
                                rowItems(categories, key = { it.id }) { category ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .width(76.dp)
                                            .clickable { onCategoryClick(category.id, category.name(lang)) },
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(categoryEmoji(category.icon), fontSize = 26.sp)
                                        }
                                        Spacer(Modifier.size(6.dp))
                                        Text(
                                            text = category.name(lang),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Featured products (horizontal)
                    val featured = products.filter { it.isFeatured }
                    if (!searching() && featured.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader(stringResource(R.string.featured_title))
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                            ) {
                                rowItems(featured, key = { it.id }) { product ->
                                    ProductCard(
                                        product = product,
                                        lang = lang,
                                        onClick = { onProductClick(product.id) },
                                        modifier = Modifier.width(165.dp),
                                    )
                                }
                            }
                        }
                    }

                    // Grid header
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SectionHeader(
                            if (searching()) stringResource(R.string.search_results, products.size)
                            else stringResource(R.string.all_products_title)
                        )
                    }

                    if (products.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(R.string.no_results),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                lang = lang,
                                onClick = { onProductClick(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerPager(banners: List<Banner>, lang: String) {
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(banners.size) {
        if (banners.size <= 1) return@LaunchedEffect
        while (true) {
            delay(4500)
            val next = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(next)
        }
    }

    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
        pageSpacing = 10.dp,
    ) { page ->
        val banner = banners[page]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp)),
        ) {
            ProductImage(
                imageUrl = banner.imageUrl,
                contentDescription = banner.title(lang),
                modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color.Transparent,
                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.72f),
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
            ) {
                Text(
                    text = banner.title(lang),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                )
                banner.subtitle(lang)?.let {
                    Text(
                        text = it,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
