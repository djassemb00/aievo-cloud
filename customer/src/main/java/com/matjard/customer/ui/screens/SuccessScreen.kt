package com.matjard.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matjard.core.formatPrice
import com.matjard.core.orderCode
import com.matjard.customer.R

@Composable
fun SuccessScreen(
    orderNumber: Long,
    total: Double,
    onTrack: () -> Unit,
    onContinueShopping: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(56.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.order_placed),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.order_placed_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.order_code), fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = orderCode(orderNumber),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${stringResource(R.string.total)} : ${formatPrice(total)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.height(26.dp))
        Button(
            onClick = onTrack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.track_my_order), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        FilledTonalButton(
            onClick = onContinueShopping,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(stringResource(R.string.back_to_shop), fontWeight = FontWeight.Bold)
        }
    }
}
