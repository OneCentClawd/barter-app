package com.barter.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.barter.app.data.model.UserRatingResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToChat: (Long) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.profile?.nickname ?: uiState.profile?.username ?: "用户资料") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.profile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.profile != null) {
            val profile = uiState.profile!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // 用户头像和基本信息
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 头像
                        AsyncImage(
                            model = profile.avatar,
                            contentDescription = "头像",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 昵称
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile.nickname ?: profile.username,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (profile.isAdmin) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "管理员",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        // 评分
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < (profile.rating?.toInt() ?: 5)) 
                                        Icons.Default.Star else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < (profile.rating?.toInt() ?: 5)) 
                                        Color(0xFFFFD700) else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = String.format("%.1f", profile.rating ?: 5.0),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Text(
                                text = "(${profile.ratingCount ?: 0}人评价)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        // 简介
                        if (!profile.bio.isNullOrBlank()) {
                            Text(
                                text = profile.bio,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        // 统计
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${profile.itemCount ?: 0}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("发布物品", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showRatingDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (profile.myRating != null) "修改评分" else "评分")
                            }
                            
                            Button(
                                onClick = { onNavigateToChat(profile.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("发消息")
                            }
                        }
                        
                        // 我的评分
                        profile.myRating?.let { myRating ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("我的评分", fontWeight = FontWeight.Medium)
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = if (index < myRating.rating) 
                                                    Color(0xFFFFD700) else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (!myRating.comment.isNullOrBlank()) {
                                        Text(
                                            text = myRating.comment,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Divider()
                }
                
                // 评价列表标题
                item {
                    Text(
                        text = "用户评价 (${uiState.ratings.size})",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // 评价列表
                if (uiState.ratings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无评价", color = Color.Gray)
                        }
                    }
                } else {
                    items(uiState.ratings) { rating ->
                        RatingItem(rating)
                        Divider()
                    }
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.error!!, color = Color.Red)
            }
        }
    }

    // 评分对话框
    if (showRatingDialog) {
        RatingDialog(
            currentRating = uiState.profile?.myRating?.rating ?: 5,
            currentComment = uiState.profile?.myRating?.comment ?: "",
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                viewModel.rateUser(rating, comment)
                showRatingDialog = false
            }
        )
    }
}

@Composable
private fun RatingItem(rating: UserRatingResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = rating.raterAvatar,
            contentDescription = "头像",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = rating.raterNickname ?: "用户",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTime(rating.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Row(modifier = Modifier.padding(top = 4.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < rating.rating) Color(0xFFFFD700) else Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            if (!rating.comment.isNullOrBlank()) {
                Text(
                    text = rating.comment,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RatingDialog(
    currentRating: Int,
    currentComment: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String?) -> Unit
) {
    var rating by remember { mutableStateOf(currentRating) }
    var comment by remember { mutableStateOf(currentComment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("评价用户") },
        text = {
            Column {
                Text("评分", fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFD700) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("评价内容（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment.takeIf { it.isNotBlank() }) }) {
                Text("提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatTime(timeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timeStr.replace("T", " ").take(19), 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    } catch (e: Exception) {
        timeStr.take(10)
    }
}
