package com.barter.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.barter.app.BuildConfig

/**
 * 通用头像组件
 * - 有头像时显示头像图片
 * - 无头像时显示首字母 + 彩色背景
 */
@Composable
fun AvatarImage(
    avatarUrl: String?,
    name: String,
    userId: Long,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: TextUnit = 20.sp,
    onClick: (() -> Unit)? = null
) {
    val fullAvatarUrl = avatarUrl?.let {
        if (it.startsWith("http")) it else BuildConfig.API_BASE_URL.trimEnd('/') + it
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    if (fullAvatarUrl != null) {
        AsyncImage(
            model = fullAvatarUrl,
            contentDescription = "$name 的头像",
            modifier = clickableModifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        // 生成默认头像
        val initial = getInitial(name)
        val backgroundColor = generateColorFromId(userId)

        Box(
            modifier = clickableModifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 获取显示名称的首字母/首字符
 */
private fun getInitial(name: String): String {
    if (name.isBlank()) return "?"
    
    val trimmed = name.trim()
    val firstChar = trimmed.first()
    
    return when {
        // 中文取第一个字
        firstChar.code in 0x4E00..0x9FFF -> firstChar.toString()
        // 英文取首字母大写
        firstChar.isLetter() -> firstChar.uppercaseChar().toString()
        // 其他情况
        else -> firstChar.toString()
    }
}

/**
 * 根据用户 ID 生成颜色
 */
private fun generateColorFromId(userId: Long): Color {
    val colors = listOf(
        Color(0xFFE57373), // 红
        Color(0xFFBA68C8), // 紫
        Color(0xFF7986CB), // 靛蓝
        Color(0xFF64B5F6), // 蓝
        Color(0xFF4FC3F7), // 浅蓝
        Color(0xFF4DB6AC), // 青
        Color(0xFF81C784), // 绿
        Color(0xFFAED581), // 浅绿
        Color(0xFFFFD54F), // 黄
        Color(0xFFFFB74D), // 橙
        Color(0xFFFF8A65), // 深橙
        Color(0xFFA1887F), // 棕
        Color(0xFF90A4AE), // 蓝灰
        Color(0xFFF06292), // 粉
        Color(0xFF9575CD), // 深紫
        Color(0xFF4DD0E1)  // 青蓝
    )
    
    val index = (userId % colors.size).toInt()
    return colors[index]
}
