package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.model.Collaborator
import com.example.coficall.model.sampleCollaborators
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.CoficabYellow
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.theme.LightGrayBg
import com.example.coficall.theme.LightGrayBorder
import com.example.coficall.theme.LightOrangeBg
import com.example.coficall.theme.LightOrangeText
import com.example.coficall.theme.NeutralMedGrey
import com.example.coficall.ui.components.AlphabetSectionHeader
import com.example.coficall.ui.components.CollaboratorListItem

@Composable
fun DirectoryScreen(
    collaborators: List<Collaborator> = sampleCollaborators,
    onFavoriteToggle: (Collaborator) -> Unit = {},
    onPhoneClick: (Collaborator) -> Unit = {},
    onMessageClick: (Collaborator) -> Unit = {},
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) } // null = "A-Z"

    val filtered = remember(searchQuery, selectedLetter, collaborators) {
        var list = collaborators
        
        // Filter by letter
        if (selectedLetter != null) {
            list = list.filter { it.name.startsWith(selectedLetter!!, ignoreCase = true) }
        }
        
        // Filter by search query
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.jobTitle.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
            }
        }
        
        list
    }

    val grouped = remember(filtered) {
        filtered.sortedBy { it.name }
            .groupBy { it.name.first().uppercaseChar() }
            .toSortedMap()
    }

    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val fabColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val fabIconColor = if (isDark) CoficabBlue else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            DirectoryTopBar(isOffline = isOffline)

            // Search Bar
            SearchBarContainer(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Alphabet A-Z Quick Jump Row
            AlphabetQuickBar(
                selectedLetter = selectedLetter,
                onLetterSelected = { selectedLetter = it },
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Collaborators List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
            ) {
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum colaborador encontrado.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    grouped.forEach { (letter, items) ->
                        item(key = "header_$letter") {
                            AlphabetSectionHeader(letter = letter.toString())
                        }
                        items(items, key = { it.id }) { collaborator ->
                            CollaboratorListItem(
                                collaborator = collaborator,
                                onFavoriteToggle = onFavoriteToggle,
                                onPhoneClick = onPhoneClick,
                                onMessageClick = onMessageClick,
                            )
                        }
                    }
                }
            }
        }

        // FAB - Floating Action Button (Add User)
        FloatingActionButton(
            onClick = {},
            containerColor = fabColor,
            contentColor = fabIconColor,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset(y = (-70).dp) // Offset to sit above bottom navigation bar
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = "Adicionar Colaborador",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DirectoryTopBar(isOffline: Boolean, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val color = if (isDark) CoficabYellow else CoficabRoyalBlue
    
    val offlineBg = if (isDark) CoficabYellow.copy(alpha = 0.15f) else LightOrangeBg
    val offlineText = if (isDark) CoficabYellow else LightOrangeText

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
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
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
                
                // Pílula amarela/laranja "Offline" integrada na barra superior
                if (isOffline) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = offlineBg,
                        border = if (isDark) null else BorderStroke(0.5.dp, offlineText.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = offlineText,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = offlineText
                            )
                        }
                    }
                }
            }
            
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Pesquisar",
                    tint = if (isDark) Color.White else CoficabRoyalBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SearchBarContainer(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val containerBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    val borderStroke = if (isDark) null else BorderStroke(1.dp, LightGrayBorder)
    val filterBtnBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightGrayBg
    val iconColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else NeutralMedGrey

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerBg,
        shape = RoundedCornerShape(12.dp),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Procurar Colaborador", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = if (isDark) CoficabYellow else CoficabRoyalBlue,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
            
            // Botão filtros integrado
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = filterBtnBg,
                border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else BorderStroke(1.dp, LightGrayBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AlphabetQuickBar(
    selectedLetter: Char?,
    onLetterSelected: (Char?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val alphabet = ('A'..'Z').toList()
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Opção "A-Z"
        item {
            val isAllSelected = selectedLetter == null
            
            val containerColor = if (isAllSelected) {
                if (isDark) CoficabYellow else CoficabRoyalBlue
            } else {
                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else LightGrayBg
            }
            
            val contentColor = if (isAllSelected) {
                if (isDark) CoficabBlue else Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor)
                    .clickable { onLetterSelected(null) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A-Z",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
        
        // Letras individuais
        items(alphabet) { char ->
            val isSelected = selectedLetter == char
            
            val containerColor = if (isSelected) {
                if (isDark) CoficabYellow else CoficabRoyalBlue
            } else {
                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else LightGrayBg
            }
            
            val contentColor = if (isSelected) {
                if (isDark) CoficabBlue else Color.White
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor)
                    .clickable { onLetterSelected(char) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DirectoryScreenPreview() {
    CofiCallTheme { DirectoryScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DirectoryScreenDarkPreview() {
    CofiCallTheme(darkTheme = true) { DirectoryScreen(isOffline = true) }
}
