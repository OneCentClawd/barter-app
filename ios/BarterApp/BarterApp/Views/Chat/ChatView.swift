import SwiftUI

struct ChatView: View {
    let conversationId: Int64
    @StateObject private var viewModel = ChatViewModel()
    @State private var messageText = ""
    
    var body: some View {
        VStack(spacing: 0) {
            // 消息列表
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(
                                message: message,
                                onAvatarTap: {
                                    // TODO: 跳转用户资料
                                }
                            )
                            .id(message.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let lastMessage = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // 输入框
            inputBar
        }
        .navigationTitle(viewModel.otherUserName ?? "聊天")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                HStack(spacing: 8) {
                    AvatarView(
                        avatarUrl: viewModel.otherUserAvatar,
                        name: viewModel.otherUserName ?? "用户",
                        userId: viewModel.otherUserId ?? 0,
                        size: 32
                    )
                    Text(viewModel.otherUserName ?? "聊天")
                        .fontWeight(.medium)
                }
            }
        }
        .task {
            await viewModel.loadConversation(conversationId)
        }
    }
    
    private var inputBar: some View {
        HStack(spacing: 12) {
            TextField("输入消息...", text: $messageText, axis: .vertical)
                .textFieldStyle(.roundedBorder)
                .lineLimit(1...5)
            
            Button(action: sendMessage) {
                Image(systemName: "paperplane.fill")
                    .foregroundColor(messageText.isEmpty ? .gray : .primaryGreen)
            }
            .disabled(messageText.isEmpty || viewModel.isSending)
        }
        .padding()
        .background(Color(.systemBackground))
    }
    
    private func sendMessage() {
        let content = messageText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !content.isEmpty else { return }
        
        messageText = ""
        Task {
            await viewModel.sendMessage(content)
        }
    }
}

struct NewChatView: View {
    let userId: Int64
    @StateObject private var viewModel = NewChatViewModel()
    @State private var messageText = ""
    
    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.messages) { message in
                            MessageBubble(
                                message: message,
                                onAvatarTap: {}
                            )
                            .id(message.id)
                        }
                    }
                    .padding()
                }
                .onChange(of: viewModel.messages.count) { _ in
                    if let lastMessage = viewModel.messages.last {
                        withAnimation {
                            proxy.scrollTo(lastMessage.id, anchor: .bottom)
                        }
                    }
                }
            }
            
            // 输入框
            HStack(spacing: 12) {
                TextField("输入消息...", text: $messageText, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .lineLimit(1...5)
                
                Button(action: sendMessage) {
                    Image(systemName: "paperplane.fill")
                        .foregroundColor(messageText.isEmpty ? .gray : .primaryGreen)
                }
                .disabled(messageText.isEmpty || viewModel.isSending)
            }
            .padding()
            .background(Color(.systemBackground))
        }
        .navigationTitle(viewModel.otherUserName ?? "聊天")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                HStack(spacing: 8) {
                    AvatarView(
                        avatarUrl: viewModel.otherUserAvatar,
                        name: viewModel.otherUserName ?? "用户",
                        userId: viewModel.otherUserId ?? 0,
                        size: 32
                    )
                    Text(viewModel.otherUserName ?? "聊天")
                        .fontWeight(.medium)
                }
            }
        }
        .task {
            await viewModel.initChat(userId: userId)
        }
    }
    
    private func sendMessage() {
        let content = messageText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !content.isEmpty else { return }
        
        messageText = ""
        Task {
            await viewModel.sendMessage(content)
        }
    }
}

struct MessageBubble: View {
    let message: ChatMessage
    var onAvatarTap: () -> Void
    
    var body: some View {
        VStack(alignment: message.isMe ? .trailing : .leading, spacing: 4) {
            HStack(alignment: .bottom, spacing: 8) {
                if !message.isMe {
                    AvatarView(
                        avatarUrl: message.senderAvatar,
                        name: message.senderName,
                        userId: message.senderId,
                        size: 36,
                        onTap: onAvatarTap
                    )
                }
                
                Text(message.content)
                    .padding(12)
                    .background(message.isMe ? Color.primaryGreen : Color(.systemGray5))
                    .foregroundColor(message.isMe ? .white : .primary)
                    .cornerRadius(16, corners: message.isMe ? [.topLeft, .topRight, .bottomLeft] : [.topLeft, .topRight, .bottomRight])
                
                if message.isMe {
                    Spacer().frame(width: 36)
                }
            }
            .frame(maxWidth: .infinity, alignment: message.isMe ? .trailing : .leading)
            
            // 时间
            if let time = message.createdAt {
                Text(time.formatMessageTime())
                    .font(.caption2)
                    .foregroundColor(.gray)
                    .padding(.leading, message.isMe ? 0 : 44)
            }
        }
    }
}

// 圆角扩展
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}
