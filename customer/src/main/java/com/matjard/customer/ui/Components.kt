package com.matjard.customer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.matjard.core.Product
import com.matjard.core.formatPrice
import com.matjard.customer.R

@Composable
fun ProductImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isNullOrBlank()) {
        ImagePlaceholder(contentDescription, modifier)
        return
    }
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = { ImagePlaceholder(contentDescription, Modifier.fillMaxSize()) },
        error = { ImagePlaceholder(contentDescription, Modifier.fillMaxSize()) },
    )
}

@Composable
fun ImagePlaceholder(contentDescription: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Devices,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(44.dp),
        )
    }
}

@Composable
fun SectionHeader(title: String, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorRetry(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    enabled: Boolean = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = onDecrease, enabled = enabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 14.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        FilledTonalIconButton(onClick = onIncrease, enabled = enabled, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

/** Emoji for a category icon key — works on every device, no icon font needed. */
fun categoryEmoji(key: String): String = when (key.lowercase()) {
    "smartphone", "phone" -> "📱"
    "laptop", "computer" -> "💻"
    "tv", "screen" -> "📺"
    "kitchen", "appliance", "home" -> "🧺"
    "audio", "headphones" -> "🎧"
    "cable", "accessory" -> "🔌"
    "game" -> "🎮"
    "camera" -> "📷"
    "watch" -> "⌚"
    else -> "⚡"
}

@Composable
fun ProductCard(
    product: Product,
    lang: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box {
                ProductImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name(lang),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                )
                product.discountPercent()?.let { pct ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.discount_off, pct),
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
                if (product.stock <= 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.out_of_stock),
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.name(lang),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatPrice(product.price),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                    )
                    product.oldPrice?.takeIf { it > product.price }?.let { old ->
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formatPrice(old),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                        )
                    }
                }
            }
        }
    }
}
