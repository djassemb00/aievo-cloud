package com.matjard.admin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.TextButton
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

sealed class LoginStage {
    object Password : LoginStage()
    object Otp : LoginStage()
}

@Composable
fun LoginScreen(
    client: MatjardClient,
    onSignedIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var otpSent by remember { mutableStateOf(false) }
    var resendEnabled by remember { mutableStateOf(true) }
    var resendCountdown by remember { mutableStateOf(60) }
    var stage by remember { mutableStateOf<LoginStage>(LoginStage.Password) }

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
        when (stage) {
            LoginStage.Password -> {
                Text(stringResource(R.string.login_title), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.login_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LoginStage.Otp -> {
                Text(stringResource(R.string.verify_title), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.verify_subtitle_for, email), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(28.dp))

        if (stage == LoginStage.Password) {
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
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "يرجى تعبئة البريد الإلكتروني وكلمة المرور"
                        return@Button
                    }
                    busy = true
                    error = null
                    scope.launch {
                        try {
                            client.signIn(email, password)
                            client.sendLoginOTP(email)
                            otpSent = true
                            resendCountdown = 60
                            stage = LoginStage.Otp
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
        } else {
            Text(stringResource(R.string.enter_code_sent_to, email), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it; error = null },
                label = { Text(stringResource(R.string.otp_code)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                maxLength = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        if (otpCode.length != 6) {
                            error = "يرجى إدخال رمز مكون من 6 أرقام"
                            return@Button
                        }
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                val ok = client.verifyLoginOTP(email, otpCode)
                                if (ok) {
                                    onSignedIn()
                                } else {
                                    error = "رمز غير صحيح أو منتهي الصلاحية"
                                }
                            } catch (e: Exception) {
                                error = "تعذر التحقق من الرمز. حاول مجدداً."
                            } finally {
                                busy = false
                            }
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(54.dp).weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.size(10.dp))
                        Text(stringResource(R.string.verifying), fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(R.string.verify_code), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (!resendEnabled) {
                            error = "يرجى الانتظار ${resendCountdown} ثانية قبل إعادة الإرسال"
                            return@Button
                        }
                        busy = true
                        scope.launch {
                            try {
                                client.sendLoginOTP(email)
                                resendCountdown = 60
                                otpSent = true
                            } catch (e: Exception) {
                                error = "تعذر إعادة إرسال الرمز"
                            } finally {
                                busy = false
                            }
                        }
                    },
                    enabled = !busy && resendEnabled,
                    modifier = Modifier.width(110.dp).height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else if (!resendEnabled) {
                        Text("إعادة الإرسال (${resendCountdown}s)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(stringResource(R.string.resend_code), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { stage = LoginStage.Password; otpSent = false; otpCode = "" },
            ) {
                Text(stringResource(R.string.change_email), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            if (otpSent) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
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
                            stringResource(R.string.code_sent_info),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }

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

        Spacer(Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Row(
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
        if (stage == LoginStage.Password) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                Button(
                    onClick = onSignUp,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(stringResource(R.string.create_new_account), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
