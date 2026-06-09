package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Collaborator
import com.example.ui.components.CofiIcon
import com.example.ui.theme.StateFavoriteStar
import com.example.ui.theme.StateOfflineAmber
import com.example.ui.theme.StateOnlineGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.net.Uri
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.verticalScroll


fun uriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close() ?: return ""
        val resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
        "data:image/jpeg;base64,$base64"
    } catch (e: Exception) {
        Log.e("CofiCall", "Erro ao converter imagem para base64", e)
        ""
    }
}

fun getPhotoModel(photoUrl: String): Any {
    if (photoUrl.startsWith("data:image")) {
        try {
            val base64Data = photoUrl.substringAfter("base64,")
            val decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) ?: photoUrl
        } catch (e: Exception) {
            Log.e("CofiCall", "Erro ao decodificar base64", e)
        }
    }
    return photoUrl
}

object LocaleStrings {
    private val strings = mapOf(
        "pt" to mapOf(
            "tab_inicio" to "Início",
            "tab_colaboradores" to "Colaboradores",
            "tab_favoritos" to "Favoritos",
            "tab_def" to "Definições",
            "working_offline" to "A trabalhar offline. Sincronizado: ",
            "working_offline_short" to "Modo Offline • Atualizado há 5 min",
            "business_units" to "UNIDADES DE NEGÓCIO",
            "unidades" to "Unidades",
            "fabricas" to "Fábricas",
            "escritorios" to "Escritórios",
            "collaborator" to "COLABORADOR",
            "collaborators" to "COLABORADORES",
            "search_hint" to "Procurar Colaborador",
            "no_colab_found" to "Nenhum colaborador encontrado",
            "no_colab_desc" to "Experimente alterar os filtros ou pesquisar outra palavra.",
            "empty_fav" to "Sem contactos favoritos",
            "empty_fav_desc" to "Marque os seus colegas mais frequentes com uma estrela para aceder-lhes rapidamente aqui.",
            "empty_explore" to "Explorar Diretório",
            "settings" to "Definições",
            "local_db" to "Base de Dados Local",
            "last_sync" to "Última sincronização: ",
            "updated" to "ATUALIZADO",
            "language" to "Idioma",
            "language_name" to "Português (PT)",
            "my_account" to "Minha Conta",
            "offline_sync" to "Sincronização Offline",
            "dark_mode" to "Modo Escuro",
            "notifications" to "Notificações",
            "about" to "Sobre",
            "logout" to "Sair da Conta",
            "new_colab" to "Novo Colaborador",
            "full_name" to "Nome Completo",
            "dept_role" to "Departamento / Cargo",
            "business_unit_label" to "Unidade de Negócio / Site",
            "cancel" to "Cancelar",
            "add" to "Adicionar",
            "details_title" to "Detalhes do Colaborador",
            "edit" to "Editar",
            "delete" to "Eliminar",
            "delete_confirm" to "Tem a certeza que deseja eliminar este colaborador?",
            "delete_title" to "Eliminar Colaborador",
            "edit_colab" to "Editar Colaborador",
            "save" to "Guardar",
            "choose_photo" to "Escolher Foto",
            "photo_optional" to "Foto (Opcional)",
            "email" to "Email",
            "phone" to "Telemóvel",
            "admin_panel" to "Painel de Administração"
        ),
        "en" to mapOf(
            "tab_inicio" to "Home",
            "tab_colaboradores" to "Collaborators",
            "tab_favoritos" to "Favorites",
            "tab_def" to "Settings",
            "working_offline" to "Working offline. Synced: ",
            "working_offline_short" to "Offline Mode • Updated 5 min ago",
            "business_units" to "BUSINESS UNITS",
            "unidades" to "Units",
            "fabricas" to "Factories",
            "escritorios" to "Offices",
            "collaborator" to "COLABORATOR",
            "collaborators" to "COLLABORATORS",
            "search_hint" to "Search Collaborator",
            "no_colab_found" to "No collaborator found",
            "no_colab_desc" to "Try changing the filters or searching for another term.",
            "empty_fav" to "No favorite contacts",
            "empty_fav_desc" to "Mark your most frequent colleagues with a star to quickly access them here.",
            "empty_explore" to "Explore Directory",
            "settings" to "Settings",
            "local_db" to "Local Database",
            "last_sync" to "Last synchronization: ",
            "updated" to "UPDATED",
            "language" to "Language",
            "language_name" to "English (EN)",
            "my_account" to "My Account",
            "offline_sync" to "Offline Sync",
            "dark_mode" to "Dark Mode",
            "notifications" to "Notifications",
            "about" to "About",
            "logout" to "Log Out",
            "new_colab" to "New Collaborator",
            "full_name" to "Full Name",
            "dept_role" to "Department / Role",
            "business_unit_label" to "Business Unit / Site",
            "cancel" to "Cancel",
            "add" to "Add",
            "details_title" to "Collaborator Details",
            "edit" to "Edit",
            "delete" to "Delete",
            "delete_confirm" to "Are you sure you want to delete this collaborator?",
            "delete_title" to "Delete Collaborator",
            "edit_colab" to "Edit Collaborator",
            "save" to "Save",
            "choose_photo" to "Choose Photo",
            "photo_optional" to "Photo (Optional)",
            "email" to "Email",
            "phone" to "Mobile Phone",
            "admin_panel" to "Admin Panel"
        )
    )

    fun get(key: String, lang: String): String {
        return strings[lang]?.get(key) ?: strings["pt"]?.get(key) ?: key
    }
}

@Composable
fun CofiCallApp(
    viewModel: CollaboratorViewModel,
    modifier: Modifier = Modifier
) {
    val collaborators by viewModel.allCollaborators.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isSyncing by remember { mutableStateOf(false) }
    
    // Animation of the sync arrow
    val syncRotationAngle by animateFloatAsState(
        targetValue = if (isSyncing) 360f else 0f,
        animationSpec = if (isSyncing) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            snap()
        },
        label = "syncRotation"
    )

    var showSplash by remember { mutableStateOf(true) }
    var currentAuthScreen by remember { mutableStateOf("login") }
    var selectedCollaboratorForDetail by remember { mutableStateOf<Collaborator?>(null) }
    var selectedCollaboratorForEdit by remember { mutableStateOf<Collaborator?>(null) }
    var isMyAccountDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    if (!viewModel.isLoggedIn) {
        if (currentAuthScreen == "login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { currentAuthScreen = "register" }
            )
        } else {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { currentAuthScreen = "login" }
            )
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CofiAppBar(
                isSyncing = isSyncing,
                rotationAngle = syncRotationAngle,
                onSyncClick = {
                    if (!isSyncing) {
                        scope.launch {
                            isSyncing = true
                            viewModel.triggerSync()
                            delay(1200) // Simulated network call
                            isSyncing = false
                            Toast.makeText(context, "Sincronização concluída com sucesso!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        },
        bottomBar = {
            CofiBottomBar(
                currentScreen = viewModel.currentScreen,
                lang = viewModel.appLanguage,
                onScreenSelected = { viewModel.navigateTo(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (viewModel.currentScreen) {
                Screen.Inicio -> {
                    InicioScreen(
                        collaborators = collaborators,
                        viewModel = viewModel
                    )
                }
                Screen.Colaboradores -> {
                    ColaboradoresScreen(
                        collaborators = collaborators,
                        viewModel = viewModel,
                        onCardClick = { selectedCollaboratorForDetail = it }
                    )
                }
                Screen.Favoritos -> {
                    FavoritosScreen(
                        collaborators = collaborators,
                        viewModel = viewModel,
                        onCardClick = { selectedCollaboratorForDetail = it }
                    )
                }
                Screen.Definições -> {
                    DefiniçõesScreen(
                        viewModel = viewModel,
                        lastSyncTimeStr = lastSyncTime,
                        onEditProfileClick = { isMyAccountDialogOpen = true }
                    )
                }
            }

            // Universal Add Collaborator Dialog in directory
            if (viewModel.isAddDialogOpen) {
                AddCollaboratorDialog(
                    collaborators = collaborators,
                    onDismiss = { viewModel.closeAddDialog() },
                    onConfirm = { name, dept, comp, email, phone, photoUrl, isFactory, isOffice ->
                        viewModel.addCollaborator(name, dept, comp, email, phone, photoUrl, isFactory, isOffice)
                        Toast.makeText(context, "Colaborador adicionado!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Dialogs of details and edit for contacts
            if (selectedCollaboratorForDetail != null) {
                CollaboratorDetailsDialog(
                    collaborator = selectedCollaboratorForDetail!!,
                    onDismiss = { selectedCollaboratorForDetail = null },
                    onEditClick = {
                        selectedCollaboratorForEdit = selectedCollaboratorForDetail
                        selectedCollaboratorForDetail = null
                    },
                    viewModel = viewModel
                )
            }

            if (selectedCollaboratorForEdit != null) {
                EditCollaboratorDialog(
                    collaborator = selectedCollaboratorForEdit!!,
                    collaborators = collaborators,
                    onDismiss = { selectedCollaboratorForEdit = null },
                    onConfirm = { updated ->
                        viewModel.updateCollaborator(updated)
                        selectedCollaboratorForEdit = null
                        Toast.makeText(context, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (isMyAccountDialogOpen) {
                MyAccountDialog(
                    viewModel = viewModel,
                    onDismiss = { isMyAccountDialogOpen = false }
                )
            }
        }
    }
}

@Composable
fun CofiAppBar(
    isSyncing: Boolean,
    rotationAngle: Float,
    onSyncClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CofiIcon(
                    name = "corporate_fare",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "CofiCall",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            IconButton(
                onClick = onSyncClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .rotate(rotationAngle)
                    .testTag("appbar_sync_btn")
            ) {
                CofiIcon(
                    name = "sync",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CofiBottomBar(
    currentScreen: Screen,
    lang: String,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(80.dp),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val screens = listOf(
            Triple(Screen.Inicio, LocaleStrings.get("tab_inicio", lang), "home"),
            Triple(Screen.Colaboradores, LocaleStrings.get("tab_colaboradores", lang), "groups"),
            Triple(Screen.Favoritos, LocaleStrings.get("tab_favoritos", lang), "star"),
            Triple(Screen.Definições, LocaleStrings.get("tab_def", lang), "settings")
        )

        screens.forEach { (screen, label, iconName) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Box(
                        modifier = if (isSelected) {
                            Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        } else Modifier
                    ) {
                        CofiIcon(
                            name = if (isSelected && iconName == "star") "star" else if (iconName == "star") "star_outline" else iconName,
                            tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent // Customized design handles indicator manually
                ),
                modifier = Modifier.testTag("bottom_nav_${screen.name.lowercase()}")
            )
        }
    }
}

// ==========================================
// SCREEN 1: INICIO SCREEN
// ==========================================
@Composable
fun BoxScope.InicioScreen(
    collaborators: List<Collaborator>,
    viewModel: CollaboratorViewModel
) {
    val scrollState = rememberScrollState()
    val lang = viewModel.appLanguage

    // Aggregate counts dynamically
    val cofPtCount = collaborators.filter { it.company == "COF PT" }.size
    val cofGrCount = collaborators.filter { it.company == "COF GR" }.size
    val coePtCount = collaborators.filter { it.company == "CoE PT" }.size
    val coeGrCount = collaborators.filter { it.company == "CoE GR" }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Horizontal Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainFilter.values().forEach { filter ->
                    val isSelected = viewModel.mainFilter == filter
                    val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val textTint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Surface(
                        color = background,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { viewModel.setMainTabFilter(filter) }
                            .testTag("main_tab_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = when (filter) {
                                MainFilter.Unidades -> LocaleStrings.get("unidades", lang)
                                MainFilter.Fabricas -> LocaleStrings.get("fabricas", lang)
                                MainFilter.Escritorios -> LocaleStrings.get("escritorios", lang)
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = textTint,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Pink Offline Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFD9E3), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CofiIcon(
                    name = "cloud_off",
                    tint = Color(0xFF31111D),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = LocaleStrings.get("working_offline", lang) + " 2m",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF31111D),
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Section Title
        item {
            Text(
                text = LocaleStrings.get("business_units", lang),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Dynamic Company Cards according to Filter selecting
        val showCofPt = viewModel.mainFilter == MainFilter.Unidades || viewModel.mainFilter == MainFilter.Fabricas
        val showCofGr = viewModel.mainFilter == MainFilter.Unidades || viewModel.mainFilter == MainFilter.Fabricas
        val showCoePt = viewModel.mainFilter == MainFilter.Unidades || viewModel.mainFilter == MainFilter.Escritorios
        val showCoeGr = viewModel.mainFilter == MainFilter.Unidades || viewModel.mainFilter == MainFilter.Escritorios

        if (showCofPt) {
            item {
                BusinessUnitCard(
                    title = "COF PT",
                    subtitle = "Portugal",
                    count = cofPtCount,
                    isFactory = true,
                    lang = lang,
                    onClick = {
                        viewModel.filterByCompany("COF PT")
                        viewModel.selectLetterFilter(null)
                        viewModel.navigateTo(Screen.Colaboradores)
                    }
                )
            }
        }

        if (showCofGr) {
            item {
                BusinessUnitCard(
                    title = "COF GR",
                    subtitle = "Grécia",
                    count = cofGrCount,
                    isFactory = true,
                    lang = lang,
                    onClick = {
                        viewModel.filterByCompany("COF GR")
                        viewModel.selectLetterFilter(null)
                        viewModel.navigateTo(Screen.Colaboradores)
                    }
                )
            }
        }

        if (showCoePt) {
            item {
                BusinessUnitCard(
                    title = "CoE PT",
                    subtitle = "Centro de Excelência PT",
                    count = coePtCount,
                    isFactory = false,
                    lang = lang,
                    onClick = {
                        viewModel.filterByCompany("CoE PT")
                        viewModel.selectLetterFilter(null)
                        viewModel.navigateTo(Screen.Colaboradores)
                    }
                )
            }
        }

        if (showCoeGr) {
            item {
                BusinessUnitCard(
                    title = "CoE GR",
                    subtitle = "Centro de Excelência GR",
                    count = coeGrCount,
                    isFactory = false,
                    lang = lang,
                    onClick = {
                        viewModel.filterByCompany("CoE GR")
                        viewModel.selectLetterFilter(null)
                        viewModel.navigateTo(Screen.Colaboradores)
                    }
                )
            }
        }
    }
}

@Composable
fun BusinessUnitCard(
    title: String,
    subtitle: String,
    count: Int,
    isFactory: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("bu_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val iconBackground = if (isFactory) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                val iconTint = if (isFactory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBackground, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CofiIcon(
                        name = if (isFactory) "factory" else "hub",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = LocaleStrings.get(if (count == 1) "collaborator" else "collaborators", lang),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

// ==========================================
// SCREEN 2: COLABORADORES SCREEN
// ==========================================
fun makePhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:${phoneNumber.trim()}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao iniciar chamada", Toast.LENGTH_SHORT).show()
    }
}

fun sendEmail(context: Context, emailAddress: String) {
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:${emailAddress.trim()}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao enviar e-mail", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun BoxScope.ColaboradoresScreen(
    collaborators: List<Collaborator>,
    viewModel: CollaboratorViewModel,
    onCardClick: (Collaborator) -> Unit
) {
    val alphabetState = rememberScrollState()
    val alphabetLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }
    val context = LocalContext.current
    val lang = viewModel.appLanguage

    // Query & Filters Processing (only name, department, and letter filter)
    val filteredList = collaborators.filter { c ->
        val matchSearch = c.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                c.department.contains(viewModel.searchQuery, ignoreCase = true)
        
        val matchLetter = viewModel.selectedLetter == null || c.name.startsWith(viewModel.selectedLetter!!, ignoreCase = true)
        
        matchSearch && matchLetter
    }

    val groupedCollaborators = filteredList.groupBy { it.name.first().uppercase() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upper search bar nested layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Procurar input card de largura completa com botão "X"
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = LocaleStrings.get("search_hint", lang),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    leadingIcon = {
                        CofiIcon(name = "person_search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Limpar pesquisa",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("colab_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // Horizontal Alphabet Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(alphabetState)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { viewModel.selectLetterFilter(null) },
                shape = RoundedCornerShape(8.dp),
                color = if (viewModel.selectedLetter == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(height = 40.dp, width = 50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "A-Z",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (viewModel.selectedLetter == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            alphabetLetters.forEach { l ->
                val isSelectedLetter = viewModel.selectedLetter == l
                Surface(
                    onClick = { viewModel.selectLetterFilter(l) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelectedLetter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = l,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isSelectedLetter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Collaborators Directory Tree Lists
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = LocaleStrings.get("no_colab_found", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = LocaleStrings.get("no_colab_desc", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.outline
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val sortedGroups = groupedCollaborators.keys.sorted()
                sortedGroups.forEach { groupChar ->
                    item {
                        Text(
                            text = groupChar,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    val groupItems = groupedCollaborators[groupChar] ?: emptyList()
                    items(groupItems) { item ->
                        CollaboratorCard(
                            collaborator = item,
                            onToggleFav = { viewModel.toggleFavorite(item) },
                            onCallClick = { makePhoneCall(context, item.phone) },
                            onChatClick = { sendEmail(context, item.email) },
                            onCardClick = { onCardClick(item) }
                        )
                    }
                }
            }
        }
    }

    FloatingActionButton(
        onClick = { viewModel.openAddDialog() },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(bottom = 16.dp, end = 16.dp)
            .align(Alignment.BottomEnd)
            .testTag("fab_add_collaborator")
    ) {
        CofiIcon(name = "person_add")
    }
}

@Composable
fun CompanyFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun CollaboratorCard(
    collaborator: Collaborator,
    onToggleFav: () -> Unit,
    onCallClick: () -> Unit,
    onChatClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("colab_card_${collaborator.name.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Profile Avatar Container with status dot
                Box(
                    modifier = Modifier.size(56.dp)
                ) {
                    if (collaborator.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(collaborator.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar de ${collaborator.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        // Fallback Initials Avatar
                        val initials = collaborator.name.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    // Online state dot indicator
                    val statusColor = if (collaborator.status == "online") StateOnlineGreen else StateOfflineAmber
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(statusColor, shape = CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                // General info blocks
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = collaborator.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = collaborator.department,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Company tags
                    Text(
                        text = collaborator.company,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Quick Actions Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onCallClick,
                    icon = { CofiIcon(name = "call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
                
                NavigationBarItem(
                    selected = false,
                    onClick = onChatClick,
                    icon = { CofiIcon(name = "chat", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )

                IconButton(
                    onClick = onToggleFav,
                    modifier = Modifier.testTag("fav_btn_${collaborator.name.lowercase().replace(" ", "_")}")
                ) {
                    CofiIcon(
                        name = if (collaborator.isFavorite) "star" else "star_outline",
                        tint = if (collaborator.isFavorite) StateFavoriteStar else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 3: FAVORITOS SCREEN
// ==========================================
@Composable
fun BoxScope.FavoritosScreen(
    collaborators: List<Collaborator>,
    viewModel: CollaboratorViewModel,
    onCardClick: (Collaborator) -> Unit
) {
    val context = LocalContext.current
    val lang = viewModel.appLanguage
    val favorites = collaborators.filter { it.isFavorite }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LocaleStrings.get("tab_favoritos", lang),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )

        // Offline update banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CofiIcon(
                name = "cloud_off",
                tint = StateOfflineAmber,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = LocaleStrings.get("working_offline_short", lang),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        if (favorites.isEmpty()) {
            // Elegant illustrative Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CofiIcon(
                            name = "star_outline",
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )
                    }
                    Text(
                        text = LocaleStrings.get("empty_fav", lang),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = LocaleStrings.get("empty_fav_desc", lang),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Colaboradores) },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("empty_explore_btn")
                    ) {
                        Text(
                            text = LocaleStrings.get("empty_explore", lang),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(favorites) { item ->
                    CollaboratorCard(
                        collaborator = item,
                        onToggleFav = { viewModel.toggleFavorite(item) },
                        onCallClick = { makePhoneCall(context, item.phone) },
                        onChatClick = { sendEmail(context, item.email) },
                        onCardClick = { onCardClick(item) }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: DEFINIÇÕES SCREEN
// ==========================================
@Composable
fun DefiniçõesScreen(
    viewModel: CollaboratorViewModel,
    lastSyncTimeStr: String,
    onEditProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = viewModel.appLanguage
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isSyncingLocal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = LocaleStrings.get("settings", lang),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Active Profile view of currentUser
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(64.dp)) {
                            if (viewModel.currentUserPhoto.isNotEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(getPhotoModel(viewModel.currentUserPhoto))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                val initials = viewModel.currentUserName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2)
                                    .joinToString("")
                                    .uppercase()
                                    .ifEmpty { "U" }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(StateOnlineGreen, shape = CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = viewModel.currentUserName.ifEmpty { "Utilizador" },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = viewModel.currentUserDept.ifEmpty { "" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = viewModel.currentUserCompany.ifEmpty { "" },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .clickable { onEditProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        CofiIcon(name = "edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // DB Synced notification card (clicável para sincronizar)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!isSyncingLocal) {
                            scope.launch {
                                isSyncingLocal = true
                                viewModel.triggerSync()
                                kotlinx.coroutines.delay(1200)
                                isSyncingLocal = false
                                Toast.makeText(context, if (lang == "pt") "Sincronização concluída!" else "Sync complete!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CofiIcon(name = "storage", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                text = LocaleStrings.get("local_db", lang),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = LocaleStrings.get("last_sync", lang) + lastSyncTimeStr,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    if (isSyncingLocal) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Surface(
                            color = StateOnlineGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = LocaleStrings.get("updated", lang),
                                color = StateOnlineGreen,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Language selector setting
        item {
            var isLangExpanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = LocaleStrings.get("language", lang),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
                Box {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { isLangExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LocaleStrings.get("language_name", lang),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            CofiIcon(name = "chevron_right", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    DropdownMenu(
                        expanded = isLangExpanded,
                        onDismissRequest = { isLangExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🇵🇹 Português (PT)") },
                            onClick = { viewModel.setLanguage("pt"); isLangExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("🇬🇧 English (EN)") },
                            onClick = { viewModel.setLanguage("en"); isLangExpanded = false }
                        )
                    }
                }
            }
        }

        // Nav Options Lists
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column {
                    SettingsRowItem(
                        title = LocaleStrings.get("my_account", lang),
                        icon = "manage_accounts",
                        onClick = { onEditProfileClick() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = LocaleStrings.get("offline_sync", lang),
                        icon = "sync",
                        onClick = {
                            if (!isSyncingLocal) {
                                scope.launch {
                                    isSyncingLocal = true
                                    viewModel.triggerSync()
                                    kotlinx.coroutines.delay(1200)
                                    isSyncingLocal = false
                                    Toast.makeText(context, if (lang == "pt") "Sincronização concluída!" else "Sync complete!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Dark Mode Option switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleDarkMode(!viewModel.isDarkTheme) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CofiIcon(name = "dark_mode", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = LocaleStrings.get("dark_mode", lang),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Switch(
                            checked = viewModel.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isNotificationEnabled = !isNotificationEnabled }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CofiIcon(name = "notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = LocaleStrings.get("notifications", lang),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = LocaleStrings.get("about", lang),
                        icon = "info",
                        onClick = {
                            Toast.makeText(context, "CofiCall v2.4.0 • Enterprise Directory", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Red outlined Sair Button
        item {
            OutlinedButton(
                onClick = {
                    viewModel.logoutUser()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("logout_btn")
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CofiIcon(name = "exit_to_app", tint = MaterialTheme.colorScheme.error)
                    Text(
                        text = LocaleStrings.get("logout", lang),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Versão 2.4.0 (Build 882)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    title: String,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CofiIcon(name = icon, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        CofiIcon(name = "chevron_right", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ==========================================
// COMPONENT: ADD COLLABORATOR DIALOG
// ==========================================
@Composable
fun AddCollaboratorDialog(
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("COF PT") }
    var department by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var isFactory by remember { mutableStateOf(true) }

    val context = LocalContext.current

    val deptsForSelectedCompany = remember(company) {
        when (company) {
            "COF PT" -> listOf("Departamento de Logística", "Recursos Humanos", "Qualidade e Produção", "Manutenção Industrial", "Segurança e Ambiente")
            "COF GR" -> listOf("Engenharia de Redes", "Produção de Cabos", "Logística e Distribuição", "Controlo de Qualidade")
            "CoE PT" -> listOf("Operações de Campo", "Administração de Sistemas", "Desenvolvimento de Software", "Direção de Operações", "Suporte Técnico")
            "CoE GR" -> listOf("Marketing Digital", "Recursos Humanos Global", "Desenvolvimento de Negócios", "Análise de Dados")
            else -> listOf("Outro")
        }
    }

    var isCompanyExpanded by remember { mutableStateOf(false) }
    var isDeptExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(company) {
        department = deptsForSelectedCompany.first()
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = uriToBase64(context, it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Novo Colaborador",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Selecionar Foto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .clickable { photoLauncher.launch("image/*") }
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(getPhotoModel(photoUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto selecionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            CofiIcon(name = "camera_alt", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "Foto (Opcional)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_name"),
                    singleLine = true
                )

                // Site selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidade de Negócio / Site") },
                        trailingIcon = {
                            IconButton(onClick = { isCompanyExpanded = !isCompanyExpanded }) {
                                CofiIcon(name = if (isCompanyExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isCompanyExpanded,
                        onDismissRequest = { isCompanyExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                            DropdownMenuItem(
                                text = { Text(comp) },
                                onClick = {
                                    company = comp
                                    isFactory = comp.startsWith("COF")
                                    isCompanyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Dept dropdown (dinamicamente filtrado)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Departamento / Cargo") },
                        trailingIcon = {
                            IconButton(onClick = { isDeptExpanded = !isDeptExpanded }) {
                                CofiIcon(name = if (isDeptExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDeptExpanded,
                        onDismissRequest = { isDeptExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        deptsForSelectedCompany.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    department = d
                                    isDeptExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_email"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telemóvel") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_phone"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.outline)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && department.isNotBlank()) {
                                onConfirm(
                                    name,
                                    department,
                                    company,
                                    if (email.isNotBlank()) email else "${name.lowercase().replace(" ", "")}@coficall.com",
                                    if (phone.isNotBlank()) phone else "+351 912 345 600",
                                    photoUrl,
                                    isFactory,
                                    !isFactory // isOffice
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("dialog_confirm_btn")
                    ) {
                        Text("Adicionar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 5: SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    val t = it - 1.0f
                    t * t * ((2.0f + 1.0f) * t + 2.0f) + 1.0f
                }
            )
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF005FB8),
                        Color(0xFF00488D),
                        Color(0xFF060E20)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shadowElevation = 8.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CofiIcon(
                        name = "corporate_fare",
                        tint = Color(0xFFFFB400),
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CofiCall",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Conectando Colaboradores, Fortalecendo a Equipa",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFFFFB400),
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .alpha(alpha.value)
            )
        }
    }
}

// ==========================================
// SCREEN 6: LOGIN SCREEN
// ==========================================
@Composable
fun LoginScreen(
    viewModel: CollaboratorViewModel,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val lang = viewModel.appLanguage
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CofiIcon(
                name = "corporate_fare",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "CofiCall",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = if (lang == "pt") "Diretório Corporativo" else "Corporate Directory",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text(if (lang == "pt") "E-mail corporativo" else "Corporate email") },
                placeholder = { Text("user@coficab.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(if (lang == "pt") "Palavra-passe" else "Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        CofiIcon(
                            name = if (passwordVisible) "visibility_off" else "visibility",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = if (lang == "pt") "Preencha todos os campos." else "Please fill in all fields."
                        return@Button
                    }
                    if (!email.contains("@") || (!email.endsWith("@coficab.com") && !email.endsWith("@coficall.com"))) {
                        errorMessage = if (lang == "pt") "Use um e-mail @coficab.com ou @coficall.com" else "Use a @coficab.com or @coficall.com email"
                        return@Button
                    }
                    isLoading = true
                    viewModel.loginUser(email, password) { success ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, if (lang == "pt") "Bem-vindo ao CofiCall!" else "Welcome to CofiCall!", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = if (lang == "pt") "Credenciais incorretas." else "Invalid credentials."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (lang == "pt") "Entrar" else "Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = if (lang == "pt") "Não tem conta? Criar Conta" else "No account? Register Here",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// SCREEN 7: REGISTER SCREEN
// ==========================================
@Composable
fun RegisterScreen(
    viewModel: CollaboratorViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("COF PT") }
    var phone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val lang = viewModel.appLanguage
    val context = LocalContext.current

    val deptsForSelectedCompany = remember(company) {
        when (company) {
            "COF PT" -> listOf("Departamento de Logística", "Recursos Humanos", "Qualidade e Produção", "Manutenção Industrial", "Segurança e Ambiente")
            "COF GR" -> listOf("Engenharia de Redes", "Produção de Cabos", "Logística e Distribuição", "Controlo de Qualidade")
            "CoE PT" -> listOf("Operações de Campo", "Administração de Sistemas", "Desenvolvimento de Software", "Direção de Operações", "Suporte Técnico")
            "CoE GR" -> listOf("Marketing Digital", "Recursos Humanos Global", "Desenvolvimento de Negócios", "Análise de Dados")
            else -> listOf("Outro")
        }
    }

    var isCompanyExpanded by remember { mutableStateOf(false) }
    var isDeptExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(company) {
        dept = deptsForSelectedCompany.first()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            CofiIcon(
                name = "corporate_fare",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = if (lang == "pt") "Criar Conta" else "Create Account",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null },
                label = { Text(if (lang == "pt") "Nome Completo" else "Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text(if (lang == "pt") "E-mail corporativo" else "Corporate Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text(if (lang == "pt") "Palavra-passe" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = company,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (lang == "pt") "Unidade de Negócio / Site" else "Business Unit / Site") },
                    trailingIcon = {
                        IconButton(onClick = { isCompanyExpanded = !isCompanyExpanded }) {
                            CofiIcon(name = if (isCompanyExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = isCompanyExpanded,
                    onDismissRequest = { isCompanyExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                        DropdownMenuItem(
                            text = { Text(comp) },
                            onClick = {
                                company = comp
                                isCompanyExpanded = false
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dept,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (lang == "pt") "Departamento / Cargo" else "Department / Role") },
                    trailingIcon = {
                        IconButton(onClick = { isDeptExpanded = !isDeptExpanded }) {
                            CofiIcon(name = if (isDeptExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = isDeptExpanded,
                    onDismissRequest = { isDeptExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    deptsForSelectedCompany.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = {
                                dept = d
                                isDeptExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; errorMessage = null },
                label = { Text(if (lang == "pt") "Telemóvel" else "Mobile Phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
                        errorMessage = if (lang == "pt") "Todos os campos são obrigatórios." else "All fields are required."
                        return@Button
                    }
                    if (!email.contains("@") || (!email.endsWith("@coficab.com") && !email.endsWith("@coficall.com"))) {
                        errorMessage = if (lang == "pt") "Use um e-mail @coficab.com ou @coficall.com" else "Use a @coficab.com or @coficall.com email"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = if (lang == "pt") "A palavra-passe deve ter pelo menos 6 caracteres." else "Password must be at least 6 characters."
                        return@Button
                    }

                    viewModel.registerUser(name, email, password, dept, company, phone)
                    Toast.makeText(context, if (lang == "pt") "Conta criada com sucesso! Faça login." else "Account created successfully! Please login.", Toast.LENGTH_LONG).show()
                    onNavigateToLogin()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(if (lang == "pt") "Registar" else "Register")
            }

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = if (lang == "pt") "Já tem conta? Iniciar Sessão" else "Already have an account? Login",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// COMPONENT: COLLABORATOR DETAILS DIALOG
// ==========================================
@Composable
fun CollaboratorDetailsDialog(
    collaborator: Collaborator,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: CollaboratorViewModel
) {
    val context = LocalContext.current
    val lang = viewModel.appLanguage
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(LocaleStrings.get("delete_title", lang)) },
            text = { Text(LocaleStrings.get("delete_confirm", lang)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCollaborator(collaborator)
                        showDeleteConfirm = false
                        onDismiss()
                        Toast.makeText(context, if (lang == "pt") "Colaborador eliminado!" else "Collaborator deleted!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(LocaleStrings.get("delete", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(LocaleStrings.get("cancel", lang))
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                    IconButton(onClick = onDismiss) {
                        CofiIcon(name = "close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Box(modifier = Modifier.size(100.dp)) {
                    if (collaborator.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(getPhotoModel(collaborator.photoUrl))
                                .crossfade(true)
                                .build(),
                            contentDescription = collaborator.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        val initials = collaborator.name.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials.uppercase(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }

                    val statusColor = if (collaborator.status == "online") StateOnlineGreen else StateOfflineAmber
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(statusColor, shape = CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = collaborator.name,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = collaborator.department,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = collaborator.company,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { makePhoneCall(context, collaborator.phone) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CofiIcon(name = "call", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = LocaleStrings.get("phone", lang), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline))
                            Text(text = collaborator.phone, style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sendEmail(context, collaborator.email) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CofiIcon(name = "email", tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = LocaleStrings.get("email", lang), style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.outline))
                            Text(text = collaborator.email, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                if (viewModel.isAdmin) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LocaleStrings.get("edit", lang))
                        }

                        Button(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LocaleStrings.get("delete", lang))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: EDIT COLLABORATOR DIALOG
// ==========================================
@Composable
fun EditCollaboratorDialog(
    collaborator: Collaborator,
    collaborators: List<Collaborator>,
    onDismiss: () -> Unit,
    onConfirm: (Collaborator) -> Unit
) {
    var name by remember { mutableStateOf(collaborator.name) }
    var company by remember { mutableStateOf(collaborator.company) }
    var department by remember { mutableStateOf(collaborator.department) }
    var email by remember { mutableStateOf(collaborator.email) }
    var phone by remember { mutableStateOf(collaborator.phone) }
    var photoUrl by remember { mutableStateOf(collaborator.photoUrl) }
    var isFactory by remember { mutableStateOf(collaborator.isFactory) }

    val context = LocalContext.current

    val deptsForSelectedCompany = remember(company) {
        when (company) {
            "COF PT" -> listOf("Departamento de Logística", "Recursos Humanos", "Qualidade e Produção", "Manutenção Industrial", "Segurança e Ambiente")
            "COF GR" -> listOf("Engenharia de Redes", "Produção de Cabos", "Logística e Distribuição", "Controlo de Qualidade")
            "CoE PT" -> listOf("Operações de Campo", "Administração de Sistemas", "Desenvolvimento de Software", "Direção de Operações", "Suporte Técnico")
            "CoE GR" -> listOf("Marketing Digital", "Recursos Humanos Global", "Desenvolvimento de Negócios", "Análise de Dados")
            else -> listOf("Outro")
        }
    }

    var isCompanyExpanded by remember { mutableStateOf(false) }
    var isDeptExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(company) {
        if (!deptsForSelectedCompany.contains(department)) {
            department = deptsForSelectedCompany.first()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = uriToBase64(context, it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar Colaborador",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .clickable { photoLauncher.launch("image/*") }
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(getPhotoModel(photoUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto selecionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            CofiIcon(name = "camera_alt", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = "Foto (Opcional)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidade de Negócio / Site") },
                        trailingIcon = {
                            IconButton(onClick = { isCompanyExpanded = !isCompanyExpanded }) {
                                CofiIcon(name = if (isCompanyExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isCompanyExpanded,
                        onDismissRequest = { isCompanyExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                            DropdownMenuItem(
                                text = { Text(comp) },
                                onClick = {
                                    company = comp
                                    isFactory = comp.startsWith("COF")
                                    isCompanyExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Departamento / Cargo") },
                        trailingIcon = {
                            IconButton(onClick = { isDeptExpanded = !isDeptExpanded }) {
                                CofiIcon(name = if (isDeptExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDeptExpanded,
                        onDismissRequest = { isDeptExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        deptsForSelectedCompany.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    department = d
                                    isDeptExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telemóvel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.outline)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && department.isNotBlank()) {
                                onConfirm(
                                    collaborator.copy(
                                        name = name,
                                        department = department,
                                        company = company,
                                        email = email,
                                        phone = phone,
                                        photoUrl = photoUrl,
                                        isFactory = isFactory,
                                        isOffice = !isFactory
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT: MY ACCOUNT DIALOG
// ==========================================
@Composable
fun MyAccountDialog(
    viewModel: CollaboratorViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(viewModel.currentUserName) }
    var company by remember { mutableStateOf(viewModel.currentUserCompany) }
    var department by remember { mutableStateOf(viewModel.currentUserDept) }
    var phone by remember { mutableStateOf(viewModel.currentUserPhone) }
    var photoUrl by remember { mutableStateOf(viewModel.currentUserPhoto) }

    val context = LocalContext.current
    val lang = viewModel.appLanguage

    val deptsForSelectedCompany = remember(company) {
        when (company) {
            "COF PT" -> listOf("Departamento de Logística", "Recursos Humanos", "Qualidade e Produção", "Manutenção Industrial", "Segurança e Ambiente")
            "COF GR" -> listOf("Engenharia de Redes", "Produção de Cabos", "Logística e Distribuição", "Controlo de Qualidade")
            "CoE PT" -> listOf("Operações de Campo", "Administração de Sistemas", "Desenvolvimento de Software", "Direção de Operações", "Suporte Técnico")
            "CoE GR" -> listOf("Marketing Digital", "Recursos Humanos Global", "Desenvolvimento de Negócios", "Análise de Dados")
            else -> listOf("Outro")
        }
    }

    var isCompanyExpanded by remember { mutableStateOf(false) }
    var isDeptExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(company) {
        if (!deptsForSelectedCompany.contains(department)) {
            department = deptsForSelectedCompany.first()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUrl = uriToBase64(context, it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = LocaleStrings.get("my_account", lang),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                            .clickable { photoLauncher.launch("image/*") }
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(getPhotoModel(photoUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            CofiIcon(name = "camera_alt", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Text(
                        text = LocaleStrings.get("photo_optional", lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(LocaleStrings.get("full_name", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LocaleStrings.get("business_unit_label", lang)) },
                        trailingIcon = {
                            IconButton(onClick = { isCompanyExpanded = !isCompanyExpanded }) {
                                CofiIcon(name = if (isCompanyExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isCompanyExpanded,
                        onDismissRequest = { isCompanyExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                            DropdownMenuItem(
                                text = { Text(comp) },
                                onClick = {
                                    company = comp
                                    isCompanyExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(LocaleStrings.get("dept_role", lang)) },
                        trailingIcon = {
                            IconButton(onClick = { isDeptExpanded = !isDeptExpanded }) {
                                CofiIcon(name = if (isDeptExpanded) "keyboard_arrow_up" else "keyboard_arrow_down")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = isDeptExpanded,
                        onDismissRequest = { isDeptExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        deptsForSelectedCompany.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = {
                                    department = d
                                    isDeptExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(LocaleStrings.get("phone", lang)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(LocaleStrings.get("cancel", lang), color = MaterialTheme.colorScheme.outline)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && department.isNotBlank()) {
                                viewModel.updateCurrentUserProfile(name, department, company, phone, photoUrl)
                                onDismiss()
                                Toast.makeText(context, if (lang == "pt") "Dados de perfil atualizados!" else "Profile data updated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(LocaleStrings.get("save", lang), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}


