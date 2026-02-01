package com.barter.app.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barter.app.data.model.WalletTransaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadWalletData()
    }
    
    // 签到提示
    uiState.signInMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSignInMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的钱包") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 钱包卡片
                item {
                    WalletCard(
                        points = uiState.wallet?.points ?: 0,
                        balance = uiState.wallet?.balance ?: 0.0,
                        frozenPoints = uiState.wallet?.frozenPoints ?: 0,
                        frozenBalance = uiState.wallet?.frozenBalance ?: 0.0,
                        signedToday = uiState.wallet?.signedToday ?: false,
                        signInStreak = uiState.wallet?.signInStreak ?: 0,
                        nextSignInPoints = uiState.wallet?.nextSignInPoints ?: 1,
                        onSignIn = { viewModel.signIn() }
                    )
                }
                
                // 信用分卡片
                item {
                    uiState.credit?.let { credit ->
                        CreditCard(credit)
                    }
                }
                
                // 签到提示
                uiState.signInMessage?.let { message ->
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(message, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
                
                // 最近记录
                if (uiState.recentTransactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "最近记录",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    items(uiState.recentTransactions) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletCard(
    points: Int,
    balance: Double,
    frozenPoints: Int,
    frozenBalance: Double,
    signedToday: Boolean,
    signInStreak: Int,
    nextSignInPoints: Int,
    onSignIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF8BC34A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "积分",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$points",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (frozenPoints > 0) {
                            Text(
                                text = "冻结: $frozenPoints",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "余额",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "¥${String.format("%.2f", balance)}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (frozenBalance > 0) {
                            Text(
                                text = "冻结: ¥${String.format("%.2f", frozenBalance)}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 签到按钮
                if (signedToday) {
                    // 已签到
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.5f),
                            contentColor = Color.White
                        ),
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("已签到 · 连续${signInStreak}天")
                    }
                } else {
                    Button(
                        onClick = onSignIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (signInStreak > 0) {
                            Text("签到 +${nextSignInPoints}积分 · 连续${signInStreak}天")
                        } else {
                            Text("签到 +${nextSignInPoints}积分")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditCard(credit: com.barter.app.data.model.CreditInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "信用分",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${credit.creditScore}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = getCreditColor(credit.creditScore)
                    )
                }
                
                Surface(
                    color = getCreditColor(credit.creditScore).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = credit.levelName,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = getCreditColor(credit.creditScore),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 进度条
            credit.nextLevelScore?.let { nextScore ->
                Column {
                    LinearProgressIndicator(
                        progress = credit.creditScore.toFloat() / nextScore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = getCreditColor(credit.creditScore),
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "距离下一等级还需 ${nextScore - credit.creditScore} 分",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 权益说明
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (credit.canRemoteTrade) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (credit.canRemoteTrade) Color(0xFF4CAF50) else Color.Gray
                    )
                    Text(
                        text = "远程交换",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${((1 - credit.depositRatio) * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "保证金减免",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: WalletTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = transaction.description ?: getTransactionTypeName(transaction.type),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = transaction.createdAt?.take(10) ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                transaction.pointsChange?.let { points ->
                    if (points != 0) {
                        Text(
                            text = "${if (points > 0) "+" else ""}$points 积分",
                            color = if (points > 0) Color(0xFF4CAF50) else Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                transaction.balanceChange?.let { balance ->
                    if (balance != 0.0) {
                        Text(
                            text = "${if (balance > 0) "+" else ""}¥${String.format("%.2f", balance)}",
                            color = if (balance > 0) Color(0xFF4CAF50) else Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun getCreditColor(score: Int): Color {
    return when {
        score >= 301 -> Color(0xFF4CAF50)  // 优秀 - 绿色
        score >= 151 -> Color(0xFF2196F3)  // 良好 - 蓝色
        score >= 60 -> Color(0xFFFF9800)   // 普通 - 橙色
        else -> Color(0xFFF44336)          // 新手 - 红色
    }
}

private fun getTransactionTypeName(type: String): String {
    return when (type) {
        "RECHARGE" -> "充值"
        "WITHDRAW" -> "提现"
        "SIGN_IN" -> "签到奖励"
        "TRADE_REWARD" -> "交易奖励"
        "DEPOSIT_FREEZE" -> "保证金冻结"
        "DEPOSIT_UNFREEZE" -> "保证金退还"
        "DEPOSIT_FORFEIT" -> "保证金没收"
        "DEPOSIT_RECEIVE" -> "收到赔偿"
        "INVITE_REWARD" -> "邀请奖励"
        else -> type
    }
}
