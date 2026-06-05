package com.example.coficall

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Bottom Navigation Tabs
@Serializable data object HomeDestination : NavKey
@Serializable data object DirectoryDestination : NavKey
@Serializable data object FavoritesDestination : NavKey
@Serializable data object SettingsDestination : NavKey

// Detail Screens
@Serializable data class CollaboratorDetailDestination(val collaboratorId: String) : NavKey

// Auth
@Serializable data object LoginDestination : NavKey
