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
import coil.compose.AsyncImage
import com.barter.app.BuildConfig
import com.barter.app.ui.screens.main.home.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit
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
                // 头像
                val avatarUrl = uiState.avatar?.let {
                    if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
                }
                AsyncImage(
                    model = avatarUrl ?: "https://via.placeholder.com/80",
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
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

                // 统计
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "物品", value = uiState.itemCount.toString())
                    StatItem(label = "交易", value = uiState.tradeCount.toString())
                    StatItem(label = "评价", value = uiState.ratingCount.toString())
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
                    headlineContent = { Text("编辑资料") },
                    leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigateToEditProfile() }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("设置") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { }
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

        // 我的物品
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "我的物品",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.myItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无物品", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.myItems) { item ->
                    ItemCard(item = item, onClick = { onNavigateToItemDetail(item.id) })
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
