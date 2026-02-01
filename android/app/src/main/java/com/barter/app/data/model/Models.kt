package com.barter.app.data.model

import com.google.gson.annotations.SerializedName

// 通用 API 响应
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

// 认证相关
data class LoginRequest(
    val username: String,
    val password: String
)

data class EmailLoginRequest(
    val email: String,
    val password: String
)

data class CodeLoginRequest(
    val email: String,
    val code: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val nickname: String?,
    val verificationCode: String,
    val referrerId: Long? = null
)

data class AuthResponse(
    val token: String,
    val type: String,
    val userId: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?
)

// 用户
data class User(
    val id: Long,
    val username: String,
    val email: String?,
    val nickname: String?,
    val avatar: String?,
    val phone: String?,
    val bio: String?,
    val rating: Double?,
    val ratingCount: Int?,
    val itemCount: Int?,
    val tradeCount: Int?,
    val isAdmin: Boolean = false,
    val createdAt: String?
)

// 公开用户资料
data class PublicProfile(
    val id: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val bio: String?,
    val rating: Double?,
    val ratingCount: Int?,
    val itemCount: Int?,
    val isAdmin: Boolean = false,
    val createdAt: String?,
    val myRating: UserRatingResponse?
)

// 用户评分
data class UserRatingResponse(
    val id: Long,
    val raterId: Long,
    val raterNickname: String?,
    val raterAvatar: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)

data class RateUserRequest(
    val rating: Int,
    val comment: String?
)

data class UserBrief(
    val id: Long,
    val username: String,
    val nickname: String?,
    val avatar: String?,
    val rating: Double?
)

data class UpdateProfileRequest(
    val nickname: String?,
    val phone: String?,
    val bio: String?
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class UserSettings(
    val showPhoneToOthers: Boolean = true,
    val allowStrangersMessage: Boolean = true,
    val notifyNewMessage: Boolean = true,
    val notifyTradeUpdate: Boolean = true,
    val notifySystemAnnouncement: Boolean = true
)

data class UpdateSettingsRequest(
    val showPhoneToOthers: Boolean? = null,
    val allowStrangersMessage: Boolean? = null,
    val notifyNewMessage: Boolean? = null,
    val notifyTradeUpdate: Boolean? = null,
    val notifySystemAnnouncement: Boolean? = null
)

// 登录记录
data class LoginRecord(
    val id: Long,
    val ipAddress: String?,
    val deviceType: String?,
    val userAgent: String?,
    val success: Boolean,
    val failReason: String?,
    val loginTime: String?
)

// 物品
enum class ItemCondition {
    NEW, LIKE_NEW, GOOD, FAIR, POOR
}

enum class ItemStatus {
    AVAILABLE, PENDING, TRADED, REMOVED
}

data class Item(
    val id: Long,
    val title: String,
    val description: String?,
    val category: String?,
    val condition: ItemCondition,
    val status: ItemStatus,
    val wantedItems: String?,
    val owner: UserBrief,
    val images: List<String>?,
    val viewCount: Int?,
    val wishCount: Int?,
    val isWished: Boolean?,
    val createdAt: String?,
    val tradeInfo: TradeInfo?
)

data class TradeInfo(
    val tradeRequestId: Long?,
    val previousOwner: UserBrief?,
    val tradedForItem: ItemBrief?,
    val tradedAt: String?
)

data class ItemBrief(
    val id: Long,
    val title: String,
    val coverImage: String?
)

data class WishResponse(
    val itemId: Long,
    val isWished: Boolean,
    val wishCount: Int
)

data class ItemListItem(
    val id: Long,
    val title: String,
    val category: String?,
    val condition: ItemCondition,
    val status: ItemStatus,
    val coverImage: String?,
    val owner: UserBrief,
    val createdAt: String?
)

data class CreateItemRequest(
    val title: String,
    val description: String?,
    val category: String?,
    val condition: ItemCondition = ItemCondition.GOOD,
    val wantedItems: String?
)

data class UpdateItemRequest(
    val title: String?,
    val description: String?,
    val category: String?,
    val condition: ItemCondition?,
    val wantedItems: String?,
    val status: ItemStatus?
)

// 交换请求
enum class TradeStatus {
    PENDING, ACCEPTED, DEPOSIT_PAID, SHIPPING, DELIVERED, COMPLETED, REJECTED, CANCELLED
}

data class TradeRequest(
    val id: Long,
    val targetItem: ItemListItem,
    val offeredItem: ItemListItem,
    val requester: UserBrief,
    val message: String?,
    val status: TradeStatus,
    val requesterConfirmed: Boolean = false,
    val targetConfirmed: Boolean = false,
    // 远程交易字段
    val tradeMode: TradeMode? = null,
    val estimatedValue: Double? = null,
    val requesterTrackingNo: String? = null,
    val targetTrackingNo: String? = null,
    val requesterShippedAt: String? = null,
    val targetShippedAt: String? = null,
    val requesterDepositPaid: Boolean = false,
    val targetDepositPaid: Boolean = false,
    val createdAt: String?
)

data class CreateTradeRequest(
    val targetItemId: Long,
    val offeredItemId: Long,
    val message: String?,
    val tradeMode: TradeMode? = null,
    val estimatedValue: Double? = null
)

data class UpdateTradeStatusRequest(
    val status: TradeStatus
)

data class ShipRequest(
    val trackingNo: String
)

data class DepositCalculation(
    val totalAmount: Double,
    val ratio: Double,
    val pointsNeeded: Int,
    val cashNeeded: Double,
    val canAfford: Boolean
)

// 聊天
enum class MessageType {
    TEXT, IMAGE
}

data class Message(
    val id: Long,
    val senderId: Long,
    val senderNickname: String?,
    val senderAvatar: String?,
    val content: String,
    val type: MessageType,
    val isRead: Boolean?,
    val createdAt: String?
)

data class Conversation(
    val id: Long,
    val otherUser: UserBrief,
    val lastMessage: Message?,
    val unreadCount: Int?,
    val lastMessageAt: String?
)

data class ConversationDetail(
    val id: Long,
    val otherUser: UserBrief,
    val messages: List<Message>
)

data class SendMessageRequest(
    val receiverId: Long,
    val content: String,
    val type: MessageType = MessageType.TEXT
)

// 评价
data class Review(
    val id: Long,
    val reviewer: UserBrief,
    val rating: Int,
    val comment: String?,
    val createdAt: String?
)

data class Rating(
    val id: Long,
    val raterId: Long,
    val raterNickname: String?,
    val raterAvatar: String?,
    val rating: Int,
    val comment: String?,
    val createdAt: String
) {
    // 为了 UI 使用方便，构造一个 rater 对象
    val rater: UserBrief
        get() = UserBrief(raterId, "", raterNickname, raterAvatar, null)
    val score: Int
        get() = rating
}

data class CreateReviewRequest(
    val tradeRequestId: Long,
    val rating: Int,
    val comment: String?
)

// 分页
data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)

// 管理员配置
data class SystemConfigResponse(
    val allowUserChat: Boolean,
    val allowUserViewItems: Boolean
)

data class AllowUserChatRequest(
    val allow: Boolean
)

data class AllowRequest(
    val allow: Boolean
)

// 钱包相关
data class WalletInfo(
    val points: Int,
    val balance: Double,
    val frozenPoints: Int,
    val frozenBalance: Double,
    val availablePoints: Int,
    val availableBalance: Double,
    // 签到信息
    val signedToday: Boolean = false,
    val signInStreak: Int = 0,
    val nextSignInPoints: Int = 1
)

data class WalletTransaction(
    val id: Long,
    val type: String,
    val pointsChange: Int?,
    val balanceChange: Double?,
    val description: String?,
    val createdAt: String?
)

data class CreditInfo(
    val creditScore: Int,
    val level: String,
    val levelName: String,
    val depositRatio: Double,
    val canRemoteTrade: Boolean,
    val nextLevelScore: Int?
)

data class CreditRecord(
    val id: Long,
    val type: String,
    val scoreChange: Int,
    val scoreAfter: Int,
    val description: String?,
    val createdAt: String?
)

enum class TradeMode {
    IN_PERSON,  // 面交
    REMOTE      // 远程
}
