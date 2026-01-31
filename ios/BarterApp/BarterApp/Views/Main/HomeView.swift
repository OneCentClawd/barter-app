import SwiftUI

struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var searchText = ""
    
    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.items.isEmpty {
                    LoadingView()
                } else if let error = viewModel.error, viewModel.items.isEmpty {
                    ErrorView(message: error) {
                        Task { await viewModel.loadItems(refresh: true) }
                    }
                } else if viewModel.items.isEmpty {
                    EmptyStateView(
                        icon: "tray",
                        title: "暂无物品",
                        message: "快去发布你的第一件物品吧"
                    )
                } else {
                    itemList
                }
            }
            .navigationTitle("易物")
            .searchable(text: $searchText, prompt: "搜索物品")
            .onSubmit(of: .search) {
                viewModel.search(keyword: searchText)
            }
            .refreshable {
                await viewModel.loadItems(refresh: true)
            }
        }
        .task {
            await viewModel.loadItems(refresh: true)
        }
    }
    
    private var itemList: some View {
        ScrollView {
            LazyVGrid(columns: [
                GridItem(.flexible()),
                GridItem(.flexible())
            ], spacing: 12) {
                ForEach(viewModel.items) { item in
                    NavigationLink(destination: ItemDetailView(itemId: item.id)) {
                        ItemCard(item: item)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding()
            
            if viewModel.hasMore {
                ProgressView()
                    .onAppear {
                        Task { await viewModel.loadItems() }
                    }
            }
        }
    }
}

struct ItemCard: View {
    let item: Item
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // 图片
            if let firstImage = item.images?.first {
                AsyncImage(url: URL(string: fullImageUrl(firstImage))) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                }
                .frame(height: 120)
                .clipped()
                .cornerRadius(8)
            } else {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .frame(height: 120)
                    .cornerRadius(8)
                    .overlay(
                        Image(systemName: "photo")
                            .foregroundColor(.gray)
                    )
            }
            
            // 标题
            Text(item.title)
                .font(.subheadline)
                .fontWeight(.medium)
                .lineLimit(2)
            
            // 成色
            if let condition = item.condition {
                ConditionBadge(condition: condition)
            }
            
            // 发布者
            HStack(spacing: 4) {
                AvatarView(
                    avatarUrl: item.owner.avatar,
                    name: item.owner.nickname ?? item.owner.username,
                    userId: item.owner.id,
                    size: 20
                )
                Text(item.owner.nickname ?? item.owner.username)
                    .font(.caption)
                    .foregroundColor(.gray)
                    .lineLimit(1)
            }
        }
        .padding(8)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
    
    private func fullImageUrl(_ url: String) -> String {
        if url.hasPrefix("http") {
            return url
        }
        return "\(ApiConfig.shared.baseURL)\(url)"
    }
}
