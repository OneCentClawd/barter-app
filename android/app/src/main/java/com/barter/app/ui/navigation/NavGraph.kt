package com.barter.app.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barter.app.ui.screens.auth.LoginScreen
import com.barter.app.ui.screens.auth.LoginViewModel
import com.barter.app.ui.screens.auth.RegisterScreen
import com.barter.app.ui.screens.auth.RegisterViewModel
import com.barter.app.ui.screens.chat.ChatScreen
import com.barter.app.ui.screens.chat.NewChatScreen
import com.barter.app.ui.screens.item.CreateItemScreen
import com.barter.app.ui.screens.item.EditItemScreen
import com.barter.app.ui.screens.item.ItemDetailScreen
import com.barter.app.ui.screens.main.MainScreen
import com.barter.app.ui.screens.profile.EditProfileScreen
import com.barter.app.ui.screens.profile.MyItemsScreen
import com.barter.app.ui.screens.profile.MyRatingsScreen
import com.barter.app.ui.screens.wallet.WalletScreen
import com.barter.app.ui.screens.profile.MyTradesScreen
import com.barter.app.ui.screens.profile.MyWishesScreen
import com.barter.app.ui.screens.profile.UserProfileScreen
import com.barter.app.ui.screens.admin.AdminSettingsScreen
import com.barter.app.ui.screens.settings.LoginRecordsScreen
import com.barter.app.ui.screens.settings.SettingsScreen
import com.barter.app.ui.screens.splash.SplashScreen
import com.barter.app.ui.screens.trade.CreateTradeScreen
import com.barter.app.ui.screens.trade.TradeDetailScreen
import com.barter.app.util.AuthEvent
import com.barter.app.util.AuthEventBus

@Composable
fun BarterNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // 监听被踢下线事件
    LaunchedEffect(Unit) {
        AuthEventBus.events.collect { event ->
            when (event) {
                is AuthEvent.TokenExpired -> {
                    Toast.makeText(context, "账号已在其他设备登录，请重新登录", Toast.LENGTH_LONG).show()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 启动页
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 登录
        composable(Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                uiState = uiState,
                onLogin = viewModel::login,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 注册
        composable(Screen.Register.route) {
            val viewModel: RegisterViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                uiState = uiState,
                onRegister = viewModel::register,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 主界面
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                },
                onNavigateToCreateItem = {
                    navController.navigate(Screen.CreateItem.route)
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId))
                },
                onNavigateToNewChat = { userId ->
                    navController.navigate(Screen.NewChat.createRoute(userId))
                },
                onNavigateToTradeDetail = { tradeId ->
                    navController.navigate(Screen.TradeDetail.createRoute(tradeId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToMyItems = {
                    navController.navigate(Screen.MyItems.route)
                },
                onNavigateToMyTrades = {
                    navController.navigate(Screen.MyTrades.route)
                },
                onNavigateToMyRatings = {
                    navController.navigate(Screen.MyRatings.route)
                },
                onNavigateToMyWishes = {
                    navController.navigate(Screen.MyWishes.route)
                },
                onNavigateToWallet = {
                    navController.navigate(Screen.Wallet.route)
                }
            )
        }

        // 物品详情
        composable(
            route = Screen.ItemDetail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateTrade = { targetItemId ->
                    navController.navigate(Screen.CreateTrade.createRoute(targetItemId))
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Screen.NewChat.createRoute(userId))
                },
                onNavigateToEditItem = { id ->
                    navController.navigate(Screen.EditItem.createRoute(id))
                }
            )
        }

        // 发布物品
        composable(Screen.CreateItem.route) {
            CreateItemScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemCreated = { navController.popBackStack() }
            )
        }
        
        // 编辑物品
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: return@composable
            EditItemScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onItemUpdated = { navController.popBackStack() }
            )
        }

        // 发起交换
        composable(
            route = Screen.CreateTrade.route,
            arguments = listOf(navArgument("targetItemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val targetItemId = backStackEntry.arguments?.getLong("targetItemId") ?: return@composable
            CreateTradeScreen(
                targetItemId = targetItemId,
                onNavigateBack = { navController.popBackStack() },
                onTradeCreated = {
                    navController.popBackStack()
                    navController.popBackStack()
                }
            )
        }

        // 编辑资料
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 设置
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onNavigateToLoginRecords = {
                    navController.navigate(Screen.LoginRecords.route)
                },
                onNavigateToAdminSettings = {
                    navController.navigate(Screen.AdminSettings.route)
                }
            )
        }

        // 登录记录
        composable(Screen.LoginRecords.route) {
            LoginRecordsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 管理员设置
        composable(Screen.AdminSettings.route) {
            AdminSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 聊天（已有会话）
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: return@composable
            ChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfile.createRoute(userId))
                }
            )
        }

        // 新聊天（通过用户ID开始）
        composable(
            route = Screen.NewChat.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            NewChatScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { uid ->
                    navController.navigate(Screen.UserProfile.createRoute(uid))
                }
            )
        }

        // 交换详情
        composable(
            route = Screen.TradeDetail.route,
            arguments = listOf(navArgument("tradeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tradeId = backStackEntry.arguments?.getLong("tradeId") ?: return@composable
            TradeDetailScreen(
                tradeId = tradeId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }

        // 用户资料
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            UserProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { uid ->
                    navController.navigate(Screen.NewChat.createRoute(uid))
                }
            )
        }
        
        // 我的物品
        composable(Screen.MyItems.route) {
            MyItemsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }
        
        // 我的交易
        composable(Screen.MyTrades.route) {
            MyTradesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTradeDetail = { tradeId ->
                    navController.navigate(Screen.TradeDetail.createRoute(tradeId))
                }
            )
        }
        
        // 我的评价
        composable(Screen.MyRatings.route) {
            MyRatingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // 我的收藏
        composable(Screen.MyWishes.route) {
            MyWishesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItemDetail = { itemId ->
                    navController.navigate(Screen.ItemDetail.createRoute(itemId))
                }
            )
        }
        
        // 钱包
        composable(Screen.Wallet.route) {
            WalletScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
