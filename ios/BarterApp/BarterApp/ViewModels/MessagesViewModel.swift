import Foundation

@MainActor
class MessagesViewModel: ObservableObject {
    @Published var conversations: [Conversation] = []
    @Published var adminUser: PublicProfile?
    @Published var isLoading = false
    @Published var error: String?
    
    func loadConversations() async {
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.getConversations()
            if response.success, let data = response.data {
                conversations = data.content
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
    
    func loadAdminUser() async {
        do {
            let response = try await ApiService.shared.getAdminUser()
            if response.success, let data = response.data {
                adminUser = data
            }
        } catch {
            // 忽略错误
        }
    }
}
