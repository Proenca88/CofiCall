package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
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
import com.example.coficall.model.BusinessUnit
import com.example.coficall.model.BusinessUnitType
import com.example.coficall.model.sampleBusinessUnits
import com.example.coficall.model.DepartmentInfo
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.CoficabYellow
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.theme.LightBluePillBg
import com.example.coficall.theme.LightGrayBg
import com.example.coficall.theme.LightGrayBorder
import com.example.coficall.theme.LightOfflineBg
import com.example.coficall.theme.LightOfflineText
import com.example.coficall.theme.NeutralMedGrey

@Composable
fun HomeScreen(
    isOffline: Boolean = false,
    businessUnits: List<BusinessUnit> = emptyList(),
    departments: List<DepartmentInfo> = emptyList(),
    onUnitClick: (BusinessUnit) -> Unit = {},
    onDepartmentClick: (DepartmentInfo) -> Unit = {},
    onRefresh: () -> Unit = {},
    getString: (String) -> String = { it },
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar com Logo e Sinc
        HomeTopBar(onRefresh = onRefresh)

        // Tab Row Ovals
        HomeTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            getString = getString
        )

        // Offline Banner
        if (isOffline) {
            OfflineBanner(getString = getString)
        }

        // Section Title
        val sectionTitle = when (selectedTab) {
            0 -> getString("business_units_title")
            1 -> getString("coes_title")
            else -> getString("departments").uppercase()
        }
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            when (selectedTab) {
                0 -> { // Unidades / Fábricas (COF PT, COF GR)
                    val units = businessUnits.filter { it.type == BusinessUnitType.UNIT }
                    if (units.isEmpty()) {
                        item { EmptyStateMessage() }
                    } else {
                        items(units, key = { it.id }) { unit ->
                            BusinessUnitCard(unit = unit, onClick = { onUnitClick(unit) }, getString = getString)
                        }
                    }
                }
                1 -> { // CoE (CoE PT, CoE GR)
                    val coes = businessUnits.filter { it.type == BusinessUnitType.FACTORY }
                    if (coes.isEmpty()) {
                        item { EmptyStateMessage() }
                    } else {
                        items(coes, key = { it.id }) { unit ->
                            BusinessUnitCard(unit = unit, onClick = { onUnitClick(unit) }, getString = getString)
                        }
                    }
                }
                2 -> { // Departamentos
                    if (departments.isEmpty()) {
                        item { EmptyStateMessage(getString("no_collaborators")) }
                    } else {
                        items(departments, key = { it.name }) { dept ->
                            DepartmentCard(department = dept, onClick = { onDepartmentClick(dept) }, getString = getString)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String = "Sem unidades disponíveis nesta categoria.") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralMedGrey
        )
    }
}

@Composable
fun HomeTopBar(onRefresh: () -> Unit, modifier: Modifier = Modifier) {
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
fun HomeTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    getString: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(getString("factories"), getString("coe"), getString("departments"))
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, text ->
            val isSelected = selectedTab == index
            
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun OfflineBanner(getString: (String) -> String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val containerColor = if (isDark) CoficabYellow.copy(alpha = 0.15f) else LightOfflineBg
    val contentColor = if (isDark) CoficabYellow else LightOfflineText
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = getString("offline_banner"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
fun BusinessUnitCard(
    unit: BusinessUnit,
    onClick: () -> Unit,
    getString: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val iconBg = if (unit.type == BusinessUnitType.FACTORY) {
                if (isDark) MaterialTheme.colorScheme.secondaryContainer else LightBluePillBg
            } else {
                primaryColor
            }
            
            val iconColor = if (unit.type == BusinessUnitType.FACTORY) {
                primaryColor
            } else {
                if (isDark) CoficabBlue else Color.White
            }
            
            val icon = if (unit.type == BusinessUnitType.FACTORY) {
                Icons.Filled.Hub
            } else {
                Icons.Filled.Factory
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = unit.shortName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (unit.shortName == "COF GR") "Portugal" else unit.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${unit.collaboratorCount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                )
                Text(
                    text = if (unit.collaboratorCount == 1) getString("collaborator") else getString("collaborators_upper"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun DepartmentCard(
    department: DepartmentInfo,
    onClick: () -> Unit,
    getString: (String) -> String,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isDark) MaterialTheme.colorScheme.secondaryContainer else LightBluePillBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Hub,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = department.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "CofiCab " + getString("department_field"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${department.collaboratorCount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                )
                Text(
                    text = if (department.collaboratorCount == 1) getString("collaborator") else getString("collaborators_upper"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    CofiCallTheme { HomeScreen() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenDarkPreview() {
    CofiCallTheme(darkTheme = true) { HomeScreen(isOffline = true) }
}
