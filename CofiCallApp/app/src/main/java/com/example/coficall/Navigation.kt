package com.example.coficall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.coficall.ui.MainViewModel
import com.example.coficall.ui.components.CofiCallBottomBar
import com.example.coficall.ui.components.MainTab
import com.example.coficall.ui.components.toNavDestination
import com.example.coficall.ui.screen.*

@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDarkMode = viewModel.isDarkMode

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {}, 
            onLoginExecute = { email, password, callback ->
                viewModel.login(email, password, callback)
            },
            onRegisterExecute = { email, password, callback ->
                viewModel.register(email, password, callback)
            }
        )
        return
    }

    val collaborators by viewModel.collaborators.collectAsState()
    val businessUnits by viewModel.businessUnits.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val isMockMode by viewModel.isMockMode.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    val backStack = rememberNavBackStack(HomeDestination)

    Scaffold(
        bottomBar = {
            CofiCallBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    val dest = tab.toNavDestination()
                    // Pop back to root and navigate to new tab
                    while (backStack.size > 1) backStack.removeLastOrNull()
                    if (backStack.lastOrNull() != dest) {
                        backStack.removeLastOrNull()
                        backStack.add(dest)
                    }
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            entryProvider = entryProvider {
                entry<HomeDestination> {
                    HomeScreen(
                        isOffline = isOffline,
                        businessUnits = businessUnits,
                        onUnitClick = {}
                    )
                }
                entry<DirectoryDestination> {
                    DirectoryScreen(
                        collaborators = collaborators,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isOffline = isOffline
                    )
                }
                entry<FavoritesDestination> {
                    FavoritesScreen(
                        collaborators = collaborators,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isOffline = isOffline
                    )
                }
                entry<SettingsDestination> {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = { viewModel.toggleDarkMode(it) },
                        onLogout = { viewModel.logout() },
                        currentUserEmail = currentUserEmail,
                        isMockMode = isMockMode,
                        isOffline = isOffline
                    )
                }
            },
        )
    }
}
