import Foundation

// MARK: - API Response
struct ApiResponse<T: Codable>: Codable {
    let success: Bool
    let message: String?
    let data: T?
}

struct PageResponse<T: Codable>: Codable {
    let content: [T]
    let totalPages: Int
    let totalElements: Int
    let number: Int
    let size: Int
    let first: Bool
    let last: Bool
}

// MARK: - Auth
struct LoginRequest: Codable {
    let username: String
    let password: String
}

struct RegisterRequest: Codable {
    let username: String
    let email: String
    let password: String
    let nickname: String?
}

struct AuthResponse: Codable {
    let token: String
    let userId: Int64
    let username: String
    let nickname: String?
    let avatar: String?
}

// MARK: - User
struct User: Codable, Identifiable {
    let id: Int64
    let username: String
    let nickname: String?
    let avatar: String?
    let bio: String?
    let rating: Double?
    let ratingCount: Int?
    let isAdmin: Bool?
    let itemCount: Int?
    let createdAt: String?
}

struct PublicProfile: Codable, Identifiable {
    let id: Int64
    let username: String
    let nickname: String?
    let avatar: String?
    let bio: String?
    let rating: Double?
    let ratingCount: Int?
    let isAdmin: Bool
    let itemCount: Int?
    let myRating: MyRating?
}

struct MyRating: Codable {
    let rating: Int
    let comment: String?
}

struct UserRatingResponse: Codable, Identifiable {
    let id: Int64
    let raterId: Int64
    let raterNickname: String?
    let raterAvatar: String?
    let rating: Int
    let comment: String?
    let createdAt: String
}

// MARK: - Item
struct Item: Codable, Identifiable {
    let id: Int64
    let title: String
    let description: String?
    let images: [String]?
    let category: String?
    let condition: ItemCondition?
    let status: ItemStatus?
    let owner: ItemOwner
    let createdAt: String?
    let viewCount: Int?
    let wishCount: Int?
    let isWished: Bool?
}

struct ItemOwner: Codable {
    let id: Int64
    let username: String
    let nickname: String?
    let avatar: String?
    let rating: Double?
}

enum ItemCondition: String, Codable, CaseIterable {
    case NEW = "NEW"
    case LIKE_NEW = "LIKE_NEW"
    case GOOD = "GOOD"
    case FAIR = "FAIR"
    case POOR = "POOR"
    
    var displayName: String {
        switch self {
        case .NEW: return "全新"
        case .LIKE_NEW: return "几乎全新"
        case .GOOD: return "良好"
        case .FAIR: return "一般"
        case .POOR: return "较差"
        }
    }
}

enum ItemStatus: String, Codable {
    case AVAILABLE = "AVAILABLE"
    case RESERVED = "RESERVED"
    case TRADED = "TRADED"
    case DELETED = "DELETED"
}

struct CreateItemRequest: Codable {
    let title: String
    let description: String?
    let category: String?
    let condition: String
    let images: [String]?
}

// MARK: - Conversation & Message
struct Conversation: Codable, Identifiable {
    let id: Int64
    let otherUser: ConversationUser
    let lastMessage: LastMessage?
    let unreadCount: Int?
    let updatedAt: String?
}

struct ConversationUser: Codable {
    let id: Int64
    let username: String
    let nickname: String?
    let avatar: String?
}

struct LastMessage: Codable {
    let content: String
    let createdAt: String?
}

struct ConversationDetail: Codable {
    let id: Int64
    let otherUser: ConversationUser?
    let messages: [Message]
}

struct Message: Codable, Identifiable {
    let id: Int64
    let senderId: Int64
    let senderNickname: String?
    let senderAvatar: String?
    let content: String
    let type: MessageType?
    let isRead: Bool?
    let createdAt: String?
}

enum MessageType: String, Codable {
    case TEXT = "TEXT"
    case IMAGE = "IMAGE"
    case SYSTEM = "SYSTEM"
}

struct SendMessageRequest: Codable {
    let receiverId: Int64
    let content: String
    let type: String = "TEXT"
}

struct SendMessageResponse: Codable {
    let id: Int64
    let conversationId: Int64
}

// MARK: - Trade
struct TradeRequest: Codable, Identifiable {
    let id: Int64
    let requester: TradeUser
    let targetUser: TradeUser
    let offeredItem: TradeItem
    let targetItem: TradeItem
    let status: TradeStatus
    let message: String?
    let createdAt: String?
}

struct TradeUser: Codable {
    let id: Int64
    let username: String
    let nickname: String?
    let avatar: String?
}

struct TradeItem: Codable {
    let id: Int64
    let title: String
    let images: [String]?
}

enum TradeStatus: String, Codable {
    case PENDING = "PENDING"
    case ACCEPTED = "ACCEPTED"
    case REJECTED = "REJECTED"
    case CANCELLED = "CANCELLED"
    case COMPLETED = "COMPLETED"
    
    var displayName: String {
        switch self {
        case .PENDING: return "待处理"
        case .ACCEPTED: return "已接受"
        case .REJECTED: return "已拒绝"
        case .CANCELLED: return "已取消"
        case .COMPLETED: return "已完成"
        }
    }
}
