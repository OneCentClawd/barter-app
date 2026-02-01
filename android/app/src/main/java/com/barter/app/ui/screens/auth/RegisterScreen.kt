package com.barter.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    val codeSent: Boolean = false,
    val cooldownSeconds: Int = 0,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onSendCode: (String) -> Unit,
    onRegister: (String, String, String, String, String, Long?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var referrerId by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("注册") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "创建账号",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 邮箱 + 发送验证码
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = {
                    TextButton(
                        onClick = { 
                            if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                onSendCode(email) 
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

            Spacer(modifier = Modifier.height(12.dp))

            // 验证码输入
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { if (it.length <= 6) verificationCode = it },
                label = { Text("验证码 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("请输入6位验证码") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("确认密码 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 推荐人ID
            OutlinedTextField(
                value = referrerId,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) referrerId = it },
                label = { Text("推荐人ID（可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("填写推荐人ID，推荐人可获得50积分奖励", color = Color.Gray, fontSize = 12.sp) }
            )

            val error = uiState.error ?: localError
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    localError = null
                    when {
                        username.length < 3 -> localError = "用户名至少3个字符"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> localError = "邮箱格式不正确"
                        verificationCode.length != 6 -> localError = "请输入6位验证码"
                        password.length < 6 -> localError = "密码至少6个字符"
                        password != confirmPassword -> localError = "两次密码不一致"
                        else -> onRegister(
                            username, 
                            email, 
                            password, 
                            nickname.ifBlank { username },
                            verificationCode,
                            referrerId.toLongOrNull()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading && username.isNotBlank() && email.isNotBlank() 
                        && verificationCode.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("注册", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(
                    text = "已有账号？",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "立即登录",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigateBack() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
