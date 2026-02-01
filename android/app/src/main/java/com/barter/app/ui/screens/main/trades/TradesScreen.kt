package com.barter.app.ui.screens.main.trades

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.barter.app.data.model.TradeRequest
import com.barter.app.data.model.TradeStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
    viewModel: TradesViewModel = hiltViewModel(),
    onNavigateToTradeDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("收到的", "发起的")

    LaunchedEffect(Unit) {
        viewModel.loadTrades()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                val trades = if (selectedTab == 0) uiState.receivedTrades else uiState.sentTrades

                if (trades.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无交换请求")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(trades) { trade ->
                            TradeCard(
                                trade = trade,
                                isReceived = selectedTab == 0,
                                onClick = { onNavigateToTradeDetail(trade.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TradeCard(
    trade: TradeRequest,
    isReceived: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isReceived) "来自 ${trade.requester.nickname ?: trade.requester.username}" else "发给 ${trade.targetItem.owner.nickname ?: trade.targetItem.owner.username}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                StatusChip(status = trade.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 物品交换
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 想要的物品
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val targetImage = trade.targetItem.coverImage?.let {
                        if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                    }
                    AsyncImage(
                        model = targetImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trade.targetItem.title,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // 交换的物品
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val offeredImage = trade.offeredItem.coverImage?.let {
                        if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                    }
                    AsyncImage(
                        model = offeredImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trade.offeredItem.title,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // 消息
            trade.message?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$message\"",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: TradeStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TradeStatus.PENDING -> Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), "待处理")
        TradeStatus.ACCEPTED -> Triple(Color(0xFFE3F2FD), Color(0xFF2196F3), "已接受")
        TradeStatus.DEPOSIT_PAID -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "已付保证金")
        TradeStatus.SHIPPING -> Triple(Color(0xFFFFF8E1), Color(0xFFFFA000), "运输中")
        TradeStatus.DELIVERED -> Triple(Color(0xFFE1F5FE), Color(0xFF03A9F4), "待确认收货")
        TradeStatus.REJECTED -> Triple(Color(0xFFFFEBEE), Color(0xFFF44336), "已拒绝")
        TradeStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), "已完成")
        TradeStatus.CANCELLED -> Triple(Color(0xFFFAFAFA), Color(0xFF9E9E9E), "已取消")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
