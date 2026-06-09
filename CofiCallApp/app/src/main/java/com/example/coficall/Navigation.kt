package com.example.coficall

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.coficall.ui.MainViewModel
import com.example.coficall.ui.components.CofiCallBottomBar
import com.example.coficall.ui.components.MainTab
import com.example.coficall.ui.components.toNavDestination
import com.example.coficall.ui.screen.*

@Composable
fun MainNavigation(viewModel: MainViewModel, onTriggerUpdate: (String) -> Unit) {
    val isInitializing by viewModel.isInitializing.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isDarkMode = viewModel.isDarkMode

    val updateInfo by viewModel.updateInfo.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(true) }

    if (updateInfo != null && showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!updateInfo!!.forceUpdate) {
                    showUpdateDialog = false 
                }
            },
            title = {
                Text(
                    text = "Nova Versão Disponível",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Está disponível uma nova atualização (v${updateInfo!!.latestVersionName}).\n\nDeseja descarregar e instalar agora?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTriggerUpdate(updateInfo!!.apkUrl)
                        if (!updateInfo!!.forceUpdate) {
                            showUpdateDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Atualizar",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = if (!updateInfo!!.forceUpdate) {
                {
                    TextButton(
                        onClick = { showUpdateDialog = false }
                    ) {
                        Text(text = "Mais Tarde")
                    }
                }
            } else null,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        )
    }

    if (isInitializing) {
        SplashScreen()
        return
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {}, 
            onLoginExecute = { email, password, callback ->
                viewModel.login(email, password, callback)
            },
            onRegisterExecute = { email, password, callback ->
                viewModel.register(email, password, callback)
            },
            onForgotPasswordExecute = { email, callback ->
                viewModel.resetPassword(email, callback)
            }
        )
        return
    }

    val collaborators by viewModel.collaborators.collectAsState()
    val businessUnits by viewModel.businessUnits.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val isMockMode by viewModel.isMockMode.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()

    val lang by viewModel.language.collectAsState()

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
                getString = { viewModel.getString(it) },
                language = lang
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
                    val realBusinessUnits by viewModel.businessUnitsWithCounts.collectAsState()
                    val realDepartments by viewModel.departments.collectAsState()
                    val lang by viewModel.language.collectAsState()
                    HomeScreen(
                        isOffline = isOffline,
                        businessUnits = realBusinessUnits,
                        departments = realDepartments,
                        onUnitClick = { unit ->
                            viewModel.updateFilterSite(unit.shortName)
                            currentTab = MainTab.DIRECTORY
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            if (backStack.lastOrNull() != DirectoryDestination) {
                                backStack.removeLastOrNull()
                                backStack.add(DirectoryDestination)
                            }
                        },
                        onDepartmentClick = { dept ->
                            viewModel.updateFilterDepartment(dept.name)
                            currentTab = MainTab.DIRECTORY
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            if (backStack.lastOrNull() != DirectoryDestination) {
                                backStack.removeLastOrNull()
                                backStack.add(DirectoryDestination)
                            }
                        },
                        getString = { viewModel.getString(it) },
                        onRefresh = { viewModel.refresh() }
                    )
                }
                entry<DirectoryDestination> {
                    val lang by viewModel.language.collectAsState()
                    DirectoryScreen(
                        collaborators = collaborators,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isOffline = isOffline,
                        filterSite = viewModel.filterSite,
                        filterDepartment = viewModel.filterDepartment,
                        onClearFilters = { viewModel.clearFilters() },
                        onUpdatePhoto = { colabId, base64 -> viewModel.updateCollaboratorPhoto(colabId, base64) },
                        onAddCollaborator = { viewModel.addCollaborator(it) },
                        onDeleteCollaborator = { viewModel.deleteCollaborator(it) },
                        onEditCollaborator = { viewModel.updateCollaboratorProfile(it) },
                        currentUserEmail = currentUserEmail,
                        getString = { viewModel.getString(it) },
                        onRefresh = { viewModel.refresh() }
                    )
                }
                entry<FavoritesDestination> {
                    val lang by viewModel.language.collectAsState()
                    FavoritesScreen(
                        collaborators = collaborators,
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        isOffline = isOffline,
                        onUpdatePhoto = { colabId, base64 -> viewModel.updateCollaboratorPhoto(colabId, base64) },
                        getString = { viewModel.getString(it) },
                        onRefresh = { viewModel.refresh() }
                    )
                }
                entry<SettingsDestination> {
                    val currentUserColab = remember(collaborators, currentUserEmail) {
                        collaborators.find { it.email.equals(currentUserEmail, ignoreCase = true) }
                    }
                    val lang by viewModel.language.collectAsState()
                    val updateInfoState by viewModel.updateInfo.collectAsState()
                    val serverInfoState by viewModel.serverVersionInfo.collectAsState()
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onDarkModeToggle = { viewModel.toggleDarkMode(it) },
                        onLogout = { viewModel.logout() },
                        currentUserEmail = currentUserEmail,
                        currentUserCollaborator = currentUserColab,
                        collaborators = collaborators,
                        isMockMode = isMockMode,
                        isOffline = isOffline,
                        onUpdatePhoto = { colabId, base64 -> viewModel.updateCollaboratorPhoto(colabId, base64) },
                        onUpdateProfile = { updatedColab -> viewModel.updateCollaboratorProfile(updatedColab) },
                        onRefresh = { viewModel.refresh() },
                        onRepopulateDb = { viewModel.repopulateDatabase() },
                        onApproveProfile = { viewModel.approveProfileUpdate(it) },
                        onRejectProfile = { viewModel.rejectProfileUpdate(it) },
                        onDeleteCollaborator = { viewModel.deleteCollaborator(it) },
                        onDeleteCurrentUserAccount = { callback -> viewModel.deleteCurrentUserAccount(callback) },
                        language = lang,
                        onLanguageChange = { viewModel.updateLanguage(it) },
                        getString = { viewModel.getString(it) },
                        onPromoteVersion = { code, name, callback -> viewModel.promoteCurrentVersion(code, name, callback) },
                        latestProdVersionName = serverInfoState?.latestVersionName ?: "",
                        latestProdVersionCode = serverInfoState?.latestVersionCode ?: 0,
                        latestProdApkUrl = serverInfoState?.apkUrl ?: "",
                        onUpdateApkUrl = { url, callback -> viewModel.updateApkUrl(url, callback) }
                    )
                }
            },
        )
    }
}
