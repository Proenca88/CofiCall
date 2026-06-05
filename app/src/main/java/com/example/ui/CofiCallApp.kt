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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
                        viewModel = viewModel
                    )
                }
                Screen.Favoritos -> {
                    FavoritosScreen(
                        collaborators = collaborators,
                        viewModel = viewModel
                    )
                }
                Screen.Definições -> {
                    DefiniçõesScreen(
                        viewModel = viewModel,
                        lastSyncTimeStr = lastSyncTime
                    )
                }
            }

            // Universal Add Collaborator Dialog in directory
            if (viewModel.isAddDialogOpen) {
                AddCollaboratorDialog(
                    onDismiss = { viewModel.closeAddDialog() },
                    onConfirm = { name, dept, comp, email, phone, isFactory, isOffice ->
                        viewModel.addCollaborator(name, dept, comp, email, phone, isFactory, isOffice)
                        Toast.makeText(context, "Colaborador adicionado!", Toast.LENGTH_SHORT).show()
                    }
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
            Triple(Screen.Inicio, "Início", "home"),
            Triple(Screen.Colaboradores, "Colaboradores", "groups"),
            Triple(Screen.Favoritos, "Favoritos", "star"),
            Triple(Screen.Definições, "Definições", "settings")
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
                                MainFilter.Unidades -> "Unidades"
                                MainFilter.Fabricas -> "Fábricas"
                                MainFilter.Escritorios -> "Escritórios"
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
                    text = "A trabalhar offline. Última sincronização há 2m.",
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
                text = "UNIDADES DE NEGÓCIO",
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
                    text = if (count == 1) "COLABORADOR" else "COLABORADORES",
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
@Composable
fun BoxScope.ColaboradoresScreen(
    collaborators: List<Collaborator>,
    viewModel: CollaboratorViewModel
) {
    val alphabetState = rememberScrollState()
    val alphabetLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".map { it.toString() }
    val context = LocalContext.current
    
    // Manage active filters panel toggle local state
    var showFilterOptions by remember { mutableStateOf(false) }

    // Query & Filters Processing
    val filteredList = collaborators.filter { c ->
        val matchSearch = c.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                c.department.contains(viewModel.searchQuery, ignoreCase = true)
        
        val matchCompany = viewModel.selectedCompanyFilter == null || c.company == viewModel.selectedCompanyFilter
        
        val matchLetter = viewModel.selectedLetter == null || c.name.startsWith(viewModel.selectedLetter!!, ignoreCase = true)
        
        matchSearch && matchCompany && matchLetter
    }

    // Grouping Collaborators for alphabet structure headers
    val groupedCollaborators = filteredList.groupBy { it.name.first().uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize(),
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
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Procurar input card
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Procurar Colaborador",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    },
                    leadingIcon = {
                        CofiIcon(name = "person_search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("colab_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )

                // Filtros toggle trigger buttons
                Surface(
                    onClick = { showFilterOptions = !showFilterOptions },
                    shape = RoundedCornerShape(12.dp),
                    color = if (showFilterOptions || viewModel.selectedCompanyFilter != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("filter_options_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CofiIcon(
                            name = "filter_list",
                            tint = if (showFilterOptions || viewModel.selectedCompanyFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (showFilterOptions || viewModel.selectedCompanyFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Company Category Expandable Filters Block
            AnimatedVisibility(visible = showFilterOptions || viewModel.selectedCompanyFilter != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filtrar por Empresa:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Todos" chip option
                        CompanyFilterChip(
                            label = "Todas",
                            isSelected = viewModel.selectedCompanyFilter == null,
                            onClick = { viewModel.filterByCompany(null) }
                        )

                        listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                            CompanyFilterChip(
                                label = comp,
                                isSelected = viewModel.selectedCompanyFilter == comp,
                                onClick = { viewModel.filterByCompany(comp) }
                            )
                        }
                    }
                }
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
            // "A-Z" reset option
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
                        text = "Nenhum colaborador encontrado",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.outline
                        )
                    )
                    Text(
                        text = "Experimente alterar os filtros ou pesquisar outra palavra.",
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
                // Traverse grouped keys alphabetically
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
                                .border(
                                    width = 0.dp,
                                    color = Color.Transparent
                                )
                        )
                    }

                    val groupItems = groupedCollaborators[groupChar] ?: emptyList()
                    items(groupItems) { item ->
                        CollaboratorCard(
                            collaborator = item,
                            onToggleFav = { viewModel.toggleFavorite(item) },
                            onCallClick = {
                                Toast.makeText(context, "A ligar para ${item.name} (${item.phone})...", Toast.LENGTH_SHORT).show()
                            },
                            onChatClick = {
                                Toast.makeText(context, "Abrir chat com ${item.name} em ${item.email}...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dynamic Floating Action Button for prompt insertions
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
    onChatClick: () -> Unit
) {
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
    viewModel: CollaboratorViewModel
) {
    val context = LocalContext.current
    val favorites = collaborators.filter { it.isFavorite }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Favoritos",
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
                text = "Modo Offline • Atualizado há 5 min",
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
                        text = "Sem contactos favoritos",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Marque os seus colegas mais frequentes com uma estrela para aceder-lhes rapidamente aqui.",
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
                            text = "Explorar Diretório",
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
                        onCallClick = {
                            Toast.makeText(context, "A ligar para ${item.name} (${item.phone})...", Toast.LENGTH_SHORT).show()
                        },
                        onChatClick = {
                            Toast.makeText(context, "Abrir chat com ${item.name} em ${item.email}...", Toast.LENGTH_SHORT).show()
                        }
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
    lastSyncTimeStr: String
) {
    val context = LocalContext.current
    var isNotificationEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "Definições",
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
                            // Standard placeholder portrait
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "RS",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
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
                                text = "Ricardo Silva",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Diretor de Operações",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Text(
                                text = "LOGÍSTICA GLOBAL S.A.",
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
                            .clickable {
                                Toast.makeText(context, "Editar perfil indisponível offline", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CofiIcon(name = "edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // DB Synced notification card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
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
                        CofiIcon(name = "language", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Column {
                            Text(
                                text = "Base de Dados Local",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Última sincronização: $lastSyncTimeStr",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        color = StateOnlineGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ATUALIZADO",
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

        // Language selector setting
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Idioma",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
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
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Português (PT)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        CofiIcon(name = "chevron_right", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        title = "Minha Conta",
                        icon = "groups",
                        onClick = { Toast.makeText(context, "Módulo de conta em desenvolvimento", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = "Sincronização Offline",
                        icon = "sync",
                        onClick = { Toast.makeText(context, "Sincronização offline ativa", Toast.LENGTH_SHORT).show() }
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
                            CofiIcon(name = "settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Modo Escuro",
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
                            CofiIcon(name = "language", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Notificações",
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
                        title = "Sobre",
                        icon = "language",
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
                onClick = { Toast.makeText(context, "Sessão encerrada", Toast.LENGTH_SHORT).show() },
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
                        text = "Sair da Conta",
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
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Boolean, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("COF PT") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isFactory by remember { mutableStateOf(true) }

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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Novo Colaborador",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                // Input field for Nome
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Completo") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_name"),
                    singleLine = true
                )

                // Input field for Cargo/Departamento
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Departamento / Cargo") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_dept"),
                    singleLine = true
                )

                // Selection of company tags
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Empresa Unidade",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("COF PT", "COF GR", "CoE PT", "CoE GR").forEach { comp ->
                            val isSelected = company == comp
                            Surface(
                                onClick = {
                                    company = comp
                                    // Auto configure type based on selection
                                    isFactory = comp.startsWith("COF")
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = comp,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_email"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telemóvel") },
                    modifier = Modifier.fillMaxWidth().testTag("add_input_phone"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                // Action buttons footer
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
