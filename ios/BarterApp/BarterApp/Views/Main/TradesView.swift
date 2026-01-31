import SwiftUI

struct TradesView: View {
    var body: some View {
        NavigationStack {
            EmptyStateView(
                icon: "arrow.left.arrow.right",
                title: "交换请求",
                message: "暂无交换请求"
            )
            .navigationTitle("交换")
        }
    }
}
