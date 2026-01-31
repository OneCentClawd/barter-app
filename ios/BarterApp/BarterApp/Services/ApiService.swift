import Foundation

enum ApiError: Error, LocalizedError {
    case invalidURL
    case noData
    case decodingError(Error)
    case serverError(String)
    case networkError(Error)
    case unauthorized
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "无效的URL"
        case .noData:
            return "没有数据"
        case .decodingError(let error):
            return "数据解析错误: \(error.localizedDescription)"
        case .serverError(let message):
            return message
        case .networkError(let error):
            return "网络错误: \(error.localizedDescription)"
        case .unauthorized:
            return "登录已过期，请重新登录"
        }
    }
}

class ApiService {
    static let shared = ApiService()
    
    private let baseURL: String
    private let session: URLSession
    
    private init() {
        self.baseURL = ApiConfig.shared.baseURL
        self.session = URLSession.shared
    }
    
    // MARK: - Generic Request
    private func request<T: Codable>(
        endpoint: String,
        method: String = "GET",
        body: Encodable? = nil,
        requiresAuth: Bool = true
    ) async throws -> T {
        guard let url = URL(string: "\(baseURL)\(endpoint)") else {
            throw ApiError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        if requiresAuth, let token = TokenManager.shared.token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            request.httpBody = try JSONEncoder().encode(body)
        }
        
        do {
            let (data, response) = try await session.data(for: request)
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 401 {
                    throw ApiError.unauthorized
                }
            }
            
            let decoder = JSONDecoder()
            return try decoder.decode(T.self, from: data)
        } catch let error as ApiError {
            throw error
        } catch let error as DecodingError {
            throw ApiError.decodingError(error)
        } catch {
            throw ApiError.networkError(error)
        }
    }
    
    // MARK: - Auth
    func login(username: String, password: String) async throws -> ApiResponse<AuthResponse> {
        let body = LoginRequest(username: username, password: password)
        return try await request(endpoint: "/api/auth/login", method: "POST", body: body, requiresAuth: false)
    }
    
    func register(username: String, email: String, password: String, nickname: String?) async throws -> ApiResponse<AuthResponse> {
        let body = RegisterRequest(username: username, email: email, password: password, nickname: nickname)
        return try await request(endpoint: "/api/auth/register", method: "POST", body: body, requiresAuth: false)
    }
    
    // MARK: - User
    func getProfile() async throws -> ApiResponse<User> {
        return try await request(endpoint: "/api/users/me")
    }
    
    func getUserProfile(userId: Int64) async throws -> ApiResponse<PublicProfile> {
        return try await request(endpoint: "/api/users/\(userId)")
    }
    
    func getAdminUser() async throws -> ApiResponse<PublicProfile> {
        return try await request(endpoint: "/api/users/admin")
    }
    
    func getUserRatings(userId: Int64, page: Int = 0) async throws -> ApiResponse<PageResponse<UserRatingResponse>> {
        return try await request(endpoint: "/api/users/\(userId)/ratings?page=\(page)")
    }
    
    func rateUser(userId: Int64, rating: Int, comment: String?) async throws -> ApiResponse<UserRatingResponse> {
        struct RateRequest: Codable {
            let rating: Int
            let comment: String?
        }
        let body = RateRequest(rating: rating, comment: comment)
        return try await request(endpoint: "/api/users/\(userId)/rate", method: "POST", body: body)
    }
    
    // MARK: - Items
    func getItems(page: Int = 0, size: Int = 20, category: String? = nil, keyword: String? = nil) async throws -> ApiResponse<PageResponse<Item>> {
        var endpoint = "/api/items?page=\(page)&size=\(size)"
        if let category = category, !category.isEmpty {
            endpoint += "&category=\(category)"
        }
        if let keyword = keyword, !keyword.isEmpty {
            endpoint += "&keyword=\(keyword.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? keyword)"
        }
        return try await request(endpoint: endpoint)
    }
    
    func getItemDetail(itemId: Int64) async throws -> ApiResponse<Item> {
        return try await request(endpoint: "/api/items/\(itemId)")
    }
    
    func getMyItems(page: Int = 0) async throws -> ApiResponse<PageResponse<Item>> {
        return try await request(endpoint: "/api/items/my?page=\(page)")
    }
    
    func createItem(title: String, description: String?, category: String?, condition: String, images: [String]?) async throws -> ApiResponse<Item> {
        let body = CreateItemRequest(title: title, description: description, category: category, condition: condition, images: images)
        return try await request(endpoint: "/api/items", method: "POST", body: body)
    }
    
    func deleteItem(itemId: Int64) async throws -> ApiResponse<String> {
        return try await request(endpoint: "/api/items/\(itemId)", method: "DELETE")
    }
    
    // MARK: - Conversations & Messages
    func getConversations(page: Int = 0) async throws -> ApiResponse<PageResponse<Conversation>> {
        return try await request(endpoint: "/api/conversations?page=\(page)")
    }
    
    func getConversationDetail(conversationId: Int64) async throws -> ApiResponse<ConversationDetail> {
        return try await request(endpoint: "/api/conversations/\(conversationId)")
    }
    
    func sendMessage(receiverId: Int64, content: String) async throws -> ApiResponse<SendMessageResponse> {
        let body = SendMessageRequest(receiverId: receiverId, content: content)
        return try await request(endpoint: "/api/messages", method: "POST", body: body)
    }
    
    // MARK: - Trade Requests
    func getTradeRequests(page: Int = 0) async throws -> ApiResponse<PageResponse<TradeRequest>> {
        return try await request(endpoint: "/api/trade-requests?page=\(page)")
    }
    
    func getTradeRequestDetail(tradeId: Int64) async throws -> ApiResponse<TradeRequest> {
        return try await request(endpoint: "/api/trade-requests/\(tradeId)")
    }
    
    func createTradeRequest(targetItemId: Int64, offeredItemId: Int64, message: String?) async throws -> ApiResponse<TradeRequest> {
        struct CreateTradeRequest: Codable {
            let targetItemId: Int64
            let offeredItemId: Int64
            let message: String?
        }
        let body = CreateTradeRequest(targetItemId: targetItemId, offeredItemId: offeredItemId, message: message)
        return try await request(endpoint: "/api/trade-requests", method: "POST", body: body)
    }
    
    func respondTradeRequest(tradeId: Int64, accept: Bool) async throws -> ApiResponse<TradeRequest> {
        let action = accept ? "accept" : "reject"
        return try await request(endpoint: "/api/trade-requests/\(tradeId)/\(action)", method: "POST")
    }
}
