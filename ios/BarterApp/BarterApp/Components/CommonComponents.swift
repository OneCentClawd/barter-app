import SwiftUI

// MARK: - Avatar View
struct AvatarView: View {
    let avatarUrl: String?
    let name: String
    let userId: Int64
    let size: CGFloat
    var onTap: (() -> Void)? = nil
    
    private let colors: [Color] = [
        Color(red: 0.90, green: 0.45, blue: 0.45), // 红
        Color(red: 0.73, green: 0.41, blue: 0.78), // 紫
        Color(red: 0.47, green: 0.53, blue: 0.80), // 靛蓝
        Color(red: 0.39, green: 0.71, blue: 0.96), // 蓝
        Color(red: 0.31, green: 0.76, blue: 0.97), // 浅蓝
        Color(red: 0.30, green: 0.71, blue: 0.67), // 青
        Color(red: 0.51, green: 0.78, blue: 0.52), // 绿
        Color(red: 0.68, green: 0.84, blue: 0.51), // 浅绿
        Color(red: 1.00, green: 0.84, blue: 0.31), // 黄
        Color(red: 1.00, green: 0.72, blue: 0.30), // 橙
        Color(red: 1.00, green: 0.54, blue: 0.40), // 深橙
        Color(red: 0.63, green: 0.53, blue: 0.50), // 棕
        Color(red: 0.56, green: 0.64, blue: 0.68), // 蓝灰
        Color(red: 0.94, green: 0.38, blue: 0.57), // 粉
        Color(red: 0.58, green: 0.46, blue: 0.80), // 深紫
        Color(red: 0.30, green: 0.82, blue: 0.88)  // 青蓝
    ]
    
    var body: some View {
        Group {
            if let url = avatarUrl, !url.isEmpty {
                AsyncImage(url: URL(string: fullAvatarUrl(url))) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    defaultAvatar
                }
            } else {
                defaultAvatar
            }
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
        .onTapGesture {
            onTap?()
        }
    }
    
    private var defaultAvatar: some View {
        ZStack {
            Circle()
                .fill(colorForUserId)
            Text(initial)
                .font(.system(size: size * 0.4, weight: .bold))
                .foregroundColor(.white)
        }
    }
    
    private var initial: String {
        guard !name.isEmpty else { return "?" }
        let firstChar = name.first!
        if firstChar.isLetter {
            return String(firstChar).uppercased()
        }
        return String(firstChar)
    }
    
    private var colorForUserId: Color {
        let index = Int(userId % Int64(colors.count))
        return colors[index]
    }
    
    private func fullAvatarUrl(_ url: String) -> String {
        if url.hasPrefix("http") {
            return url
        }
        return "\(ApiConfig.shared.baseURL)\(url)"
    }
}

// MARK: - Condition Badge
struct ConditionBadge: View {
    let condition: ItemCondition
    
    var body: some View {
        Text(condition.displayName)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(conditionColor.opacity(0.2))
            .foregroundColor(conditionColor)
            .cornerRadius(4)
    }
    
    private var conditionColor: Color {
        switch condition {
        case .NEW: return .green
        case .LIKE_NEW: return .blue
        case .GOOD: return .orange
        case .FAIR: return .yellow
        case .POOR: return .red
        }
    }
}

// MARK: - Loading View
struct LoadingView: View {
    var message: String = "加载中..."
    
    var body: some View {
        VStack(spacing: 12) {
            ProgressView()
            Text(message)
                .foregroundColor(.gray)
        }
    }
}

// MARK: - Empty View
struct EmptyStateView: View {
    let icon: String
    let title: String
    let message: String?
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 50))
                .foregroundColor(.gray)
            Text(title)
                .font(.headline)
            if let message = message {
                Text(message)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
            }
        }
        .padding()
    }
}

// MARK: - Error View
struct ErrorView: View {
    let message: String
    var onRetry: (() -> Void)? = nil
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 50))
                .foregroundColor(.red)
            Text(message)
                .foregroundColor(.gray)
                .multilineTextAlignment(.center)
            if let onRetry = onRetry {
                Button("重试") {
                    onRetry()
                }
                .buttonStyle(.bordered)
            }
        }
        .padding()
    }
}

// MARK: - Rating Stars
struct RatingStars: View {
    let rating: Double
    let maxRating: Int = 5
    let size: CGFloat = 16
    
    var body: some View {
        HStack(spacing: 2) {
            ForEach(0..<maxRating, id: \.self) { index in
                Image(systemName: index < Int(rating) ? "star.fill" : "star")
                    .font(.system(size: size))
                    .foregroundColor(index < Int(rating) ? .yellow : .gray.opacity(0.3))
            }
        }
    }
}
