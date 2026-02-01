package com.barter.app.ui.screens.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barter.app.ui.components.AvatarImage
import com.barter.app.ui.screens.main.home.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyItems: () -> Unit = {},
    onNavigateToMyTrades: () -> Unit = {},
    onNavigateToMyRatings: () -> Unit = {},
    onNavigateToMyWishes: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 个人信息卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像 - 使用 AvatarImage 组件
                AvatarImage(
                    avatarUrl = uiState.avatar,
                    name = uiState.nickname ?: uiState.username ?: "用户",
                    userId = uiState.userId ?: 0L,
                    size = 80.dp,
                    fontSize = 32.sp,
                    onClick = { onNavigateToEditProfile() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 昵称
                Text(
                    text = uiState.nickname ?: uiState.username ?: "未登录",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                // 评分
                uiState.rating?.let { rating ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 统计 - 可点击
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "物品",
                        value = uiState.itemCount.toString(),
                        onClick = onNavigateToMyItems
                    )
                    StatItem(
                        label = "交易",
                        value = uiState.tradeCount.toString(),
                        onClick = onNavigateToMyTrades
                    )
                    StatItem(
                        label = "评价",
                        value = uiState.ratingCount.toString(),
                        onClick = onNavigateToMyRatings
                    )
                }
            }
        }

        // 操作列表
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("我的钱包") },
                    leadingContent = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF4CAF50)) },
                    modifier = Modifier.clickable { onNavigateToWallet() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("我的收藏") },
                    leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable { onNavigateToMyWishes() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("编辑资料") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToEditProfile() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("设置") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToSettings() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("退出登录", color = Color.Red) },
                    leadingContent = { Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red) },
                    modifier = Modifier.clickable {
                        viewModel.logout()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
