package com.matjard.admin.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.matjard.core.AdminUser
import com.matjard.core.MatjardClient
import com.matjard.admin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    client: MatjardClient,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }

    // Add employee form state
    var showAddForm by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newRole by remember { mutableStateOf<String?>(null) }
    var addingBusy by remember { mutableStateOf(false) }

    // Which user's role dropdown is open
    var expandedUserId by remember { mutableStateOf<String?>(null) }

    fun loadUsers() {
        refreshing = true
        error = null
        scope.launch {
            try {
                users = client.listAdminUsers()
            } catch (e: Exception) {
                error = "تعذر تحميل قائمة الموظفين"
            } finally {
                refreshing = false
            }
        }
    }

    // Load on first composition
    var loaded by remember { mutableStateOf(false) }
    if (!loaded) {
        loaded = true
        loadUsers()
    }

    val roles = listOf("owner", "manager", "staff")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.employees_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.employees_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))

        error?.let {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        if (refreshing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }

        if (users.isEmpty() && !refreshing) {
            Text(
                stringResource(R.string.no_employees_yet),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            users.forEach { user ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    user.email ?: stringResource(R.string.unknown_email),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    user.id,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (expandedUserId == user.id) {
                                ExposedDropdownMenuBox(
                                    expanded = true,
                                    onExpansionChange = { expanded = it },
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                ) {
                                    ExposedDropdownMenu(
                                        expanded = true,
                                    ) {
                                        roles.forEach { role ->
                                            ExposedDropdownMenuItem(
                                                onClick = {
                                                    scope.launch {
                                                        try {
                                                            busy = true
                                                            client.updateAdminUserRole(user.id, role)
                                                            loadUsers()
                                                        } catch (e: Exception) {
                                                            error = "تعذر تحديث الرتبة"
                                                        } finally {
                                                            busy = false
                                                            expandedUserId = null
                                                        }
                                                    }
                                                },
                                                text = { Text(role.uppercase()) },
                                            )
                                        }
                                    }
                                }
                                FilledTonalIconButton(
                                    onClick = { expandedUserId = null },
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            } else {
                                ChipRole(role = user.role)
                                Spacer(Modifier.width(8.dp))
                                FilledTonalIconButton(
                                    onClick = { expandedUserId = user.id },
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = null)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            busy = true
                                            client.deleteAdminUser(user.id)
                                            loadUsers()
                                        } catch (e: Exception) {
                                            error = "تعذر حذف الموظف"
                                        } finally {
                                            busy = false
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Add employee form
        if (showAddForm) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        stringResource(R.string.add_employee),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text(stringResource(R.string.email)) },
                        leadingIcon = { Icon(Icons.Filled.MailOutline, contentDescription = null) },
                        singleLine = true,
                        enabled = !addingBusy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.password)) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        singleLine = true,
                        enabled = !addingBusy,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    ExposedDropdownMenuBox(
                        expanded = newRole != null,
                        onExpansionChange = { expanded = newRole != null },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ExposedDropdownMenu(
                            expanded = newRole != null,
                        ) {
                            roles.forEach { role ->
                                ExposedDropdownMenuItem(
                                    onClick = { newRole = role },
                                    text = { Text(role.uppercase()) },
                                )
                            }
                        }
                        OutlinedTextField(
                            readOnly = true,
                            value = newRole?.uppercase() ?: stringResource(R.string.select_role),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            label = { Text(stringResource(R.string.role)) },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = { showAddForm = false },
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newEmail.isBlank() || newPassword.isBlank() || newRole == null) {
                                    error = "يرجى تعبئة جميع الحقول"
                                    return@Button
                                }
                                addingBusy = true
                                scope.launch {
                                    try {
                                        client.signUp(newEmail, newPassword)
                                        error = "تم إنشاء الحساب.请 من الموظف تسجيل الدخول مرة واحدة، ثم عيّن رتبته من هنا."
                                        newEmail = ""
                                        newPassword = ""
                                        newRole = null
                                        showAddForm = false
                                        loadUsers()
                                    } catch (e: Exception) {
                                        error = "تعذر إنشاء الحساب: ${e.message}"
                                    } finally {
                                        addingBusy = false
                                    }
                                }
                            },
                            enabled = !addingBusy,
                        ) {
                            if (addingBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            } else {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { showAddForm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                stringResource(R.string.add_employee_btn),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
        }
    }
}

/** chip يظهر الرتبة بلون حسب النوع. */
@Composable
private fun ChipRole(role: String) {
    val color = when (role) {
        "owner" -> MaterialTheme.colorScheme.primary
        "manager" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
    ) {
        Text(
            text = role.uppercase(),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp),
        )
    }
}
