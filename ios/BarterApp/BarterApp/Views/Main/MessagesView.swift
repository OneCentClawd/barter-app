import SwiftUI

struct MessagesView: View {
    @StateObject private var viewModel = MessagesViewModel()
    
    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.conversations.isEmpty {
                    LoadingView()
                } else {
                    conversationList
                }
            }
            .navigationTitle("消息")
            .refreshable {
                await viewModel.loadConversations()
                await viewModel.loadAdminUser()
            }
        }
        .task {
            await viewModel.loadConversations()
            await viewModel.loadAdminUser()
        }
    }
    
    private var conversationList: some View {
        List {
            // 联系客服
            if let admin = viewModel.adminUser {
                NavigationLink(destination: NewChatView(userId: admin.id)) {
                    HStack(spacing: 12) {
                        Image(systemName: "headphones")
                            .font(.title2)
                            .foregroundColor(.primaryGreen)
                            .frame(width: 48, height: 48)
                            .background(Color.primaryGreen.opacity(0.1))
                            .clipShape(Circle())
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("联系客服")
                                .fontWeight(.medium)
                            Text(admin.nickname ?? admin.username)
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            
            if viewModel.conversations.isEmpty {
                HStack {
                    Spacer()
                    Text("暂无消息")
                        .foregroundColor(.gray)
                    Spacer()
                }
                .padding(.vertical, 40)
            } else {
                ForEach(viewModel.conversations) { conversation in
                    NavigationLink(destination: ChatView(conversationId: conversation.id)) {
                        ConversationRow(conversation: conversation)
                    }
                }
            }
        }
        .listStyle(.plain)
    }
}

struct ConversationRow: View {
    let conversation: Conversation
    
    var body: some View {
        HStack(spacing: 12) {
            AvatarView(
                avatarUrl: conversation.otherUser.avatar,
                name: conversation.otherUser.nickname ?? conversation.otherUser.username,
                userId: conversation.otherUser.id,
                size: 48
            )
            
            VStack(alignment: .leading, spacing: 4) {
                Text(conversation.otherUser.nickname ?? conversation.otherUser.username)
                    .fontWeight(.medium)
                
                Text(conversation.lastMessage?.content ?? "暂无消息")
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .lineLimit(1)
            }
            
            Spacer()
            
            if let count = conversation.unreadCount, count > 0 {
                Text(count > 99 ? "99+" : "\(count)")
                    .font(.caption)
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.red)
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }
}
