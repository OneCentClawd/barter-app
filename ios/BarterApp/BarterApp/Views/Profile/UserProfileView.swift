import SwiftUI

struct UserProfileView: View {
    let userId: Int64
    @State private var profile: PublicProfile?
    @State private var isLoading = true
    @State private var error: String?
    
    var body: some View {
        Group {
            if isLoading {
                LoadingView()
            } else if let error = error {
                ErrorView(message: error) {
                    Task { await loadProfile() }
                }
            } else if let profile = profile {
                profileContent(profile)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadProfile()
        }
    }
    
    private func profileContent(_ profile: PublicProfile) -> some View {
        ScrollView {
            VStack(spacing: 20) {
                // 头像和基本信息
                VStack(spacing: 12) {
                    AvatarView(
                        avatarUrl: profile.avatar,
                        name: profile.nickname ?? profile.username,
                        userId: profile.id,
                        size: 100
                    )
                    
                    HStack(spacing: 8) {
                        Text(profile.nickname ?? profile.username)
                            .font(.title2)
                            .fontWeight(.bold)
                        
                        if profile.isAdmin {
                            Text("管理员")
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.primaryGreen)
                                .foregroundColor(.white)
                                .cornerRadius(4)
                        }
                    }
                    
                    // 评分
                    HStack(spacing: 4) {
                        RatingStars(rating: profile.rating ?? 5.0)
                        Text(String(format: "%.1f", profile.rating ?? 5.0))
                            .fontWeight(.medium)
                        Text("(\(profile.ratingCount ?? 0)人评价)")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                    
                    // 简介
                    if let bio = profile.bio, !bio.isEmpty {
                        Text(bio)
                            .foregroundColor(.gray)
                            .multilineTextAlignment(.center)
                    }
                    
                    // 统计
                    HStack(spacing: 40) {
                        VStack {
                            Text("\(profile.itemCount ?? 0)")
                                .font(.title2)
                                .fontWeight(.bold)
                            Text("发布物品")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.top, 8)
                    
                    // 操作按钮
                    HStack(spacing: 12) {
                        Button(action: {}) {
                            HStack {
                                Image(systemName: "star")
                                Text(profile.myRating != nil ? "修改评分" : "评分")
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color(.systemGray5))
                            .cornerRadius(12)
                        }
                        
                        NavigationLink(destination: NewChatView(userId: profile.id)) {
                            HStack {
                                Image(systemName: "envelope")
                                Text("发消息")
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.primaryGreen)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                        }
                    }
                }
                .padding()
                
                Divider()
                
                // 评价标题
                Text("用户评价")
                    .font(.headline)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal)
                
                // TODO: 评价列表
                Text("暂无评价")
                    .foregroundColor(.gray)
                    .padding()
            }
        }
    }
    
    private func loadProfile() async {
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.getUserProfile(userId: userId)
            if response.success, let data = response.data {
                profile = data
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
}
