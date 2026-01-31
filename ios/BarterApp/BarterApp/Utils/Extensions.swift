import Foundation

extension String {
    func formatMessageTime() -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        // 尝试多种格式
        var date: Date?
        
        // 标准 ISO8601
        date = formatter.date(from: self)
        
        // 带空格格式
        if date == nil {
            let df = DateFormatter()
            df.dateFormat = "yyyy-MM-dd HH:mm:ss"
            date = df.date(from: String(self.prefix(19)))
        }
        
        // T 分隔格式
        if date == nil {
            let df = DateFormatter()
            df.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
            date = df.date(from: String(self.prefix(19)))
        }
        
        guard let parsedDate = date else {
            return String(self.prefix(16)).replacingOccurrences(of: "T", with: " ")
        }
        
        let now = Date()
        let calendar = Calendar.current
        let outputFormatter = DateFormatter()
        
        if calendar.isDateInToday(parsedDate) {
            outputFormatter.dateFormat = "HH:mm"
            return outputFormatter.string(from: parsedDate)
        } else if calendar.isDateInYesterday(parsedDate) {
            outputFormatter.dateFormat = "HH:mm"
            return "昨天 \(outputFormatter.string(from: parsedDate))"
        } else if calendar.component(.year, from: parsedDate) == calendar.component(.year, from: now) {
            outputFormatter.dateFormat = "MM-dd HH:mm"
            return outputFormatter.string(from: parsedDate)
        } else {
            outputFormatter.dateFormat = "yyyy-MM-dd HH:mm"
            return outputFormatter.string(from: parsedDate)
        }
    }
}

extension Color {
    static let primaryGreen = Color(red: 0.3, green: 0.69, blue: 0.31)
}
