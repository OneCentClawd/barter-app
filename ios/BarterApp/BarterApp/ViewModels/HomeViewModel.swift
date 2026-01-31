import Foundation

@MainActor
class HomeViewModel: ObservableObject {
    @Published var items: [Item] = []
    @Published var isLoading = false
    @Published var error: String?
    @Published var hasMore = true
    
    private var currentPage = 0
    private var keyword: String?
    private var category: String?
    
    func loadItems(refresh: Bool = false) async {
        if refresh {
            currentPage = 0
            hasMore = true
        }
        
        guard hasMore, !isLoading else { return }
        
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.getItems(
                page: currentPage,
                category: category,
                keyword: keyword
            )
            if response.success, let data = response.data {
                if refresh {
                    items = data.content
                } else {
                    items.append(contentsOf: data.content)
                }
                hasMore = !data.last
                currentPage += 1
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
    
    func search(keyword: String?) {
        self.keyword = keyword
        Task {
            await loadItems(refresh: true)
        }
    }
    
    func filterByCategory(_ category: String?) {
        self.category = category
        Task {
            await loadItems(refresh: true)
        }
    }
}
