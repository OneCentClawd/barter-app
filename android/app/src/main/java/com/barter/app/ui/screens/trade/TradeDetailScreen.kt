package com.barter.app.ui.screens.trade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.BuildConfig
import com.barter.app.data.model.TradeStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeDetailScreen(
    tradeId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToItemDetail: (Long) -> Unit = {},
    viewModel: TradeDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tradeId) {
        viewModel.loadTrade(tradeId)
    }

    LaunchedEffect(uiState.isActionSuccess) {
        if (uiState.isActionSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交换详情") },
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
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.error!!, color = Color.Red)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 状态卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = getStatusColor(uiState.status).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getStatusText(uiState.status),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(uiState.status)
                        )
                    }
                }

                // 交换物品展示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 对方物品
                    ItemCard(
                        title = uiState.targetItemTitle ?: "",
                        imageUrl = uiState.targetItemImage,
                        ownerName = uiState.targetOwnerName ?: "",
                        onClick = { uiState.targetItemId?.let { onNavigateToItemDetail(it) } }
                    )

                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // 我方物品
                    ItemCard(
                        title = uiState.offeredItemTitle ?: "",
                        imageUrl = uiState.offeredItemImage,
                        ownerName = uiState.offeredOwnerName ?: "",
                        onClick = { uiState.offeredItemId?.let { onNavigateToItemDetail(it) } }
                    )
                }

                // 留言
                if (!uiState.message.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "留言",
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.message!!)
                        }
                    }
                }

                // 操作按钮（对收到的请求：接受/拒绝）
                if (uiState.canRespond && uiState.status == TradeStatus.PENDING) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.rejectTrade() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isActioning
                        ) {
                            Text("拒绝")
                        }
                        
                        Button(
                            onClick = { viewModel.acceptTrade() },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isActioning
                        ) {
                            Text("接受")
                        }
                    }
                }
                
                // 取消按钮（发起方在 PENDING 状态可取消）
                if (uiState.isRequester && uiState.status == TradeStatus.PENDING) {
                    OutlinedButton(
                        onClick = { viewModel.cancelTrade() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isActioning,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("取消交换请求")
                    }
                }

                // 已接受的交易 - 显示确认完成按钮和取消按钮
                if (uiState.status == TradeStatus.ACCEPTED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "交换已达成，请在完成线下交换后双方都确认",
                                fontSize = 14.sp,
                                color = Color(0xFFE65100)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 显示双方确认状态
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ConfirmStatusChip(
                                    label = if (uiState.isRequester) "我" else "对方",
                                    confirmed = uiState.requesterConfirmed
                                )
                                ConfirmStatusChip(
                                    label = if (uiState.isRequester) "对方" else "我",
                                    confirmed = uiState.targetConfirmed
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (uiState.myConfirmed) {
                                Text(
                                    text = "您已确认，等待对方确认",
                                    fontSize = 13.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            } else {
                                Button(
                                    onClick = { viewModel.completeTrade() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isActioning
                                ) {
                                    Text("确认完成交换")
                                }
                            }
                        }
                    }
                    
                    // 取消按钮（ACCEPTED 状态双方都可取消）
                    OutlinedButton(
                        onClick = { viewModel.cancelTrade() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isActioning,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("取消交换")
                    }
                }

                // 错误提示
                if (uiState.actionError != null) {
                    Text(
                        text = uiState.actionError!!,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmStatusChip(
    label: String,
    confirmed: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (confirmed) Icons.Default.Check else Icons.Default.Schedule,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (confirmed) Color(0xFF4CAF50) else Color.Gray
        )
        Text(
            text = "$label: ${if (confirmed) "已确认" else "待确认"}",
            fontSize = 13.sp,
            color = if (confirmed) Color(0xFF4CAF50) else Color.Gray
        )
    }
}

@Composable
private fun ItemCard(
    title: String,
    imageUrl: String?,
    ownerName: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            val fullImageUrl = imageUrl?.let {
                if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
            }
            AsyncImage(
                model = fullImageUrl ?: "https://via.placeholder.com/140",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = ownerName,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun getStatusText(status: TradeStatus?): String {
    return when (status) {
        TradeStatus.PENDING -> "等待对方确认"
        TradeStatus.ACCEPTED -> "交换已达成"
        TradeStatus.REJECTED -> "交换被拒绝"
        TradeStatus.CANCELLED -> "交换已取消"
        TradeStatus.COMPLETED -> "交换已完成"
        null -> "未知状态"
    }
}

private fun getStatusColor(status: TradeStatus?): Color {
    return when (status) {
        TradeStatus.PENDING -> Color(0xFFFF9800)
        TradeStatus.ACCEPTED, TradeStatus.COMPLETED -> Color(0xFF4CAF50)
        TradeStatus.REJECTED, TradeStatus.CANCELLED -> Color(0xFFF44336)
        null -> Color.Gray
    }
}
