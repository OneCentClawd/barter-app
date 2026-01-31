import Foundation

class ApiConfig {
    static let shared = ApiConfig()
    
    let baseURL = "http://20.221.72.222:9527"
    
    private init() {}
}
