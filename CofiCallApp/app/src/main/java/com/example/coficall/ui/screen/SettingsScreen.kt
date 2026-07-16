package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.CoficabYellow
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.theme.ErrorRed
import com.example.coficall.theme.LightBluePillBg
import com.example.coficall.theme.LightGrayBg
import com.example.coficall.theme.LightGrayBorder
import com.example.coficall.theme.NeutralMedGrey
import com.example.coficall.theme.OnlineGreen
import com.example.coficall.theme.NeutralCharcoal
import com.example.coficall.ui.components.CollaboratorAvatar
import com.example.coficall.model.Collaborator
import android.net.Uri
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    onDarkModeToggle: (Boolean) -> Unit = {},
    isDarkMode: Boolean = false,
    currentUserEmail: String? = null,
    currentUserCollaborator: Collaborator? = null,
    collaborators: List<Collaborator> = emptyList(),
    isMockMode: Boolean = false,
    isOffline: Boolean = false,
    onUpdatePhoto: (String, String) -> Unit = { _, _ -> },
    onUpdateProfile: (Collaborator) -> Unit = {},
    onRefresh: () -> Unit = {},
    onRefreshWithResult: (onResult: (Result<Unit>) -> Unit) -> Unit = { _ -> },
    onRepopulateDb: () -> Unit = {},
    onApproveProfile: (String) -> Unit = {},
    onRejectProfile: (String) -> Unit = {},
    onDeleteCollaborator: (String) -> Unit = {},
    onDeleteCurrentUserAccount: (onResult: (Result<Unit>) -> Unit) -> Unit = {},
    language: String = "PT",
    onLanguageChange: (String) -> Unit = {},
    getString: (String) -> String = { it },
    onPromoteVersion: (Int, String, (Result<Unit>) -> Unit) -> Unit = { _, _, _ -> },
    latestProdVersionName: String = "",
    latestProdVersionCode: Int = 0,
    latestProdApkUrl: String = "",
    onUpdateApkUrl: (String, (Result<Unit>) -> Unit) -> Unit = { _, _ -> },
    isSyncingContacts: Boolean = false,
    hasContactPermission: Boolean = false,
    onSyncContacts: (onResult: (Result<Unit>) -> Unit) -> Unit = { _ -> },
    onUpdateContactPermission: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showUpdateUrlDialog by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(isDarkMode) }
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[android.Manifest.permission.READ_CONTACTS] == true
        val writeGranted = permissions[android.Manifest.permission.WRITE_CONTACTS] == true
        val isGranted = readGranted && writeGranted
        onUpdateContactPermission(isGranted)
        if (isGranted) {
            onSyncContacts { result ->
                if (result.isSuccess) {
                    android.widget.Toast.makeText(context, "Contactos sincronizados com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Erro ao sincronizar contactos.", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } else {
            android.widget.Toast.makeText(context, "Permissão de contactos recusada.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    var showManageCollaboratorsDialog by remember { mutableStateOf(false) }
    var showRepopulateDbConfirmDialog by remember { mutableStateOf(false) }
    var showPromoteVersionConfirmDialog by remember { mutableStateOf(false) }
    var adminSelectedCollaboratorForEdit by remember { mutableStateOf<Collaborator?>(null) }

    val appVersionName = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    val appVersionCode = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null && currentUserCollaborator != null) {
                onUpdatePhoto(currentUserCollaborator.id, base64)
            }
        }
    }

    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        SettingsHeaderBar(onRefresh = onRefresh)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            // Profile Card (Reativo com upload de fotos!)
            item {
                val derivedName = currentUserCollaborator?.name ?: currentUserEmail?.substringBefore("@")
                    ?.replace(".", " ")
                    ?.split(" ")
                    ?.joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } } 
                    ?: "Flavio Proenca"

                val jobTitle = currentUserCollaborator?.jobTitle ?: "Diretor de Operações"
                val company = currentUserCollaborator?.businessUnit ?: "LOGÍSTICA GLOBAL S.A."
                val photoUrl = currentUserCollaborator?.photoUrl

                ProfileCard(
                    name = derivedName,
                    jobTitle = jobTitle,
                    company = company.uppercase(),
                    photoUrl = photoUrl,
                    onEditPhotoClick = { launcher.launch("image/*") },
                    onEditProfileClick = { showEditProfileDialog = true },
                    onRemovePhotoClick = { currentUserCollaborator?.id?.let { onUpdatePhoto(it, "") } }
                )
            }

            // Sync Status Card (Base de Dados Local com Nuvem)
            item {
                SyncStatusCard(
                    isMockMode = isMockMode, 
                    isOffline = isOffline,
                    title = if (isMockMode) getString("local_db") else if (isOffline) getString("local_db") else getString("cloud_db"),
                    subtitle = if (isMockMode) getString("last_sync") else if (isOffline) getString("last_sync") else getString("synced"),
                    statusText = if (isMockMode) getString("updated") else if (isOffline) getString("offline") else getString("updated"),
                    onClick = if (isOffline || isMockMode) {
                        {
                            scope.launch {
                                onRefreshWithResult { result ->
                                    result.fold(
                                        onSuccess = {
                                            android.widget.Toast.makeText(context, "Sincronização com o servidor concluída!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { error ->
                                            android.widget.Toast.makeText(context, "Falha de ligação: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                        }
                    } else null
                )
            }

            // Painel de Administração (Exclusivo para flavio.proenca@coficab.com)
            if (currentUserEmail == "flavio.proenca@coficab.com") {
                item {
                    AdminPanelSection(
                        collaborators = collaborators,
                        onRepopulateClick = { showRepopulateDbConfirmDialog = true },
                        onManageClick = { showManageCollaboratorsDialog = true },
                        onApproveProfile = onApproveProfile,
                        onRejectProfile = onRejectProfile,
                        getString = getString,
                        language = language,
                        onPromoteVersionClick = { showPromoteVersionConfirmDialog = true },
                        appVersionName = appVersionName,
                        prodVersionName = latestProdVersionName,
                        currentApkUrl = latestProdApkUrl,
                        onUpdateApkUrl = onUpdateApkUrl
                    )
                }
            }

            // Idioma Section (Estilo Card Dropdown)
            item {
                Text(
                    text = getString("language"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else LightGrayBg),
                        border = if (isDark) null else BorderStroke(1.dp, LightGrayBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDropdownExpanded = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val activeLangText = when (language) {
                                "PT" -> getString("pt")
                                "EN" -> getString("en")
                                "FR" -> getString("fr")
                                "ES" -> getString("es")
                                else -> getString("pt")
                            }
                            Text(
                                text = activeLangText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
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
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text(getString("pt")) },
                            onClick = {
                                onLanguageChange("PT")
                                isDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(getString("en")) },
                            onClick = {
                                onLanguageChange("EN")
                                isDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(getString("fr")) },
                            onClick = {
                                onLanguageChange("FR")
                                isDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(getString("es")) },
                            onClick = {
                                onLanguageChange("ES")
                                isDropdownExpanded = false
                            }
                        )

                    }
                }
            }

            // Menu Items individuais
            item {
                Spacer(Modifier.height(16.dp))
                
                val blueIconContainer = if (isDark) MaterialTheme.colorScheme.secondaryContainer else LightBluePillBg
                val blueIconTint = if (isDark) MaterialTheme.colorScheme.primary else CoficabRoyalBlue
                
                val greyIconContainer = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightGrayBg
                val greyIconTint = if (isDark) Color.White else NeutralCharcoal

                // 1. Minha Conta
                SettingsMenuItem(
                    title = getString("my_account"),
                    icon = Icons.Filled.Person,
                    iconContainerColor = blueIconContainer,
                    iconColor = blueIconTint,
                    onClick = { showEditProfileDialog = true }
                )
                
                // 2. Sincronização Offline
                SettingsMenuItem(
                    title = getString("offline_sync"),
                    icon = Icons.Filled.CloudSync,
                    iconContainerColor = blueIconContainer,
                    iconColor = blueIconTint,
                    onClick = { showSyncDialog = true }
                )
                
                // 3. Identificação de Chamadas (Sincronização Nativa)
                SettingsMenuItem(
                    title = "Identificação de Chamadas",
                    icon = Icons.Filled.Phone,
                    iconContainerColor = if (hasContactPermission) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    iconColor = if (hasContactPermission) Color(0xFF4CAF50) else Color(0xFFF44336),
                    onClick = {
                        if (hasContactPermission) {
                            onSyncContacts { result ->
                                if (result.isSuccess) {
                                    android.widget.Toast.makeText(context, "Lista de contactos atualizada com sucesso!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Erro ao atualizar contactos.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.WRITE_CONTACTS
                                )
                            )
                        }
                    },
                    trailingContent = {
                        if (isSyncingContacts) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = if (isDark) CoficabYellow else CoficabRoyalBlue
                            )
                        } else {
                            Text(
                                text = if (hasContactPermission) "Ativo" else "Configurar",
                                color = if (hasContactPermission) Color(0xFF4CAF50) else Color(0xFFF44336),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
                
                // 4. Modo Escuro
                SettingsMenuItem(
                    title = getString("dark_mode"),
                    icon = Icons.Filled.NightsStay,
                    iconContainerColor = greyIconContainer,
                    iconColor = greyIconTint,
                    onClick = {},
                    trailingContent = {
                        Switch(
                            checked = darkMode,
                            onCheckedChange = {
                                darkMode = it
                                onDarkModeToggle(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDark) CoficabYellow else CoficabRoyalBlue
                            )
                        )
                    }
                )
                
                // 4. Sobre
                SettingsMenuItem(
                    title = getString("about"),
                    icon = Icons.Filled.Info,
                    iconContainerColor = greyIconContainer,
                    iconColor = greyIconTint,
                    onClick = { showAboutDialog = true }
                )
            }

            // Botão Sair da Conta (Outlined em vermelho com fundo branco)
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.2.dp, ErrorRed),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed,
                        containerColor = if (isDark) Color.Transparent else Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ErrorRed
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = getString("logout"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ErrorRed
                    )
                }
            }

            // Versão text
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "v$appVersionName (Build $appVersionCode)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedGrey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            }
        }
    }

    // Diálogo "Editar Conta"
    if (showEditProfileDialog && currentUserCollaborator != null) {
        var editName by remember { mutableStateOf(currentUserCollaborator.name) }
        var editJobTitle by remember { mutableStateOf(currentUserCollaborator.jobTitle) }
        var editDept by remember { mutableStateOf(currentUserCollaborator.department) }
        var editSite by remember { mutableStateOf(currentUserCollaborator.businessUnit) }
        var editPhone by remember { mutableStateOf(currentUserCollaborator.phone ?: "") }
        var editExtension by remember { mutableStateOf(currentUserCollaborator.extension ?: "") }
        var editEmail by remember { mutableStateOf(currentUserCollaborator.email ?: "") }
        var editPhotoBase64 by remember { mutableStateOf(currentUserCollaborator.photoUrl) }

        var isUnitDropdownExpanded by remember { mutableStateOf(false) }
        var isDeptDropdownExpanded by remember { mutableStateOf(false) }

        val businessUnits = listOf("COF PT", "COF GR", "CoE PT", "CoE GR")
        val existingDepartmentsForSite = remember(editSite, collaborators) {
            val siteUpper = editSite.replace(" ", "").uppercase()
            collaborators.filter {
                it.businessUnit.replace(" ", "").uppercase() == siteUpper && it.department.isNotBlank()
            }.map { it.department }
             .distinct()
             .sorted()
        }

        val isAdmin = currentUserEmail == "flavio.proenca@coficab.com"
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
            focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White,
            unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )

        val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        val profilePhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val base64 = uriToBase64(context, uri)
                if (base64 != null) {
                    editPhotoBase64 = base64
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Column {
                    Text(getString("edit_account_title"), fontWeight = FontWeight.Bold)
                    if (!isAdmin) {
                        Text(
                            text = "As suas alterações aguardarão aprovação do Administrador.",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
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
                                name = if (editName.isBlank()) currentUserCollaborator.name else editName,
                                photoUrl = editPhotoBase64,
                                size = 90,
                                isOnline = true,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .clickable { profilePhotoLauncher.launch("image/*") }
                            )
                            // Botão para carregar foto
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                                    .border(1.5.dp, strokeColor, CircleShape)
                                    .clickable { profilePhotoLauncher.launch("image/*") }
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
                            if (editPhotoBase64 != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                        .border(1.5.dp, strokeColor, CircleShape)
                                        .clickable { editPhotoBase64 = null }
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
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(getString("name")) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editJobTitle,
                        onValueChange = { editJobTitle = it },
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
                                        text = editSite,
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
                                            editSite = bu
                                            isUnitDropdownExpanded = false
                                            editDept = "" // Resetar departamento ao mudar de site
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Autocomplete/Sugestão de Departamento dependente da Unidade de Negócio
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editDept,
                            onValueChange = {
                                editDept = it
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
                                            editDept = dept
                                            isDeptDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(getString("phone")) },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editExtension,
                        onValueChange = { editExtension = it },
                        label = { Text(getString("extension")) },
                        leadingIcon = { Icon(Icons.Filled.Dialpad, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(getString("email")) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDeleteCurrentUserAccount { res ->
                                if (res.isSuccess) {
                                    showEditProfileDialog = false
                                }
                            }
                        }
                    ) {
                        Text(if (language == "PT") "Eliminar Conta" else "Delete Account", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = {
                            onUpdateProfile(
                                currentUserCollaborator.copy(
                                    name = editName,
                                    jobTitle = editJobTitle,
                                    department = editDept,
                                    businessUnit = editSite,
                                    phone = if (editPhone.isBlank()) null else editPhone,
                                    extension = if (editExtension.isBlank()) null else editExtension,
                                    email = if (editEmail.isBlank()) null else editEmail,
                                    photoUrl = editPhotoBase64
                                )
                            )
                            showEditProfileDialog = false
                        }
                    ) {
                        Text(getString("save"), color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            },

            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo "Sobre" (Popup elegante com info Flávio Proença)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {},
            dismissButton = {},
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Botão Fechar no canto superior direito
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { showAboutDialog = false },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Fechar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Círculo Azul Grande com Ícone de Código
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0B46A3)), // Azul Coficab Royal
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Code,
                            contentDescription = "Código",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Títulos
                    Text(
                        text = "Sobre a Aplicação",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        text = "Desenvolvido por Flávio Proença",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(20.dp))

                    // Dois botões circulares flutuantes (Chamada e E-mail)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botão de Chamada (Azul Royal)
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:910352747"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("About", "Erro ao ligar", e)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B46A3))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Ligar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Botão de E-mail (Azul/Cinza Claro)
                        val emailBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFEBF5FF)
                        val emailIconColor = if (isDark) Color.White else Color(0xFF0B46A3)
                        IconButton(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:flavio.proenca@coficab.com"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("About", "Erro ao enviar e-mail", e)
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(emailBgColor)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = "Enviar Email",
                                tint = emailIconColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Seção CONTACTO DO DESENVOLVEDOR
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "CONTACTO DO DESENVOLVEDOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Card do Telemóvel
                    val cardBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.White
                    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                    
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:910352747"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("About", "Erro ao ligar", e)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quadrado com ícone de telemóvel
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFEBF5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Smartphone,
                                    contentDescription = null,
                                    tint = if (isDark) CoficabYellow else Color(0xFF0B46A3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Telemóvel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "910 352 747",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Card do Email
                    Card(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:flavio.proenca@coficab.com"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("About", "Erro ao enviar email", e)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(1.dp, cardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quadrado com ícone de e-mail
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFEBF5FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = if (isDark) CoficabYellow else Color(0xFF0B46A3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email Corporativo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "flavio.proenca@coficab.com",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Direitos Autorais no fundo
                    Text(
                        text = "© 2026 CofiCall",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        )
    }

    // Diálogo "Sincronização Offline"
    if (showSyncDialog) {
        var isSyncing by remember { mutableStateOf(false) }
        var syncDone by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSyncing) showSyncDialog = false },
            title = { Text(getString("sync_dialog_title"), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = primaryColor)
                        Text(getString("syncing"), style = MaterialTheme.typography.bodyMedium)
                    } else if (syncDone) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = OnlineGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(getString("sync_success"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (isOffline) getString("local_db") else getString("cloud_db"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = getString("last_sync"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (!isSyncing && !syncDone) {
                    TextButton(
                        onClick = {
                            isSyncing = true
                            scope.launch {
                                delay(1500)
                                onRefresh()
                                isSyncing = false
                                syncDone = true
                            }
                        }
                    ) {
                        Text(getString("sync_now"), color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                } else if (syncDone) {
                    TextButton(
                        onClick = {
                            showSyncDialog = false
                            syncDone = false
                        }
                    ) {
                        Text("OK", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!isSyncing && !syncDone) {
                    TextButton(onClick = { showSyncDialog = false }) {
                        Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo administrativo de confirmação de repovoamento
    if (showRepopulateDbConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRepopulateDbConfirmDialog = false },
            title = { Text(getString("reimport_db"), fontWeight = FontWeight.Bold) },
            text = { Text(getString("reimport_confirm")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRepopulateDb()
                        showRepopulateDbConfirmDialog = false
                    }
                ) {
                    Text("OK", color = primaryColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepopulateDbConfirmDialog = false }) {
                    Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showPromoteVersionConfirmDialog) {
        var isPromoting by remember { mutableStateOf(false) }
        var promoteError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                if (!isPromoting) showPromoteVersionConfirmDialog = false 
            },
            title = { Text("Confirmar Promoção de Versão", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tem a certeza de que deseja promover a versão instalada no seu telemóvel para a versão oficial de produção?")
                    Spacer(Modifier.height(8.dp))
                    Text("Versão a Promover: v$appVersionName (Build $appVersionCode)", fontWeight = FontWeight.SemiBold)
                    Text("Versão Atual em Produção: ${if (latestProdVersionName.isEmpty()) "Nenhuma" else "v$latestProdVersionName (Build $latestProdVersionCode)"}")
                    if (promoteError != null) {
                        Text(promoteError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isPromoting = true
                        promoteError = null
                        onPromoteVersion(appVersionCode, appVersionName) { result ->
                            isPromoting = false
                            if (result.isSuccess) {
                                showPromoteVersionConfirmDialog = false
                                android.widget.Toast.makeText(context, "Versão promovida com sucesso!", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                promoteError = result.exceptionOrNull()?.localizedMessage ?: "Erro ao promover versão."
                            }
                        }
                    },
                    enabled = !isPromoting
                ) {
                    if (isPromoting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Promover", color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPromoteVersionConfirmDialog = false },
                    enabled = !isPromoting
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo administrativo de listagem/pesquisa de colaboradores para gestão
    if (showManageCollaboratorsDialog) {
        ManageCollaboratorsDialog(
            collaborators = collaborators,
            onDismiss = { showManageCollaboratorsDialog = false },
            onEditCollaborator = { colab ->
                adminSelectedCollaboratorForEdit = colab
                showManageCollaboratorsDialog = false
            },
            getString = getString,
            language = language
        )
    }

    // Diálogo de Edição de Perfil por parte do Admin (gravação direta)
    adminSelectedCollaboratorForEdit?.let { collaborator ->
        var editName by remember { mutableStateOf(collaborator.name) }
        var editJobTitle by remember { mutableStateOf(collaborator.jobTitle) }
        var editDept by remember { mutableStateOf(collaborator.department) }
        var editSite by remember { mutableStateOf(collaborator.businessUnit) }
        var editPhone by remember { mutableStateOf(collaborator.phone ?: "") }
        var editExtension by remember { mutableStateOf(collaborator.extension ?: "") }
        var editEmail by remember { mutableStateOf(collaborator.email ?: "") }
        var editPhotoBase64 by remember { mutableStateOf(collaborator.photoUrl) }

        var isUnitDropdownExpanded by remember { mutableStateOf(false) }
        var isDeptDropdownExpanded by remember { mutableStateOf(false) }

        val businessUnits = listOf("COF PT", "COF GR", "CoE PT", "CoE GR")
        val existingDepartmentsForSite = remember(editSite, collaborators) {
            val siteUpper = editSite.replace(" ", "").uppercase()
            collaborators.filter {
                it.businessUnit.replace(" ", "").uppercase() == siteUpper && it.department.isNotBlank()
            }.map { it.department }
             .distinct()
             .sorted()
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder,
            focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.White,
            unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else Color(0xFFF9FAFB),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )

        val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        val profilePhotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val base64 = uriToBase64(context, uri)
                if (base64 != null) {
                    editPhotoBase64 = base64
                }
            }
        }

        AlertDialog(
            onDismissRequest = { adminSelectedCollaboratorForEdit = null },
            title = { Text("${getString("edit_account_title")} - ${collaborator.name}", fontWeight = FontWeight.Bold) },
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
                                name = if (editName.isBlank()) collaborator.name else editName,
                                photoUrl = editPhotoBase64,
                                size = 90,
                                isOnline = collaborator.isOnline,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(CircleShape)
                                    .clickable { profilePhotoLauncher.launch("image/*") }
                            )
                            // Botão para carregar foto
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                                    .border(1.5.dp, strokeColor, CircleShape)
                                    .clickable { profilePhotoLauncher.launch("image/*") }
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
                            if (editPhotoBase64 != null) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                        .border(1.5.dp, strokeColor, CircleShape)
                                        .clickable { editPhotoBase64 = null }
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
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(getString("name")) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editJobTitle,
                        onValueChange = { editJobTitle = it },
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
                                        text = editSite,
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
                                            editSite = bu
                                            isUnitDropdownExpanded = false
                                            editDept = "" // Resetar departamento ao mudar de site
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Autocomplete/Sugestão de Departamento dependente da Unidade de Negócio
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editDept,
                            onValueChange = {
                                editDept = it
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
                                            editDept = dept
                                            isDeptDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(getString("phone")) },
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editExtension,
                        onValueChange = { editExtension = it },
                        label = { Text(getString("extension")) },
                        leadingIcon = { Icon(Icons.Filled.Dialpad, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(getString("email")) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = primaryColor) },
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onDeleteCollaborator(collaborator.id)
                            adminSelectedCollaboratorForEdit = null
                        }
                    ) {
                        Text(if (language == "PT") "Eliminar" else "Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                    
                    TextButton(
                        onClick = {
                            onUpdateProfile(
                                collaborator.copy(
                                    name = editName,
                                    jobTitle = editJobTitle,
                                    department = editDept,
                                    businessUnit = editSite,
                                    phone = if (editPhone.isBlank()) null else editPhone,
                                    extension = if (editExtension.isBlank()) null else editExtension,
                                    email = if (editEmail.isBlank()) null else editEmail,
                                    photoUrl = editPhotoBase64
                                )
                            )
                            adminSelectedCollaboratorForEdit = null
                        }
                    ) {
                        Text(getString("save"), color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { adminSelectedCollaboratorForEdit = null }) {
                    Text(getString("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

}

@Composable
fun SettingsHeaderBar(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
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
fun ProfileCard(
    name: String,
    jobTitle: String,
    company: String,
    photoUrl: String?,
    onEditPhotoClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onRemovePhotoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    var showPhotoOptions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (!photoUrl.isNullOrBlank() && onRemovePhotoClick != null) {
                            showPhotoOptions = true
                        } else {
                            onEditPhotoClick()
                        }
                    },
                contentAlignment = Alignment.BottomEnd
            ) {
                CollaboratorAvatar(
                    name = name, 
                    photoUrl = photoUrl, 
                    size = 60, 
                    isOnline = true
                )
                // Ícone de câmara sobreposto indicando edição de foto
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .border(1.dp, strokeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Mudar Foto",
                        tint = if (isDark) CoficabBlue else Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            
            if (showPhotoOptions) {
                DropdownMenu(
                    expanded = showPhotoOptions,
                    onDismissRequest = { showPhotoOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isDark) "Mudar Foto" else "Alterar Foto") },
                        leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = primaryColor) },
                        onClick = {
                            showPhotoOptions = false
                            onEditPhotoClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remover Foto", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = ErrorRed) },
                        onClick = {
                            showPhotoOptions = false
                            onRemovePhotoClick?.invoke()
                        }
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = jobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = company,
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Botão lápis editar dados de perfil
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onEditProfileClick),
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightGrayBg,
                border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else BorderStroke(1.dp, LightGrayBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar Perfil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusCard(
    isMockMode: Boolean, 
    isOffline: Boolean, 
    title: String,
    subtitle: String,
    statusText: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val statusColor = if (isMockMode) OnlineGreen else if (isOffline) ErrorRed else OnlineGreen

    // Cor do fundo azul celeste suave no tema claro
    val containerBg = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFEBF3FF) // Azul suave
    }
    
    val borderStroke = if (isDark) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    } else {
        BorderStroke(1.dp, Color(0xFFC7DFFF)) // Borda azulada
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .then(clickableModifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Nuvem laranja/brownish no design claro
            val iconColor = if (isDark) CoficabYellow else Color(0xFFB45309)
            Icon(
                imageVector = Icons.Filled.Cloud,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // Badge com borda verde e fundo branco
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isDark) statusColor.copy(alpha = 0.15f) else Color.White,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.8f)),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
fun SettingsMenuItem(
    title: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconColor: Color,
    onClick: () -> Unit = {},
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else LightGrayBg
    val borderStroke = if (isDark) null else BorderStroke(1.dp, LightGrayBorder.copy(alpha = 0.5f))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icone com container colorido
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    CofiCallTheme { SettingsScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenDarkPreview() {
    CofiCallTheme(darkTheme = true) { SettingsScreen(isDarkMode = true) }
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
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
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
fun AdminPanelSection(
    collaborators: List<Collaborator>,
    onRepopulateClick: () -> Unit,
    onManageClick: () -> Unit,
    onApproveProfile: (String) -> Unit,
    onRejectProfile: (String) -> Unit,
    getString: (String) -> String,
    language: String,
    onPromoteVersionClick: () -> Unit,
    appVersionName: String,
    prodVersionName: String,
    currentApkUrl: String = "",
    onUpdateApkUrl: (String, (Result<Unit>) -> Unit) -> Unit = { _, _ -> },
) {
    var showApkUrlDialog by remember { mutableStateOf(false) }
    var localSavedUrl by remember { mutableStateOf(currentApkUrl) }
    var apkUrlInput by remember(localSavedUrl) { mutableStateOf(localSavedUrl) }
    var isApkUrlSaving by remember { mutableStateOf(false) }
    var apkUrlSaveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header do Painel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SupervisorAccount,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = getString("admin_panel"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = getString("admin_desc"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Estatísticas rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminStatItem(
                    label = getString("total_collaborators"),
                    value = collaborators.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminStatItem(
                    label = "Pendentes",
                    value = collaborators.count { it.hasPendingChanges }.toString(),
                    modifier = Modifier.weight(1f)
                )
                AdminStatItem(
                    label = getString("favorites_count"),
                    value = collaborators.count { it.isFavorite }.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Botão Gerir Colaboradores e Botão Repovoar Base de Dados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(getString("manage_collaborators"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                
                OutlinedButton(
                    onClick = onRepopulateClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(getString("reimport_db"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else LightGrayBorder.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            
            Text(
                text = "GESTÃO DE VERSÕES",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Instalada: v$appVersionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Produção: ${if (prodVersionName.isEmpty()) "Nenhuma" else "v$prodVersionName"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = onPromoteVersionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                ) {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Promover Versão", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Configurar Link do APK
            HorizontalDivider(color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else LightGrayBorder.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))
            Text(
                text = "LINK DE ATUALIZAÇÃO (APK)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            // Mostrar o link atual configurado (truncado)
            val displayUrl = if (localSavedUrl.isNotEmpty()) {
                val id = localSavedUrl.substringAfter("id=").substringBefore("&")
                if (id.isNotEmpty()) "Drive: ...${id.takeLast(12)}" else localSavedUrl.take(40) + "..."
            } else "Nenhum link configurado"

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { showApkUrlDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Configurar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Diálogo de edição do link do APK
            if (showApkUrlDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isApkUrlSaving) showApkUrlDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Link,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Link do APK (Google Drive)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Cole o link de partilha do Google Drive. A conversão para download direto é feita automaticamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = apkUrlInput,
                                onValueChange = { apkUrlInput = it },
                                label = { Text("Link do Google Drive ou URL direto") },
                                placeholder = { Text("https://drive.google.com/file/d/...") },
                                singleLine = false,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isApkUrlSaving,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor
                                )
                            )
                            if (apkUrlSaveMessage != null) {
                                Text(
                                    text = apkUrlSaveMessage!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (apkUrlSaveMessage!!.startsWith("✓")) Color(0xFF4CAF50) else ErrorRed
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Converter link de partilha para link de download direto
                                val rawUrl = apkUrlInput.trim()
                                val finalUrl = if (rawUrl.contains("drive.google.com/file/d/")) {
                                    val fileId = rawUrl
                                        .substringAfter("/file/d/")
                                        .substringBefore("/")
                                        .substringBefore("?")
                                    "https://docs.google.com/uc?export=download&id=$fileId"
                                } else {
                                    rawUrl // URL já é direto, usar como está
                                }
                                isApkUrlSaving = true
                                apkUrlSaveMessage = null
                                onUpdateApkUrl(finalUrl) { result ->
                                    isApkUrlSaving = false
                                    if (result.isSuccess) {
                                        localSavedUrl = finalUrl
                                        apkUrlInput = finalUrl
                                        scope.launch {
                                            apkUrlSaveMessage = "✓ Link guardado com sucesso!"
                                            kotlinx.coroutines.delay(1500)
                                            showApkUrlDialog = false
                                            apkUrlSaveMessage = null
                                        }
                                    } else {
                                        apkUrlSaveMessage = "✗ Erro ao guardar o link. Tente novamente."
                                    }
                                }
                            },
                            enabled = !isApkUrlSaving && apkUrlInput.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            if (isApkUrlSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Guardar", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showApkUrlDialog = false
                                apkUrlSaveMessage = null
                                apkUrlInput = currentApkUrl
                            },
                            enabled = !isApkUrlSaving
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Aprovações Pendentes ("Stand-by")
            val pendingList = collaborators.filter { it.hasPendingChanges }
            if (pendingList.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else LightGrayBorder.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = "${getString("pending_approvals")} (${pendingList.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pendingList.forEach { colab ->
                        PendingApprovalCard(
                            colab = colab,
                            onApprove = { onApproveProfile(colab.id) },
                            onReject = { onRejectProfile(colab.id) },
                            getString = getString,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun PendingApprovalCard(
    colab: Collaborator,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    getString: (String) -> String,
    language: String,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else Color(0xFFF9FAFB)
    val borderCol = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else LightGrayBorder.copy(alpha = 0.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderCol)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Nome do Colaborador
            Text(
                text = colab.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            // Lista de Comparações
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (colab.pendingName != null && colab.pendingName != colab.name) {
                    ApprovalDiffRow(label = getString("name"), current = colab.name, proposed = colab.pendingName)
                }
                if (colab.pendingJobTitle != null && colab.pendingJobTitle != colab.jobTitle) {
                    ApprovalDiffRow(label = getString("job_title"), current = colab.jobTitle, proposed = colab.pendingJobTitle)
                }
                if (colab.pendingDepartment != null && colab.pendingDepartment != colab.department) {
                    ApprovalDiffRow(label = getString("department_field"), current = colab.department, proposed = colab.pendingDepartment)
                }
                if (colab.pendingPhone != null && colab.pendingPhone != colab.phone) {
                    ApprovalDiffRow(label = getString("phone"), current = colab.phone ?: "---", proposed = colab.pendingPhone)
                }
                if (colab.pendingExtension != null && colab.pendingExtension != colab.extension) {
                    ApprovalDiffRow(label = getString("extension"), current = colab.extension ?: "---", proposed = colab.pendingExtension)
                }
                if (colab.pendingEmail != null && colab.pendingEmail != colab.email) {
                    ApprovalDiffRow(label = getString("email"), current = colab.email ?: "---", proposed = colab.pendingEmail)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botões Aprovar/Rejeitar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReject,
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(getString("reject"), fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.width(8.dp))
                
                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text(getString("approve"), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ApprovalDiffRow(
    label: String,
    current: String,
    proposed: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = current,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
        )
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(10.dp)
        )
        Text(
            text = proposed,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OnlineGreen
        )
    }
}

@Composable
fun ManageCollaboratorsDialog(
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onEditCollaborator: (Collaborator) -> Unit,
    getString: (String) -> String,
    language: String,
) {
    var searchQuery by remember { mutableStateOf("") }
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

    val filtered = remember(searchQuery, collaborators) {
        if (searchQuery.isBlank()) {
            collaborators.sortedBy { it.name }
        } else {
            collaborators.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.jobTitle.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true) ||
                (it.email ?: "").contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.name }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(getString("manage_collaborators"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Barra de pesquisa
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(getString("search_collaborators")) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = primaryColor) },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Lista de Colaboradores
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else LightGrayBg,
                    border = BorderStroke(1.dp, if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else LightGrayBorder)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { colab ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onEditCollaborator(colab) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CollaboratorAvatar(
                                    name = colab.name,
                                    photoUrl = colab.photoUrl,
                                    size = 40,
                                    isOnline = colab.isOnline
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = colab.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = colab.jobTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Editar",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(getString("cancel").let { if (it == "Cancelar") "Fechar" else "Close" }, color = primaryColor, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

