package com.barter.app.ui.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    val codeSent: Boolean = false,
    val cooldownSeconds: Int = 0,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val cachedEmails: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onLoginWithPassword: (String, String) -> Unit,
    onLoginWithCode: (String, String) -> Unit,
    onSendLoginCode: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf(uiState.cachedEmails.firstOrNull() ?: "") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var useCodeLogin by remember { mutableStateOf(false) }
    var showEmailDropdown by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    // 当cachedEmails更新时，自动填充第一个
    LaunchedEffect(uiState.cachedEmails) {
        if (email.isEmpty() && uiState.cachedEmails.isNotEmpty()) {
            email = uiState.cachedEmails.first()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎回来",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "登录以继续使用易物",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 邮箱输入框（带历史下拉）
        ExposedDropdownMenuBox(
            expanded = showEmailDropdown && uiState.cachedEmails.isNotEmpty(),
            onExpandedChange = { showEmailDropdown = it }
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = {
                    if (uiState.cachedEmails.isNotEmpty()) {
                        IconButton(onClick = { showEmailDropdown = !showEmailDropdown }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "选择邮箱")
                        }
                    }
                }
            )
            ExposedDropdownMenu(
                expanded = showEmailDropdown && uiState.cachedEmails.isNotEmpty(),
                onDismissRequest = { showEmailDropdown = false }
            ) {
                uiState.cachedEmails.forEach { cachedEmail ->
                    DropdownMenuItem(
                        text = { Text(cachedEmail) },
                        onClick = {
                            email = cachedEmail
                            showEmailDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 使用 Crossfade 实现平滑切换
        Crossfade(targetState = useCodeLogin, label = "login_mode") { isCodeLogin ->
            if (!isCodeLogin) {
                // 密码输入框
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码"
                            )
                        }
                    }
                )
            } else {
                // 验证码输入框
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 6) verificationCode = it },
                    label = { Text("验证码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("请输入6位验证码") },
                    trailingIcon = {
                        TextButton(
                            onClick = { 
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    onSendLoginCode(email)
                                } else {
                                    localError = "请输入正确的邮箱地址"
                                }
                            },
                            enabled = !uiState.isSendingCode && uiState.cooldownSeconds == 0 && email.isNotBlank()
                        ) {
                            if (uiState.isSendingCode) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else if (uiState.cooldownSeconds > 0) {
                                Text("${uiState.cooldownSeconds}s", fontSize = 12.sp)
                            } else {
                                Text(if (uiState.codeSent) "重新发送" else "获取验证码", fontSize = 12.sp)
                            }
                        }
                    }
                )
            }
        }

        val error = uiState.error ?: localError
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 切换登录方式（文字链接形式，放在输入框下方）
        Text(
            text = if (useCodeLogin) "使用密码登录" else "使用验证码登录",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            modifier = Modifier.clickable { 
                useCodeLogin = !useCodeLogin
                localError = null
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                localError = null
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    localError = "请输入正确的邮箱地址"
                    return@Button
                }
                if (useCodeLogin) {
                    if (verificationCode.length != 6) {
                        localError = "请输入6位验证码"
                        return@Button
                    }
                    onLoginWithCode(email, verificationCode)
                } else {
                    if (password.length < 6) {
                        localError = "密码至少6位"
                        return@Button
                    }
                    onLoginWithPassword(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading && email.isNotBlank() && 
                     (if (useCodeLogin) verificationCode.isNotBlank() else password.isNotBlank())
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("登录", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text(
                text = "还没有账号？",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "立即注册",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}
