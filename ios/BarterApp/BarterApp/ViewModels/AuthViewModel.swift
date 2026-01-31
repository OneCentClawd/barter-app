import Foundation
import SwiftUI

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var isLoggedIn: Bool
    
    private let tokenManager = TokenManager.shared
    
    init() {
        self.isLoggedIn = tokenManager.isLoggedIn
    }
    
    func login(username: String, password: String) async {
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.login(username: username, password: password)
            if response.success, let data = response.data {
                tokenManager.saveAuthData(response: data)
                isLoggedIn = true
            } else {
                error = response.message ?? "登录失败"
            }
        } catch let apiError as ApiError {
            error = apiError.errorDescription
        } catch {
            error = "登录失败: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
    
    func register(username: String, email: String, password: String, nickname: String?) async {
        isLoading = true
        error = nil
        
        do {
            let response = try await ApiService.shared.register(
                username: username,
                email: email,
                password: password,
                nickname: nickname
            )
            if response.success, let data = response.data {
                tokenManager.saveAuthData(response: data)
                isLoggedIn = true
            } else {
                error = response.message ?? "注册失败"
            }
        } catch let apiError as ApiError {
            error = apiError.errorDescription
        } catch {
            error = "注册失败: \(error.localizedDescription)"
        }
        
        isLoading = false
    }
    
    func logout() {
        tokenManager.clearAuthData()
        isLoggedIn = false
    }
}
