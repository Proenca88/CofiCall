package com.example.coficall.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.DirectoryDestination
import com.example.coficall.FavoritesDestination
import com.example.coficall.HomeDestination
import com.example.coficall.SettingsDestination

sealed class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    data object Home : BottomNavItem("Início", Icons.Filled.Home, Icons.Outlined.Home)
    data object Directory : BottomNavItem("Colaboradores", Icons.Filled.People, Icons.Outlined.People)
    data object Favorites : BottomNavItem("Favoritos", Icons.Filled.Star, Icons.Outlined.StarBorder)
    data object Settings : BottomNavItem("Definições", Icons.Filled.Settings, Icons.Outlined.Settings)
}

enum class MainTab { HOME, DIRECTORY, FAVORITES, SETTINGS }

fun MainTab.toNavDestination() = when (this) {
    MainTab.HOME -> HomeDestination
    MainTab.DIRECTORY -> DirectoryDestination
    MainTab.FAVORITES -> FavoritesDestination
    MainTab.SETTINGS -> SettingsDestination
}

@Composable
fun CofiCallBottomBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    getString: (String) -> String,
    language: String,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        MainTab.HOME to BottomNavItem.Home,
        MainTab.DIRECTORY to BottomNavItem.Directory,
        MainTab.FAVORITES to BottomNavItem.Favorites,
        MainTab.SETTINGS to BottomNavItem.Settings,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (tab, item) ->
                val isSelected = currentTab == tab
                val translatedLabel = when (item) {
                    BottomNavItem.Home -> getString("home")
                    BottomNavItem.Directory -> getString("collaborators")
                    BottomNavItem.Favorites -> getString("favorites")
                    BottomNavItem.Settings -> getString("settings")
                }
                BottomBarItem(
                    label = translatedLabel,
                    icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                    isSelected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = selectedColor,
                    modifier = Modifier.size(22.dp),
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = unselectedColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) selectedColor else unselectedColor,
            fontSize = 10.sp,
        )
    }
}
