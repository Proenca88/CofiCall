package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.model.Collaborator
import com.example.coficall.model.sampleCollaborators
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.CoficabYellow
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.theme.LightOrangeBg
import com.example.coficall.theme.LightOrangeText
import com.example.coficall.theme.NeutralMedGrey
import com.example.coficall.ui.components.CollaboratorListItem

@Composable
fun FavoritesScreen(
    collaborators: List<Collaborator> = sampleCollaborators,
    onFavoriteToggle: (Collaborator) -> Unit = {},
    onPhoneClick: (Collaborator) -> Unit = {},
    onMessageClick: (Collaborator) -> Unit = {},
    isOffline: Boolean = false,
    lastSyncTime: String = "há 5 min",
    onUpdatePhoto: (String, String) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    getString: (String) -> String = { it },
    modifier: Modifier = Modifier,
) {
    val favorites = remember(collaborators) {
        collaborators.filter { it.isFavorite }
    }

    var selectedCollaboratorForDetail by remember { mutableStateOf<Collaborator?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header com Logo e Refresh
        FavoritesHeaderBar(onRefresh = onRefresh)

        // Ecrã Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // padding above bottom menu
        ) {
            // Título Grande
            Text(
                text = getString("favorites"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Pílula de Modo Offline como card arredondado
            if (isOffline) {
                OfflineSyncIndicator(
                    lastSyncTime = lastSyncTime, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            if (favorites.isEmpty()) {
                // Empty State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(text = "⭐", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = getString("no_favorites"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = getString("add_favorites_hint"),
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralMedGrey,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(favorites, key = { it.id }) { collaborator ->
                        CollaboratorListItem(
                            collaborator = collaborator,
                            onFavoriteToggle = onFavoriteToggle,
                            onPhoneClick = onPhoneClick,
                            onMessageClick = onMessageClick,
                            onClick = { selectedCollaboratorForDetail = it },
                            showAllActions = true
                        )
                    }
                }
            }
        }

        // Diálogo de detalhes do colaborador
        selectedCollaboratorForDetail?.let { collaborator ->
            CollaboratorDetailsDialog(
                collaborator = collaborator,
                onDismiss = { selectedCollaboratorForDetail = null },
                onUpdatePhoto = { newBase64 ->
                    onUpdatePhoto(collaborator.id, newBase64)
                    selectedCollaboratorForDetail = collaborator.copy(photoUrl = newBase64)
                },
                onFavoriteToggle = { onFavoriteToggle(collaborator) },
                getString = getString
            )
        }
    }
}

@Composable
fun FavoritesHeaderBar(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val color = if (isDark) CoficabYellow else CoficabRoyalBlue
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CorporateFare,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "CofiCall",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else CoficabRoyalBlue,
                    letterSpacing = (-0.5).sp
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Sincronizar",
                    tint = if (isDark) Color.White else CoficabRoyalBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun OfflineSyncIndicator(lastSyncTime: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val containerBg = if (isDark) CoficabYellow.copy(alpha = 0.15f) else LightOrangeBg
    val contentColor = if (isDark) CoficabYellow else LightOrangeText
    val borderStroke = if (isDark) null else BorderStroke(0.5.dp, LightOrangeText.copy(alpha = 0.3f))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerBg,
        border = borderStroke,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Modo Offline • Atualizado $lastSyncTime",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    CofiCallTheme { FavoritesScreen(isOffline = true) }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenDarkPreview() {
    CofiCallTheme(darkTheme = true) { FavoritesScreen(isOffline = true) }
}
