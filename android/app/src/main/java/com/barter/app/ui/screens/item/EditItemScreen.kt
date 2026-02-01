package com.barter.app.ui.screens.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.barter.app.data.model.ItemCondition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: Long,
    viewModel: EditItemViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onItemUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf(ItemCondition.GOOD) }
    var wantedItems by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }
    
    // 初始化表单数据
    LaunchedEffect(uiState.item) {
        if (!initialized && uiState.item != null) {
            title = uiState.item!!.title
            description = uiState.item!!.description ?: ""
            category = uiState.item!!.category ?: ""
            condition = uiState.item!!.condition
            wantedItems = uiState.item!!.wantedItems ?: ""
            initialized = true
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onItemUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑物品") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showConfirmDialog = true },
                        enabled = !uiState.isLoading && title.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoadingItem) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
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
    
    // 确认保存弹窗
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("确认修改") },
            text = { Text("确定要保存对「$title」的修改吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.updateItem(
                            itemId = itemId,
                            title = title,
                            description = description,
                            category = category,
                            condition = condition,
                            wantedItems = wantedItems
                        )
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
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
