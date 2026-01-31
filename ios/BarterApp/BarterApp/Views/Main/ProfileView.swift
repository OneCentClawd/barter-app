import SwiftUI

struct ProfileView: View {
    @ObservedObject var authViewModel: AuthViewModel
    @State private var profile: User?
    @State private var isLoading = true
    
    var body: some View {
        NavigationStack {
            List {
                // 头像和用户名
                Section {
                    HStack(spacing: 16) {
                        AvatarView(
                            avatarUrl: profile?.avatar ?? TokenManager.shared.avatar,
                            name: profile?.nickname ?? TokenManager.shared.nickname ?? "用户",
                            userId: profile?.id ?? TokenManager.shared.userId ?? 0,
                            size: 64
                        )
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text(profile?.nickname ?? TokenManager.shared.nickname ?? "用户")
                                .font(.title2)
                                .fontWeight(.bold)
                            Text("@\(profile?.username ?? TokenManager.shared.username ?? "")")
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                        
                        Spacer()
                    }
                    .padding(.vertical, 8)
                }
                
                // 统计
                Section {
                    HStack {
                        StatItem(title: "发布物品", value: "\(profile?.itemCount ?? 0)")
                        Divider()
                        StatItem(title: "评分", value: String(format: "%.1f", profile?.rating ?? 5.0))
                        Divider()
                        StatItem(title: "评价数", value: "\(profile?.ratingCount ?? 0)")
                    }
                    .padding(.vertical, 8)
                }
                
                // 菜单
                Section {
                    NavigationLink(destination: Text("我的物品")) {
                        Label("我的物品", systemImage: "cube.box")
                    }
                    NavigationLink(destination: Text("我的收藏")) {
                        Label("我的收藏", systemImage: "heart")
                    }
                    NavigationLink(destination: Text("设置")) {
                        Label("设置", systemImage: "gearshape")
                    }
                }
                
                // 退出登录
                Section {
                    Button(role: .destructive) {
                        authViewModel.logout()
                    } label: {
                        HStack {
                            Spacer()
                            Text("退出登录")
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("我的")
            .refreshable {
                await loadProfile()
            }
        }
        .task {
            await loadProfile()
        }
    }
    
    private func loadProfile() async {
        isLoading = true
        do {
            let response = try await ApiService.shared.getProfile()
            if response.success, let data = response.data {
                profile = data
            }
        } catch {
            // 忽略错误
        }
        isLoading = false
    }
}

struct StatItem: View {
    let title: String
    let value: String
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2)
                .fontWeight(.bold)
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
        }
        .frame(maxWidth: .infinity)
    }
}
