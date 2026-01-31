package com.barter.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object ItemDetail : Screen("item/{itemId}") {
        fun createRoute(itemId: Long) = "item/$itemId"
    }
    object CreateItem : Screen("create_item")
    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: Long) = "edit_item/$itemId"
    }
    object UserProfile : Screen("user/{userId}") {
        fun createRoute(userId: Long) = "user/$userId"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: Long) = "chat/$conversationId"
    }
    object NewChat : Screen("new_chat/{userId}") {
        fun createRoute(userId: Long) = "new_chat/$userId"
    }
    object CreateTrade : Screen("create_trade/{targetItemId}") {
        fun createRoute(targetItemId: Long) = "create_trade/$targetItemId"
    }
    object TradeDetail : Screen("trade/{tradeId}") {
        fun createRoute(tradeId: Long) = "trade/$tradeId"
    }
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object LoginRecords : Screen("login_records")
    object AdminSettings : Screen("admin_settings")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: Long) = "user_profile/$userId"
    }
}

sealed class MainTab(val route: String, val title: String, val icon: String) {
    object Home : MainTab("home", "首页", "home")
    object Trades : MainTab("trades", "交换", "swap_horiz")
    object Messages : MainTab("messages", "消息", "chat")
    object Profile : MainTab("profile", "我的", "person")
}
