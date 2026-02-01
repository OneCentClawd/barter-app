package com.barter.app.ui.screens.trade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.res.painterResource
import com.barter.app.R
import com.barter.app.BuildConfig
import com.barter.app.data.model.TradeMode
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
    var showShipDialog by remember { mutableStateOf(false) }
    var trackingNo by remember { mutableStateOf("") }

    LaunchedEffect(tradeId) {
        viewModel.loadTrade(tradeId)
    }
    
    // 显示消息
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.actionMessage, uiState.actionError) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    // 发货对话框
    if (showShipDialog) {
        AlertDialog(
            onDismissRequest = { showShipDialog = false },
            title = { Text("填写物流单号") },
            text = {
                OutlinedTextField(
                    value = trackingNo,
                    onValueChange = { trackingNo = it },
                    label = { Text("物流单号") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (trackingNo.isNotBlank()) {
                            viewModel.shipItem(trackingNo)
                            showShipDialog = false
                            trackingNo = ""
                        }
                    }
                ) {
                    Text("确认发货")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShipDialog = false }) {
                    Text("取消")
                }
            }
        )
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = getStatusText(uiState.status),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = getStatusColor(uiState.status)
                        )
                        
                        // 交易模式标签
                        if (uiState.tradeMode == TradeMode.REMOTE) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF2196F3)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "远程交换",
                                        color = Color(0xFF2196F3),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 交换物品展示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    ItemCard(
                        title = uiState.offeredItemTitle ?: "",
                        imageUrl = uiState.offeredItemImage,
                        ownerName = uiState.offeredOwnerName ?: "",
                        onClick = { uiState.offeredItemId?.let { onNavigateToItemDetail(it) } }
                    )
                }

                // 留言
                if (!uiState.message.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("留言", fontWeight = FontWeight.Medium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.message!!)
                        }
                    }
                }
                
                // 远程交易估值
                if (uiState.tradeMode == TradeMode.REMOTE && uiState.estimatedValue != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("物品估值", color = Color.Gray)
                            Text("¥${String.format("%.2f", uiState.estimatedValue)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ========== 根据状态显示不同操作 ==========
                
                // PENDING: 收到的请求可以接受/拒绝，发起的请求可以取消
                if (uiState.status == TradeStatus.PENDING) {
                    if (uiState.canRespond) {
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
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.cancelTrade() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isActioning,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("取消交换请求")
                        }
                    }
                }
                
                // ACCEPTED: 面交显示确认按钮，远程显示支付保证金
                if (uiState.status == TradeStatus.ACCEPTED) {
                    if (uiState.tradeMode == TradeMode.REMOTE) {
                        // 远程交易：支付保证金
                        RemoteTradeDepositSection(
                            myDepositPaid = uiState.myDepositPaid,
                            requesterDepositPaid = uiState.requesterDepositPaid,
                            targetDepositPaid = uiState.targetDepositPaid,
                            isRequester = uiState.isRequester,
                            isActioning = uiState.isActioning,
                            onPayDeposit = { viewModel.payDeposit() },
                            onCancel = { viewModel.cancelTrade() }
                        )
                    } else {
                        // 面交：确认完成
                        InPersonTradeSection(
                            myConfirmed = uiState.myConfirmed,
                            requesterConfirmed = uiState.requesterConfirmed,
                            targetConfirmed = uiState.targetConfirmed,
                            isRequester = uiState.isRequester,
                            isActioning = uiState.isActioning,
                            onConfirm = { viewModel.completeTrade() },
                            onCancel = { viewModel.cancelTrade() }
                        )
                    }
                }
                
                // DEPOSIT_PAID: 发货
                if (uiState.status == TradeStatus.DEPOSIT_PAID) {
                    ShippingSection(
                        myTrackingNo = uiState.myTrackingNo,
                        otherTrackingNo = uiState.otherTrackingNo,
                        isActioning = uiState.isActioning,
                        onShip = { showShipDialog = true }
                    )
                }
                
                // SHIPPING: 等待双方发货
                if (uiState.status == TradeStatus.SHIPPING) {
                    ShippingStatusSection(
                        requesterTrackingNo = uiState.requesterTrackingNo,
                        targetTrackingNo = uiState.targetTrackingNo,
                        isRequester = uiState.isRequester,
                        myTrackingNo = uiState.myTrackingNo,
                        isActioning = uiState.isActioning,
                        onShip = { showShipDialog = true }
                    )
                }
                
                // DELIVERED: 确认收货
                if (uiState.status == TradeStatus.DELIVERED) {
                    DeliveredSection(
                        myConfirmed = uiState.myConfirmed,
                        requesterConfirmed = uiState.requesterConfirmed,
                        targetConfirmed = uiState.targetConfirmed,
                        isRequester = uiState.isRequester,
                        isActioning = uiState.isActioning,
                        onConfirm = { viewModel.completeTrade() }
                    )
                }
                
                // COMPLETED: 显示完成信息
                if (uiState.status == TradeStatus.COMPLETED) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "交换已完成！",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }
        
        // 加载遮罩
        if (uiState.isActioning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun RemoteTradeDepositSection(
    myDepositPaid: Boolean,
    requesterDepositPaid: Boolean,
    targetDepositPaid: Boolean,
    isRequester: Boolean,
    isActioning: Boolean,
    onPayDeposit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "远程交换需要双方支付保证金",
                fontSize = 14.sp,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConfirmStatusChip(
                    label = if (isRequester) "我" else "对方",
                    confirmed = requesterDepositPaid,
                    confirmedText = "已支付",
                    pendingText = "待支付"
                )
                ConfirmStatusChip(
                    label = if (isRequester) "对方" else "我",
                    confirmed = targetDepositPaid,
                    confirmedText = "已支付",
                    pendingText = "待支付"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (myDepositPaid) {
                Text(
                    "您已支付保证金，等待对方支付",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Button(
                    onClick = onPayDeposit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActioning
                ) {
                    Text("支付保证金")
                }
            }
        }
    }
    
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isActioning,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
    ) {
        Text("取消交换")
    }
}

@Composable
private fun InPersonTradeSection(
    myConfirmed: Boolean,
    requesterConfirmed: Boolean,
    targetConfirmed: Boolean,
    isRequester: Boolean,
    isActioning: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "交换已达成，请在完成线下交换后双方都确认",
                fontSize = 14.sp,
                color = Color(0xFFE65100)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConfirmStatusChip(
                    label = if (isRequester) "我" else "对方",
                    confirmed = requesterConfirmed
                )
                ConfirmStatusChip(
                    label = if (isRequester) "对方" else "我",
                    confirmed = targetConfirmed
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (myConfirmed) {
                Text(
                    "您已确认，等待对方确认",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50)
                )
            } else {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActioning
                ) {
                    Text("确认完成交换")
                }
            }
        }
    }
    
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isActioning,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
    ) {
        Text("取消交换")
    }
}

@Composable
private fun ShippingSection(
    myTrackingNo: String?,
    otherTrackingNo: String?,
    isActioning: Boolean,
    onShip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "保证金已支付，请发货",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1565C0)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (myTrackingNo != null) {
                Text("您已发货，物流单号: $myTrackingNo", color = Color(0xFF4CAF50))
            } else {
                Button(
                    onClick = onShip,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActioning
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("填写物流单号发货")
                }
            }
        }
    }
}

@Composable
private fun ShippingStatusSection(
    requesterTrackingNo: String?,
    targetTrackingNo: String?,
    isRequester: Boolean,
    myTrackingNo: String?,
    isActioning: Boolean,
    onShip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("物流状态", fontWeight = FontWeight.Medium, color = Color(0xFF1565C0))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 我的发货状态
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (myTrackingNo != null) Icons.Default.Check else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (myTrackingNo != null) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (myTrackingNo != null) "您已发货: $myTrackingNo" else "您尚未发货",
                    color = if (myTrackingNo != null) Color(0xFF4CAF50) else Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 对方发货状态
            val otherTrackingNo = if (isRequester) targetTrackingNo else requesterTrackingNo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (otherTrackingNo != null) Icons.Default.Check else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (otherTrackingNo != null) Color(0xFF4CAF50) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (otherTrackingNo != null) "对方已发货: $otherTrackingNo" else "对方尚未发货",
                    color = if (otherTrackingNo != null) Color(0xFF4CAF50) else Color.Gray
                )
            }
            
            if (myTrackingNo == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onShip,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActioning
                ) {
                    Text("填写物流单号发货")
                }
            }
        }
    }
}

@Composable
private fun DeliveredSection(
    myConfirmed: Boolean,
    requesterConfirmed: Boolean,
    targetConfirmed: Boolean,
    isRequester: Boolean,
    isActioning: Boolean,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "双方已发货，请确认收货",
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ConfirmStatusChip(
                    label = if (isRequester) "我" else "对方",
                    confirmed = requesterConfirmed,
                    confirmedText = "已确认收货",
                    pendingText = "待确认收货"
                )
                ConfirmStatusChip(
                    label = if (isRequester) "对方" else "我",
                    confirmed = targetConfirmed,
                    confirmedText = "已确认收货",
                    pendingText = "待确认收货"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (myConfirmed) {
                Text(
                    "您已确认收货，等待对方确认",
                    fontSize = 13.sp,
                    color = Color(0xFF4CAF50)
                )
            } else {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isActioning
                ) {
                    Text("确认收货")
                }
            }
        }
    }
}

@Composable
private fun ConfirmStatusChip(
    label: String,
    confirmed: Boolean,
    confirmedText: String = "已确认",
    pendingText: String = "待确认"
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
            text = "$label: ${if (confirmed) confirmedText else pendingText}",
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
                model = fullImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_image_placeholder),
                error = painterResource(R.drawable.ic_image_placeholder)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(title, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(ownerName, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

private fun getStatusText(status: TradeStatus?): String {
    return when (status) {
        TradeStatus.PENDING -> "等待对方确认"
        TradeStatus.ACCEPTED -> "交换已达成"
        TradeStatus.DEPOSIT_PAID -> "保证金已支付"
        TradeStatus.SHIPPING -> "运输中"
        TradeStatus.DELIVERED -> "待确认收货"
        TradeStatus.COMPLETED -> "交换已完成"
        TradeStatus.REJECTED -> "交换被拒绝"
        TradeStatus.CANCELLED -> "交换已取消"
        null -> "未知状态"
    }
}

private fun getStatusColor(status: TradeStatus?): Color {
    return when (status) {
        TradeStatus.PENDING -> Color(0xFFFF9800)
        TradeStatus.ACCEPTED, TradeStatus.DEPOSIT_PAID -> Color(0xFF2196F3)
        TradeStatus.SHIPPING, TradeStatus.DELIVERED -> Color(0xFF9C27B0)
        TradeStatus.COMPLETED -> Color(0xFF4CAF50)
        TradeStatus.REJECTED, TradeStatus.CANCELLED -> Color(0xFFF44336)
        null -> Color.Gray
    }
}
