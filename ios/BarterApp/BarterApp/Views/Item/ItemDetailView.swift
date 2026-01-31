import SwiftUI

struct ItemDetailView: View {
    let itemId: Int64
    @State private var item: Item?
    @State private var isLoading = true
    @State private var error: String?
    
    var body: some View {
        Group {
            if isLoading {
                LoadingView()
            } else if let error = error {
                ErrorView(message: error) {
                    Task { await loadItem() }
                }
            } else if let item = item {
                itemContent(item)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadItem()
        }
    }
    
    private func itemContent(_ item: Item) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                // 图片轮播
                if let images = item.images, !images.isEmpty {
                    TabView {
                        ForEach(images, id: \.self) { image in
                            AsyncImage(url: URL(string: fullImageUrl(image))) { img in
                                img
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Rectangle()
                                    .fill(Color.gray.opacity(0.2))
                            }
                        }
                    }
                    .frame(height: 300)
                    .tabViewStyle(.page)
                } else {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .frame(height: 300)
                        .overlay(
                            Image(systemName: "photo")
                                .font(.largeTitle)
                                .foregroundColor(.gray)
                        )
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    // 标题
                    Text(item.title)
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    // 成色和分类
                    HStack {
                        if let condition = item.condition {
                            ConditionBadge(condition: condition)
                        }
                        if let category = item.category {
                            Text(category)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.gray.opacity(0.1))
                                .cornerRadius(4)
                        }
                    }
                    
                    // 浏览和收藏
                    HStack(spacing: 16) {
                        Label("\(item.viewCount ?? 0) 浏览", systemImage: "eye")
                        Label("\(item.wishCount ?? 0) 收藏", systemImage: "heart")
                    }
                    .font(.caption)
                    .foregroundColor(.gray)
                    
                    Divider()
                    
                    // 描述
                    if let description = item.description, !description.isEmpty {
                        Text("物品描述")
                            .font(.headline)
                        Text(description)
                            .foregroundColor(.secondary)
                    }
                    
                    Divider()
                    
                    // 发布者信息
                    NavigationLink(destination: UserProfileView(userId: item.owner.id)) {
                        HStack(spacing: 12) {
                            AvatarView(
                                avatarUrl: item.owner.avatar,
                                name: item.owner.nickname ?? item.owner.username,
                                userId: item.owner.id,
                                size: 48
                            )
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item.owner.nickname ?? item.owner.username)
                                    .fontWeight(.medium)
                                    .foregroundColor(.primary)
                                
                                if let rating = item.owner.rating {
                                    HStack(spacing: 4) {
                                        RatingStars(rating: rating, size: 12)
                                        Text(String(format: "%.1f", rating))
                                            .font(.caption)
                                            .foregroundColor(.gray)
                                    }
                                }
                            }
                            
                            Spacer()
                            
                            Image(systemName: "chevron.right")
                                .foregroundColor(.gray)
                        }
                    }
                }
                .padding()
            }
        }
        .safeAreaInset(edge: .bottom) {
            // 底部操作栏
            HStack(spacing: 12) {
                Button(action: {}) {
                    Image(systemName: "heart")
                    Text("收藏")
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(.systemGray5))
                .cornerRadius(12)
                
                NavigationLink(destination: NewChatView(userId: item.owner.id)) {
                    HStack {
                        Image(systemName: "message")
                        Text("联系卖家")
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.primaryGreen)
                    .foregroundColor(.white)
                    .cornerRadius(12)
                }
            }
            .padding()
            .background(Color(.systemBackground))
        }
    }
    
    private func loadItem() async {
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.getItemDetail(itemId: itemId)
            if response.success, let data = response.data {
                item = data
            } else {
                error = response.message ?? "加载失败"
            }
        } catch let apiError as ApiError {
            error = apiError.errorDescription
        } catch {
            self.error = "加载失败"
        }
        
        isLoading = false
    }
    
    private func fullImageUrl(_ url: String) -> String {
        if url.hasPrefix("http") {
            return url
        }
        return "\(ApiConfig.shared.baseURL)\(url)"
    }
}
