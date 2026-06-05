package com.example.coficall.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    onDarkModeToggle: (Boolean) -> Unit = {},
    isDarkMode: Boolean = false,
    currentUserEmail: String? = null,
    isMockMode: Boolean = false,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var darkMode by remember { mutableStateOf(isDarkMode) }
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header com Logo e Refresh
        SettingsHeaderBar()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
        ) {
            // Título Grande
            item {
                Text(
                    text = "Definições",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Profile Card (Ricardo Silva com imagem real da Coil)
            item {
                val derivedName = currentUserEmail?.substringBefore("@")
                    ?.replace(".", " ")
                    ?.split(" ")
                    ?.joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } } 
                    ?: "Ricardo Silva"

                ProfileCard(
                    name = derivedName,
                    jobTitle = if (isMockMode) "Diretor de Operações" else "Diretor de Operações",
                    company = if (isMockMode) "LOGÍSTICA GLOBAL S.A." else "LOGÍSTICA GLOBAL S.A.",
                )
            }

            // Sync Status Card (Base de Dados Local com Nuvem)
            item {
                SyncStatusCard(isMockMode = isMockMode, isOffline = isOffline)
            }

            // Idioma Section (Estilo Card Dropdown)
            item {
                Text(
                    text = "Idioma",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else LightGrayBg),
                    border = if (isDark) null else BorderStroke(1.dp, LightGrayBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Português (PT)",
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
            }

            // Menu Items individuais separados conforme design
            item {
                Spacer(Modifier.height(16.dp))
                
                // Cores dos ícones
                val blueIconContainer = if (isDark) MaterialTheme.colorScheme.secondaryContainer else LightBluePillBg
                val blueIconTint = if (isDark) MaterialTheme.colorScheme.primary else CoficabRoyalBlue
                
                val greyIconContainer = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightGrayBg
                val greyIconTint = if (isDark) Color.White else NeutralCharcoal
                
                val pinkIconContainer = if (isDark) Color(0xFF881337) else Color(0xFFFFE4E6)
                val pinkIconTint = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)

                // 1. Minha Conta
                SettingsMenuItem(
                    title = "Minha Conta",
                    icon = Icons.Filled.Person,
                    iconContainerColor = blueIconContainer,
                    iconColor = blueIconTint,
                    onClick = {}
                )
                
                // 2. Sincronização Offline
                SettingsMenuItem(
                    title = "Sincronização Offline",
                    icon = Icons.Filled.CloudSync,
                    iconContainerColor = blueIconContainer,
                    iconColor = blueIconTint,
                    onClick = {}
                )
                
                // 3. Modo Escuro
                SettingsMenuItem(
                    title = "Modo Escuro",
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
                
                // 4. Notificações
                SettingsMenuItem(
                    title = "Notificações",
                    icon = Icons.Filled.Notifications,
                    iconContainerColor = pinkIconContainer,
                    iconColor = pinkIconTint,
                    onClick = {}
                )
                
                // 5. Sobre
                SettingsMenuItem(
                    title = "Sobre",
                    icon = Icons.Filled.Info,
                    iconContainerColor = greyIconContainer,
                    iconColor = greyIconTint,
                    onClick = {}
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
                        text = "Sair da Conta",
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
                    text = "Versão 2.4.0 (Build 882)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralMedGrey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
fun SettingsHeaderBar(modifier: Modifier = Modifier) {
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
            IconButton(onClick = {}) {
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
fun ProfileCard(name: String, jobTitle: String, company: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder
    val primaryColor = if (isDark) CoficabYellow else CoficabRoyalBlue

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
            // Ricardo Silva - avatar real carregado pela Coil
            CollaboratorAvatar(
                name = name, 
                photoUrl = "https://randomuser.me/api/portraits/men/10.jpg", 
                size = 60, 
                isOnline = true
            )
            
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
            
            // Botão lápis editar
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { },
                color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightGrayBg,
                border = if (isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else BorderStroke(1.dp, LightGrayBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SyncStatusCard(isMockMode: Boolean, isOffline: Boolean, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    
    val title = if (isMockMode) "Base de Dados Local" else if (isOffline) "Base de Dados Local" else "Base de Dados Cloud"
    val subtitle = if (isMockMode) "Última sincronização: Hoje, 09:42" else if (isOffline) "Última sincronização: Hoje, 09:42" else "Sincronizado com Firebase"
    val statusText = if (isMockMode) "ATUALIZADO" else if (isOffline) "OFFLINE" else "ATUALIZADO"
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
