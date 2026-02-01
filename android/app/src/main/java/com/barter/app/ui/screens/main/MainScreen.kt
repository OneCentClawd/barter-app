package com.barter.app.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barter.app.ui.navigation.MainTab
import com.barter.app.ui.screens.main.home.HomeScreen
import com.barter.app.ui.screens.main.messages.MessagesScreen
import com.barter.app.ui.screens.main.profile.ProfileScreen
import com.barter.app.ui.screens.main.trades.TradesScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToItemDetail: (Long) -> Unit,
    onNavigateToCreateItem: () -> Unit,
    onNavigateToUserProfile: (Long) -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onNavigateToNewChat: (Long) -> Unit,
    onNavigateToTradeDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMyItems: () -> Unit = {},
    onNavigateToMyTrades: () -> Unit = {},
    onNavigateToMyRatings: () -> Unit = {},
    onNavigateToMyWishes: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem(MainTab.Home.route, "首页", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem(MainTab.Trades.route, "交换", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
        BottomNavItem(MainTab.Messages.route, "消息", Icons.Filled.Chat, Icons.Outlined.Chat),
        BottomNavItem(MainTab.Profile.route, "我的", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == MainTab.Home.route) {
                FloatingActionButton(
                    onClick = onNavigateToCreateItem,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "发布物品")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(MainTab.Home.route) {
                HomeScreen(
                    onNavigateToItemDetail = onNavigateToItemDetail
                )
            }
            composable(MainTab.Trades.route) {
                TradesScreen(
                    onNavigateToTradeDetail = onNavigateToTradeDetail
                )
            }
            composable(MainTab.Messages.route) {
                MessagesScreen(
                    onNavigateToChat = onNavigateToChat,
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    onNavigateToNewChat = onNavigateToNewChat
                )
            }
            composable(MainTab.Profile.route) {
                ProfileScreen(
                    onNavigateToItemDetail = onNavigateToItemDetail,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToMyItems = onNavigateToMyItems,
                    onNavigateToMyTrades = onNavigateToMyTrades,
                    onNavigateToMyRatings = onNavigateToMyRatings,
                    onNavigateToMyWishes = onNavigateToMyWishes,
                    onNavigateToWallet = onNavigateToWallet
                )
            }
        }
    }
}
