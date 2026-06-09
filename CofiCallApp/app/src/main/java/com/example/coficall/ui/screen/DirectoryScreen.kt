package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

import android.net.Uri
import android.content.Intent
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.CameraAlt
import androidx.exifinterface.media.ExifInterface
import com.example.coficall.theme.ErrorRed
import com.example.coficall.ui.components.CollaboratorAvatar

@Composable
fun DirectoryScreen(
    collaborators: List<Collaborator> = sampleCollaborators,
    onFavoriteToggle: (Collaborator) -> Unit = {},
    onPhoneClick: (Collaborator) -> Unit = {},
    onMessageClick: (Collaborator) -> Unit = {},
    isOffline: Boolean = false,
    filterSite: String? = null,
    filterDepartment: String? = null,
    onClearFilters: () -> Unit = {},
    onUpdatePhoto: (String, String) -> Unit = { _, _ -> },
    onAddCollaborator: (Collaborator) -> Unit = {},
    onDeleteCollaborator: (String) -> Unit = {},
    onEditCollaborator: (Collaborator) -> Unit = {},
    currentUserEmail: String? = null,
    onRefresh: () -> Unit = {},
    getString: (String) -> String = { it },
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) } // null = "A-Z"
    
    // Estado para o diálogo de detalhes
    var selectedCollaboratorForDetail by remember { mutableStateOf<Collaborator?>(null) }
    var showAddCollaboratorDialog by remember { mutableStateOf(false) }
    var collaboratorToEdit by remember { mutableStateOf<Collaborator?>(null) }


    val filtered = remember(searchQuery, selectedLetter, filterSite, filterDepartment, collaborators) {
        var list = collaborators
        
        // Filtrar por Site (com a nova lógica de alocação)
        if (filterSite != null) {
            val filterUpper = filterSite.replace(" ", "").uppercase()
            list = list.filter { colab ->
                val bu = colab.businessUnit.replace(" ", "").uppercase()
                val dept = colab.department.replace(" ", "").uppercase()
                when (filterUpper) {
                    "COFPT" -> bu == "COFPT" && dept != "R&D+I"
                    "COFGR" -> bu == "COFGR"
                    "COEPT" -> (bu == "COFPT" && dept == "R&D+I") || bu == "COEPT"
                    "COEGR" -> bu == "COEGR"
                    else -> bu == filterUpper
                }
            }
        }
        
        // Filtrar por Departamento
        if (filterDepartment != null) {
            list = list.filter {
                it.department.replace(" ", "").uppercase() == filterDepartment.replace(" ", "").uppercase()
            }
        }

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
            DirectoryTopBar(isOffline = isOffline, getString = getString, onRefresh = onRefresh)

            // Search Bar
            SearchBarContainer(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                getString = getString,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Alphabet A-Z Quick Jump Row
            AlphabetQuickBar(
                selectedLetter = selectedLetter,
                onLetterSelected = { selectedLetter = it },
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Filtro ativo
            if (filterSite != null || filterDepartment != null) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterText = filterSite ?: filterDepartment ?: ""
                    SuggestionChip(
                        onClick = onClearFilters,
                        label = { Text("${getString("filters")}: $filterText") },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpar filtro",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

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
                                text = getString("no_collaborators"),
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
                                onClick = { selectedCollaboratorForDetail = it }
                            )
                        }
                    }
                }
            }
        }

        // FAB - Floating Action Button (Add User)
        FloatingActionButton(
            onClick = { showAddCollaboratorDialog = true },
            containerColor = fabColor,
            contentColor = fabIconColor,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .offset(y = (-70).dp)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = "Adicionar Colaborador",
                modifier = Modifier.size(24.dp)
            )
        }

        // Diálogo de detalhes do colaborador
        selectedCollaboratorForDetail?.let { collaborator ->
            val isAdmin = currentUserEmail == "flavio.proenca@coficab.com"
            CollaboratorDetailsDialog(
                collaborator = collaborator,
                onDismiss = { selectedCollaboratorForDetail = null },
                onUpdatePhoto = { newBase64 ->
                    onUpdatePhoto(collaborator.id, newBase64)
                    selectedCollaboratorForDetail = collaborator.copy(photoUrl = newBase64)
                },
                onFavoriteToggle = { onFavoriteToggle(collaborator) },
                isAdmin = isAdmin,
                onDelete = if (isAdmin) {
                    {
                        onDeleteCollaborator(collaborator.id)
                        selectedCollaboratorForDetail = null
                    }
                } else null,
                onEdit = if (isAdmin) {
                    {
                        collaboratorToEdit = collaborator
                        selectedCollaboratorForDetail = null
                    }
                } else null,
                getString = getString
            )
        }

        // Diálogo de Edição de Colaborador pelo Admin
        collaboratorToEdit?.let { collaborator ->
            EditCollaboratorDialog(
                collaborator = collaborator,
                collaborators = collaborators,
                onDismiss = { collaboratorToEdit = null },
                onSave = { updated ->
                    onEditCollaborator(updated)
                    collaboratorToEdit = null
                },
                getString = getString
            )
        }

        // Diálogo de Adicionar Colaborador
        if (showAddCollaboratorDialog) {
            AddCollaboratorDialog(

                collaborators = collaborators,
                onDismiss = { showAddCollaboratorDialog = false },
                onAddCollaborator = onAddCollaborator,
                getString = getString
            )
        }
    }
}

@Composable
fun DirectoryTopBar(isOffline: Boolean, getString: (String) -> String, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
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
                                text = getString("offline"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = offlineText
                            )
                        }
                    }
                }
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
fun SearchBarContainer(
    query: String,
    onQueryChange: (String) -> Unit,
    getString: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val containerBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    val borderStroke = if (isDark) null else BorderStroke(1.dp, LightGrayBorder)
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
                placeholder = { Text(getString("search_placeholder"), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpar texto",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
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
            )
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

@Composable
fun CollaboratorDetailsDialog(
    collaborator: Collaborator,
    onDismiss: () -> Unit,
    onUpdatePhoto: (String) -> Unit,
    onFavoriteToggle: () -> Unit,
    isAdmin: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    getString: (String) -> String,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

    // Launcher para selecionar imagem da galeria
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                onUpdatePhoto(base64)
            }
        }
    }

    // Diálogo de confirmação de eliminação
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Colaborador", fontWeight = FontWeight.Bold) },
            text = { Text("Tem a certeza que deseja eliminar ${collaborator.name}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = { onDelete?.invoke() }) {
                    Text("Eliminar", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onEdit != null) {
                        TextButton(onClick = { onEdit.invoke() }) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                            Spacer(Modifier.width(4.dp))
                            Text(getString("edit_account_title").let { if (it.contains("Edit",true)) "Editar" else it }, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (onDelete != null) {
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                            Spacer(Modifier.width(4.dp))
                            Text("Eliminar", color = ErrorRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(getString("cancel").let { if (it == "Cancelar") "Fechar" else if (it == "Cancel") "Close" else "Fermer" }, color = primaryColor, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header com avatar e botão de câmera
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    CollaboratorAvatar(
                        name = collaborator.name,
                        photoUrl = collaborator.photoUrl,
                        size = 100,
                        isOnline = collaborator.isOnline,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    // Botão para carregar foto
                    IconButton(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Carregar Foto",
                            tint = if (isDark) CoficabBlue else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Botão para remover foto
                    if (!collaborator.photoUrl.isNullOrBlank()) {
                        IconButton(
                            onClick = { onUpdatePhoto("") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ErrorRed)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomStart)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Remover Foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nome e Estrela
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = collaborator.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Cargo e Site
                Text(
                    text = collaborator.jobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = collaborator.businessUnit.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else LightGrayBorder.copy(alpha = 0.5f))
                Spacer(Modifier.height(16.dp))

                // Info Rows
                InfoRow(
                    icon = Icons.Filled.Phone,
                    label = getString("phone"),
                    value = collaborator.phone ?: "---",
                    onClick = if (!collaborator.phone.isNullOrBlank()) {
                        {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${collaborator.phone.trim()}")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    } else null
                )
                
                InfoRow(
                    icon = Icons.Filled.Dialpad,
                    label = getString("extension"),
                    value = collaborator.extension ?: "---",
                    onClick = if (!collaborator.extension.isNullOrBlank()) {
                        {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${collaborator.extension.trim()}")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    } else null
                )

                InfoRow(
                    icon = Icons.Filled.Email,
                    label = getString("email"),
                    value = collaborator.email ?: "---",
                    onClick = if (!collaborator.email.isNullOrBlank()) {
                        {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${collaborator.email.trim()}")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    } else null
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    )
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val colorAccent = if (isDark) CoficabYellow else CoficabRoyalBlue
    
    val clickableModifier = if (onClick != null && value != "---") {
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    } else {
        Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (onClick != null && value != "---") colorAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (onClick != null && value != "---") colorAccent else MaterialTheme.colorScheme.onSurface
            )
        }
        if (onClick != null && value != "---") {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun getRotationFromUri(context: Context, uri: Uri): Float {
    var rotation = 0f
    if (uri.scheme == "content") {
        val projection = arrayOf(android.provider.MediaStore.Images.ImageColumns.ORIENTATION)
        try {
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        val orientationColumnIndex = cursor.getColumnIndex(android.provider.MediaStore.Images.ImageColumns.ORIENTATION)
                        if (orientationColumnIndex != -1) {
                            rotation = cursor.getFloat(orientationColumnIndex)
                        }
                    }
                } finally {
                    cursor.close()
                }
            }
        } catch (e: Exception) {
            Log.e("Rotation", "Erro ao ler orientacao via MediaStore", e)
        }
    }
    if (rotation == 0f) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = androidx.exifinterface.media.ExifInterface(inputStream)
                val orientation = exif.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                )
                rotation = when (orientation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSVERSE -> 270f
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_TRANSPOSE -> 90f
                    else -> 0f
                }
            }
        } catch (e: Exception) {
            Log.e("Rotation", "Erro ao ler orientacao via ExifInterface", e)
        }
    }
    return rotation
}

private fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val rotationDegrees = getRotationFromUri(context, uri)

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        var bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        val maxSize = 250
        val width = bitmap.width
        val height = bitmap.height
        val (newWidth, newHeight) = if (width > height) {
            val ratio = width.toFloat() / maxSize
            Pair(maxSize, (height / ratio).toInt())
        } else {
            val ratio = height.toFloat() / maxSize
            Pair((width / ratio).toInt(), maxSize)
        }

        val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        "data:image/jpeg;base64,$base64"
    } catch (e: Exception) {
        Log.e("Utils", "Erro ao converter imagem para Base64", e)
        null
    }
}

@Composable
fun AddCollaboratorDialog(
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onAddCollaborator: (Collaborator) -> Unit,
    getString: (String) -> String,
) {
    var name by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var businessUnit by remember { mutableStateOf("COF PT") }
    var phone by remember { mutableStateOf("") }
    var extension by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var isDeptDropdownExpanded by remember { mutableStateOf(false) }
    var photoBase64 by remember { mutableStateOf<String?>(null) }

    val businessUnits = listOf("COF PT", "COF GR", "CoE PT", "CoE GR")
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val existingDepartmentsForSite = remember(businessUnit, collaborators) {
        val siteUpper = businessUnit.replace(" ", "").uppercase()
        collaborators.filter {
            it.businessUnit.replace(" ", "").uppercase() == siteUpper && it.department.isNotBlank()
        }.map { it.department }
         .distinct()
         .sorted()
    }

    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                photoBase64 = base64
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor,
        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
        focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White,
        unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getString("add_collaborator_title"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Seletor de Foto de Perfil Opcional
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        CollaboratorAvatar(
                            name = if (name.isBlank()) "Novo Colaborador" else name,
                            photoUrl = photoBase64,
                            size = 90,
                            isOnline = false,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .clickable { photoLauncher.launch("image/*") }
                        )
                        // Botão para carregar foto
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                                .border(1.5.dp, strokeColor, CircleShape)
                                .clickable { photoLauncher.launch("image/*") }
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Escolher Foto",
                                tint = if (isDark) CoficabBlue else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        // Botão para remover foto
                        if (photoBase64 != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                                    .border(1.5.dp, strokeColor, CircleShape)
                                    .clickable { photoBase64 = null }
                                    .align(Alignment.BottomStart),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Remover Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(getString("name")) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text(getString("job_title")) },
                    leadingIcon = { Icon(Icons.Filled.Work, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Dropdown para Unidade de Negócio/Site
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Unidade de Negócio / Site",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isUnitDropdownExpanded = true }
                                .border(
                                    1.dp,
                                    if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = businessUnit,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = isUnitDropdownExpanded,
                            onDismissRequest = { isUnitDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            businessUnits.forEach { bu ->
                                DropdownMenuItem(
                                    text = { Text(bu) },
                                    onClick = {
                                        businessUnit = bu
                                        isUnitDropdownExpanded = false
                                        // Resetar departamento ao mudar de site para forçar nova seleção
                                        department = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Autocomplete/Sugestão de Departamento dependente da Unidade de Negócio
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {
                            department = it
                            isDeptDropdownExpanded = true
                        },
                        label = { Text(getString("department_field")) },
                        leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = primaryColor) },
                        trailingIcon = {
                            if (existingDepartmentsForSite.isNotEmpty()) {
                                IconButton(onClick = { isDeptDropdownExpanded = !isDeptDropdownExpanded }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (existingDepartmentsForSite.isNotEmpty() && isDeptDropdownExpanded) {
                        DropdownMenu(
                            expanded = isDeptDropdownExpanded,
                            onDismissRequest = { isDeptDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            existingDepartmentsForSite.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        department = dept
                                        isDeptDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(getString("phone")) },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = extension,
                    onValueChange = { extension = it },
                    label = { Text(getString("extension")) },
                    leadingIcon = { Icon(Icons.Filled.Dialpad, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(getString("email")) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val docId = if (email.isNotBlank()) {
                            email.substringBefore("@").replace(".", "_")
                        } else {
                            name.lowercase().replace(" ", "_")
                        }
                        onAddCollaborator(
                            Collaborator(
                                id = docId,
                                name = name,
                                jobTitle = jobTitle,
                                department = department,
                                businessUnit = businessUnit,
                                phone = if (phone.isBlank()) null else phone,
                                extension = if (extension.isBlank()) null else extension,
                                email = if (email.isBlank()) null else email,
                                photoUrl = photoBase64,
                                isFavorite = false,
                                isOnline = false
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text(getString("save"), color = primaryColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun EditCollaboratorDialog(
    collaborator: Collaborator,
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onSave: (Collaborator) -> Unit,
    getString: (String) -> String,
) {
    var name by remember { mutableStateOf(collaborator.name) }
    var jobTitle by remember { mutableStateOf(collaborator.jobTitle) }
    var department by remember { mutableStateOf(collaborator.department) }
    var businessUnit by remember { mutableStateOf(collaborator.businessUnit) }
    var phone by remember { mutableStateOf(collaborator.phone ?: "") }
    var extension by remember { mutableStateOf(collaborator.extension ?: "") }
    var email by remember { mutableStateOf(collaborator.email ?: "") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    var isDeptDropdownExpanded by remember { mutableStateOf(false) }
    var photoBase64 by remember { mutableStateOf(collaborator.photoUrl) }

    val businessUnits = listOf("COF PT", "COF GR", "CoE PT", "CoE GR")
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    val existingDepartmentsForSite = remember(businessUnit, collaborators) {
        val siteUpper = businessUnit.replace(" ", "").uppercase()
        collaborators.filter {
            it.businessUnit.replace(" ", "").uppercase() == siteUpper && it.department.isNotBlank()
        }.map { it.department }
         .distinct()
         .sorted()
    }

    val context = LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                photoBase64 = base64
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor,
        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
        focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White,
        unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getString("edit_account_title") + " - " + collaborator.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Seletor de Foto de Perfil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        CollaboratorAvatar(
                            name = if (name.isBlank()) collaborator.name else name,
                            photoUrl = photoBase64,
                            size = 90,
                            isOnline = collaborator.isOnline,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .clickable { photoLauncher.launch("image/*") }
                        )
                        // Botão para carregar foto
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                                .border(1.5.dp, strokeColor, CircleShape)
                                .clickable { photoLauncher.launch("image/*") }
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Mudar Foto",
                                tint = if (isDark) CoficabBlue else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        // Botão para remover foto
                        if (photoBase64 != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                                    .border(1.5.dp, strokeColor, CircleShape)
                                    .clickable { photoBase64 = null }
                                    .align(Alignment.BottomStart),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Remover Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(getString("name")) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text(getString("job_title")) },
                    leadingIcon = { Icon(Icons.Filled.Work, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Dropdown para Unidade de Negócio/Site
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Unidade de Negócio / Site",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isUnitDropdownExpanded = true }
                                .border(
                                    1.dp,
                                    if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .background(
                                    if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = businessUnit,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = isUnitDropdownExpanded,
                            onDismissRequest = { isUnitDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            businessUnits.forEach { bu ->
                                DropdownMenuItem(
                                    text = { Text(bu) },
                                    onClick = {
                                        businessUnit = bu
                                        isUnitDropdownExpanded = false
                                        // Resetar departamento ao mudar de site para forçar nova seleção
                                        department = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Autocomplete/Sugestão de Departamento dependente da Unidade de Negócio
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {
                            department = it
                            isDeptDropdownExpanded = true
                        },
                        label = { Text(getString("department_field")) },
                        leadingIcon = { Icon(Icons.Filled.Business, contentDescription = null, tint = primaryColor) },
                        trailingIcon = {
                            if (existingDepartmentsForSite.isNotEmpty()) {
                                IconButton(onClick = { isDeptDropdownExpanded = !isDeptDropdownExpanded }) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (existingDepartmentsForSite.isNotEmpty() && isDeptDropdownExpanded) {
                        DropdownMenu(
                            expanded = isDeptDropdownExpanded,
                            onDismissRequest = { isDeptDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            existingDepartmentsForSite.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept) },
                                    onClick = {
                                        department = dept
                                        isDeptDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(getString("phone")) },
                    leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = extension,
                    onValueChange = { extension = it },
                    label = { Text(getString("extension")) },
                    leadingIcon = { Icon(Icons.Filled.Dialpad, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(getString("email")) },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            collaborator.copy(
                                name = name,
                                jobTitle = jobTitle,
                                department = department,
                                businessUnit = businessUnit,
                                phone = if (phone.isBlank()) null else phone,
                                extension = if (extension.isBlank()) null else extension,
                                email = if (email.isBlank()) null else email,
                                photoUrl = photoBase64
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text(getString("save"), color = primaryColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}


