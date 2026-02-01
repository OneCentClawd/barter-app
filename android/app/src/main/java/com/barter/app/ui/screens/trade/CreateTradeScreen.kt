package com.barter.app.ui.screens.trade

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.BuildConfig
import com.barter.app.data.model.ItemListItem
import com.barter.app.data.model.TradeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTradeScreen(
    targetItemId: Long,
    viewModel: CreateTradeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onTradeCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedItem by remember { mutableStateOf<ItemListItem?>(null) }
    var message by remember { mutableStateOf("") }
    var tradeMode by remember { mutableStateOf(TradeMode.IN_PERSON) }
    var estimatedValue by remember { mutableStateOf("") }

    LaunchedEffect(targetItemId) {
        viewModel.loadData(targetItemId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onTradeCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发起交换") },
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
                        selectedItem?.let { item ->
                            viewModel.createTrade(
                                targetItemId = targetItemId,
                                offeredItemId = item.id,
                                message = message.ifBlank { null },
                                tradeMode = tradeMode,
                                estimatedValue = if (tradeMode == TradeMode.REMOTE) 
                                    estimatedValue.toDoubleOrNull() else null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedItem != null && !uiState.isLoading &&
                            (tradeMode == TradeMode.IN_PERSON || estimatedValue.toDoubleOrNull() != null)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("确认交换")
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoadingItems -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    // 目标物品
                    uiState.targetItem?.let { target ->
                        Text("想要的物品", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val imageUrl = target.images?.firstOrNull()?.let {
                                    if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                                }
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(target.title, fontWeight = FontWeight.Medium)
                                    Text(
                                        target.owner.nickname ?: target.owner.username,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 选择我的物品
                    Text("选择您的物品", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.myItems.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                        ) {
                            Text(
                                "您还没有可用的物品，请先发布物品",
                                modifier = Modifier.padding(16.dp),
                                color = Color(0xFFFF9800)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.myItems) { item ->
                                val isSelected = selectedItem?.id == item.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedItem = item }
                                        .then(
                                            if (isSelected) Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(12.dp)
                                            ) else Modifier
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val imageUrl = item.coverImage?.let {
                                            if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                                        }
                                        AsyncImage(
                                            model = imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(item.title, modifier = Modifier.weight(1f))
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 交易模式选择
                    Text("交易方式", fontWeight = FontWeight.Medium, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = tradeMode == TradeMode.IN_PERSON,
                            onClick = { tradeMode = TradeMode.IN_PERSON },
                            label = { Text("面交") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Handshake,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilterChip(
                            selected = tradeMode == TradeMode.REMOTE,
                            onClick = { tradeMode = TradeMode.REMOTE },
                            label = { Text("远程") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 远程交易需要填估值
                    if (tradeMode == TradeMode.REMOTE) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "远程交换需要双方支付保证金",
                                        fontSize = 13.sp,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "保证金根据信用等级减免，交易完成后退还",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1565C0).copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = estimatedValue,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    estimatedValue = it
                                }
                            },
                            label = { Text("物品估值（元）") },
                            placeholder = { Text("用于计算保证金") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = { Text("¥") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 留言
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("给对方留言（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // 错误
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
