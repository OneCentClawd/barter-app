import Foundation

struct ChatMessage: Identifiable {
    let id: Int64
    let content: String
    let isMe: Bool
    let senderId: Int64
    let senderName: String
    let senderAvatar: String?
    let createdAt: String?
}

@MainActor
class ChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var otherUserName: String?
    @Published var otherUserId: Int64?
    @Published var otherUserAvatar: String?
    @Published var isLoading = false
    @Published var isSending = false
    @Published var error: String?
    
    private var conversationId: Int64?
    private let currentUserId = TokenManager.shared.userId
    
    func loadConversation(_ conversationId: Int64) async {
        self.conversationId = conversationId
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.getConversationDetail(conversationId: conversationId)
            if response.success, let data = response.data {
                messages = data.messages.map { msg in
                    ChatMessage(
                        id: msg.id,
                        content: msg.content,
                        isMe: msg.senderId == currentUserId,
                        senderId: msg.senderId,
                        senderName: msg.senderNickname ?? "用户",
                        senderAvatar: msg.senderAvatar,
                        createdAt: msg.createdAt
                    )
                }.sorted { ($0.createdAt ?? "") < ($1.createdAt ?? "") }
                
                otherUserName = data.otherUser?.nickname ?? data.otherUser?.username
                otherUserId = data.otherUser?.id
                otherUserAvatar = data.otherUser?.avatar
            } else {
                error = response.message ?? "加载失败"
            }
        } catch let apiError as ApiError {
            error = apiError.errorDescription
        } catch {
            error = "加载失败: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
    
    func sendMessage(_ content: String) async {
        guard let receiverId = otherUserId else { return }
        
        isSending = true
        
        do {
            let response = try await ApiService.shared.sendMessage(receiverId: receiverId, content: content)
            if response.success, let data = response.data {
                let newMessage = ChatMessage(
                    id: data.id,
                    content: content,
                    isMe: true,
                    senderId: currentUserId ?? 0,
                    senderName: "我",
                    senderAvatar: nil,
                    createdAt: ISO8601DateFormatter().string(from: Date())
                )
                messages.append(newMessage)
            }
        } catch {
            self.error = "发送失败"
        }
        
        isSending = false
    }
}

// 用于新聊天（通过 userId 开始）
@MainActor
class NewChatViewModel: ObservableObject {
    @Published var messages: [ChatMessage] = []
    @Published var otherUserName: String?
    @Published var otherUserId: Int64?
    @Published var otherUserAvatar: String?
    @Published var isLoading = false
    @Published var isSending = false
    @Published var error: String?
    
    private var targetUserId: Int64?
    
    func initChat(userId: Int64) async {
        targetUserId = userId
        otherUserId = userId
        isLoading = true
        
        do {
            let response = try await ApiService.shared.getUserProfile(userId: userId)
            if response.success, let data = response.data {
                otherUserName = data.nickname ?? data.username
                otherUserAvatar = data.avatar
            } else {
                otherUserName = "用户"
            }
        } catch {
            otherUserName = "用户"
        }
        
        isLoading = false
    }
    
    func sendMessage(_ content: String) async {
        guard let receiverId = targetUserId else { return }
        
        isSending = true
        
        do {
            let response = try await ApiService.shared.sendMessage(receiverId: receiverId, content: content)
            if response.success, let data = response.data {
                let newMessage = ChatMessage(
                    id: data.id,
                    content: content,
                    isMe: true,
                    senderId: TokenManager.shared.userId ?? 0,
                    senderName: "我",
                    senderAvatar: nil,
                    createdAt: ISO8601DateFormatter().string(from: Date())
                )
                messages.append(newMessage)
            }
        } catch {
            self.error = "发送失败"
        }
        
        isSending = false
    }
}
