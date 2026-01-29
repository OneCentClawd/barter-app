package com.barter.app.ui.screens.item

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.barter.app.data.model.ItemCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    viewModel: CreateItemViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onItemCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(ItemCondition.GOOD) }
    var wantedItems by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = (selectedImages + uris).take(5) // 最多5张图
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onItemCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布物品") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createItem(
                                title = title,
                                description = description,
                                category = category,
                                condition = condition,
                                wantedItems = wantedItems,
                                imageUris = selectedImages
                            )
                        },
                        enabled = !uiState.isLoading && title.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("发布")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 图片选择
            Text("物品图片", fontWeight = FontWeight.Medium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImages) { uri ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImages = selectedImages - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "删除",
                                tint = Color.White,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(2.dp)
                                    .size(16.dp)
                            )
                        }
                    }
                }

                if (selectedImages.size < 5) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                .clickable { imagePicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                                Text("添加图片", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("物品名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 描述
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("物品描述") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 分类
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("分类") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 成色
            Text("物品成色", fontWeight = FontWeight.Medium)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = getConditionLabel(condition),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ItemCondition.values().forEach { item ->
                        DropdownMenuItem(
                            text = { Text(getConditionLabel(item)) },
                            onClick = {
                                condition = item
                                expanded = false
                            }
                        )
                    }
                }
            }

            // 想要交换的物品
            OutlinedTextField(
                value = wantedItems,
                onValueChange = { wantedItems = it },
                label = { Text("想要交换的物品") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("描述您想要交换的物品类型...") }
            )

            // 错误信息
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun getConditionLabel(condition: ItemCondition): String {
    return when (condition) {
        ItemCondition.NEW -> "全新"
        ItemCondition.LIKE_NEW -> "几乎全新"
        ItemCondition.GOOD -> "良好"
        ItemCondition.FAIR -> "一般"
        ItemCondition.POOR -> "较旧"
    }
}
