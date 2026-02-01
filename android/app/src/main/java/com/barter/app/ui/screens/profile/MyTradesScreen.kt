package com.barter.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.BuildConfig
import com.barter.app.data.model.TradeRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTradesScreen(
    viewModel: MyTradesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTradeDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyTrades()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的交易") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.trades.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无交易记录", color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trades) { trade ->
                        TradeCard(trade = trade, onClick = { onNavigateToTradeDetail(trade.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeCard(trade: TradeRequest, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 状态标签
            val (statusText, statusColor) = when (trade.status.name) {
                "PENDING" -> "待处理" to Color(0xFFFF9800)
                "ACCEPTED" -> "已接受" to Color(0xFF4CAF50)
                "REJECTED" -> "已拒绝" to Color(0xFFF44336)
                "COMPLETED" -> "已完成" to Color(0xFF2196F3)
                "CANCELLED" -> "已取消" to Color.Gray
                else -> trade.status.name to Color.Gray
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 交换物品
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 我的物品
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val myItemImage = trade.offeredItem.coverImage?.let {
                        if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                    }
                    AsyncImage(
                        model = myItemImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trade.offeredItem.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                // 对方物品
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val targetItemImage = trade.targetItem.coverImage?.let {
                        if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                    }
                    AsyncImage(
                        model = targetItemImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trade.targetItem.title,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
