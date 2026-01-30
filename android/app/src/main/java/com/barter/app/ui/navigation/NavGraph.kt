package com.barter.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.barter.app.ui.screens.item.CreateItemScreen
import com.barter.app.ui.screens.item.ItemDetailScreen
import com.barter.app.ui.screens.main.MainScreen
import com.barter.app.ui.screens.profile.EditProfileScreen
import com.barter.app.ui.screens.settings.SettingsScreen
import com.barter.app.ui.screens.splash.SplashScreen
import com.barter.app.ui.screens.trade.CreateTradeScreen

@Composable
fun BarterNavGraph() {
    val navController = rememberNavController()

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
                }
            )
        }
    }
}
