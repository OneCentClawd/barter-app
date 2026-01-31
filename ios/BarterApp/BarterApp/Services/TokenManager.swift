import Foundation
import SwiftUI

class TokenManager: ObservableObject {
    static let shared = TokenManager()
    
    private let tokenKey = "auth_token"
    private let userIdKey = "user_id"
    private let usernameKey = "username"
    private let nicknameKey = "nickname"
    private let avatarKey = "avatar"
    
    @Published var token: String? {
        didSet {
            if let token = token {
                UserDefaults.standard.set(token, forKey: tokenKey)
            } else {
                UserDefaults.standard.removeObject(forKey: tokenKey)
            }
        }
    }
    
    @Published var userId: Int64? {
        didSet {
            if let userId = userId {
                UserDefaults.standard.set(userId, forKey: userIdKey)
            } else {
                UserDefaults.standard.removeObject(forKey: userIdKey)
            }
        }
    }
    
    @Published var username: String? {
        didSet {
            if let username = username {
                UserDefaults.standard.set(username, forKey: usernameKey)
            } else {
                UserDefaults.standard.removeObject(forKey: usernameKey)
            }
        }
    }
    
    @Published var nickname: String? {
        didSet {
            if let nickname = nickname {
                UserDefaults.standard.set(nickname, forKey: nicknameKey)
            } else {
                UserDefaults.standard.removeObject(forKey: nicknameKey)
            }
        }
    }
    
    @Published var avatar: String? {
        didSet {
            if let avatar = avatar {
                UserDefaults.standard.set(avatar, forKey: avatarKey)
            } else {
                UserDefaults.standard.removeObject(forKey: avatarKey)
            }
        }
    }
    
    var isLoggedIn: Bool {
        return token != nil
    }
    
    private init() {
        self.token = UserDefaults.standard.string(forKey: tokenKey)
        self.userId = UserDefaults.standard.object(forKey: userIdKey) as? Int64
        self.username = UserDefaults.standard.string(forKey: usernameKey)
        self.nickname = UserDefaults.standard.string(forKey: nicknameKey)
        self.avatar = UserDefaults.standard.string(forKey: avatarKey)
    }
    
    func saveAuthData(response: AuthResponse) {
        self.token = response.token
        self.userId = response.userId
        self.username = response.username
        self.nickname = response.nickname
        self.avatar = response.avatar
    }
    
    func clearAuthData() {
        self.token = nil
        self.userId = nil
        self.username = nil
        self.nickname = nil
        self.avatar = nil
    }
}
