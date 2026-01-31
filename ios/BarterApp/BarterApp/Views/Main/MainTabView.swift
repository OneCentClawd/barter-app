import SwiftUI

struct MainTabView: View {
    @ObservedObject var authViewModel: AuthViewModel
    
    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("首页", systemImage: "house")
                }
            
            TradesView()
                .tabItem {
                    Label("交换", systemImage: "arrow.left.arrow.right")
                }
            
            MessagesView()
                .tabItem {
                    Label("消息", systemImage: "message")
                }
            
            ProfileView(authViewModel: authViewModel)
                .tabItem {
                    Label("我的", systemImage: "person")
                }
        }
        .tint(.primaryGreen)
    }
}
