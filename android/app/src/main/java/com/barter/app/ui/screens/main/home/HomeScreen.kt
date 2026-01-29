package com.barter.app.ui.screens.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.barter.app.data.model.ItemCondition
import com.barter.app.data.model.ItemListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToItemDetail: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("搜索物品...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadItems() }) {
                            Text("重试")
                        }
                    }
                }
            }
            uiState.items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无物品，快去发布第一个吧！")
                }
            }
            else -> {
                val filteredItems = if (searchQuery.isBlank()) {
                    uiState.items
                } else {
                    uiState.items.filter { it.title.contains(searchQuery, ignoreCase = true) }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredItems) { item ->
                        ItemCard(item = item, onClick = { onNavigateToItemDetail(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: ItemListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.LightGray)
            ) {
                if (item.coverImage != null) {
                    val imageUrl = if (item.coverImage.startsWith("http")) {
                        item.coverImage
                    } else {
                        BuildConfig.API_BASE_URL.trimEnd('/') + item.coverImage
                    }
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无图片", color = Color.Gray)
                    }
                }

                // 成色标签
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getConditionColor(item.condition).copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = getConditionText(item.condition),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // 信息
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.owner.nickname ?: item.owner.username,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

fun getConditionColor(condition: ItemCondition): Color {
    return when (condition) {
        ItemCondition.NEW -> Color(0xFF4CAF50)
        ItemCondition.LIKE_NEW -> Color(0xFF8BC34A)
        ItemCondition.GOOD -> Color(0xFFFF9800)
        ItemCondition.FAIR -> Color(0xFFFF5722)
        ItemCondition.POOR -> Color(0xFF9E9E9E)
    }
}

fun getConditionText(condition: ItemCondition): String {
    return when (condition) {
        ItemCondition.NEW -> "全新"
        ItemCondition.LIKE_NEW -> "几乎全新"
        ItemCondition.GOOD -> "良好"
        ItemCondition.FAIR -> "一般"
        ItemCondition.POOR -> "较旧"
    }
}
