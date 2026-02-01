package com.barter.app.ui.screens.wallet

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(
    onNavigateBack: () -> Unit,
    viewModel: RechargeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAmount by remember { mutableStateOf<Int?>(null) }
    var customAmount by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("alipay") }
    
    val presetAmounts = listOf(10, 30, 50, 100, 200, 500)
    
    // 实际充值金额
    val actualAmount = selectedAmount ?: customAmount.toIntOrNull() ?: 0
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("充值") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { 
                        if (actualAmount > 0) {
                            viewModel.recharge(actualAmount.toDouble(), selectedPayment)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = actualAmount > 0 && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("确认充值 ¥$actualAmount")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 当前余额
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("当前余额", color = Color.Gray)
                    Text(
                        "¥${String.format("%.2f", uiState.currentBalance)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 选择金额
            Text(
                "选择充值金额",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            // 预设金额网格
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in presetAmounts.chunked(3)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { amount ->
                            val isSelected = selectedAmount == amount && customAmount.isEmpty()
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedAmount = amount
                                        customAmount = ""
                                    }
                                    .then(
                                        if (isSelected) Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    ),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                                else Color(0xFFF5F5F5)
                            ) {
                                Text(
                                    "¥$amount",
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 自定义金额
            OutlinedTextField(
                value = customAmount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                        customAmount = it
                        if (it.isNotEmpty()) selectedAmount = null
                    }
                },
                label = { Text("其他金额") },
                placeholder = { Text("输入自定义金额") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Text("¥", color = Color.Gray) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 支付方式
            Text(
                "支付方式",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    // 支付宝
                    ListItem(
                        headlineContent = { Text("支付宝") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color(0xFF1677FF)
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = selectedPayment == "alipay",
                                onClick = { selectedPayment = "alipay" }
                            )
                        },
                        modifier = Modifier.clickable { selectedPayment = "alipay" }
                    )
                    
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // 微信支付
                    ListItem(
                        headlineContent = { Text("微信支付") },
                        leadingContent = {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF07C160)
                            )
                        },
                        trailingContent = {
                            RadioButton(
                                selected = selectedPayment == "wechat",
                                onClick = { selectedPayment = "wechat" }
                            )
                        },
                        modifier = Modifier.clickable { selectedPayment = "wechat" }
                    )
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 充值说明
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "充值说明",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFFF57C00)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "• 充值金额将存入您的钱包余额\n• 余额可用于支付远程交换保证金\n• 充值后不支持退款，请谨慎操作",
                        fontSize = 12.sp,
                        color = Color(0xFFF57C00).copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
