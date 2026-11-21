package com.matjard.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.MatjardClient
import com.matjard.admin.R

@Composable
fun LoginScreen(
    client: MatjardClient,
    onSignedIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Text("D", fontSize = 46.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.login_title), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(stringResource(R.string.login_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text(stringResource(R.string.email)) },
            leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
            singleLine = true,
            enabled = !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            singleLine = true,
            enabled = !busy,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                FilledTonalIconButton(onClick = { showPassword = !showPassword }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
        )

        error?.let { key ->
            Spacer(Modifier.height(12.dp))
            Text(
                when (key) {
                    "invalidCredentials" -> stringResource(R.string.invalid_credentials)
                    "notAdmin" -> stringResource(R.string.not_admin)
                    else -> stringResource(R.string.login_error)
                },
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "loginError"
                    return@Button
                }
                busy = true
                error = null
                scope.launch {
                    try {
                        client.signIn(email, password)
                        onSignedIn()
                    } catch (e: Exception) {
                        error = when (e.message) {
                            "invalidCredentials" -> "invalidCredentials"
                            else -> "loginError"
                        }
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            if (busy) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.size(10.dp))
                Text(stringResource(R.string.signing_in), fontWeight = FontWeight.Bold)
            } else {
                Text(stringResource(R.string.sign_in), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    stringResource(R.string.first_time_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    lineHeight = 17.sp,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Button(
                onClick = onSignUp,
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.create_account), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

