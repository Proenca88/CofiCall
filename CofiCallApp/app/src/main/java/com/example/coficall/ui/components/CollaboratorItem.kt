package com.example.coficall.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.coficall.model.Collaborator
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabRoyalBlue
import com.example.coficall.theme.LightBluePillBg
import com.example.coficall.theme.LightGrayBorder
import com.example.coficall.theme.NeutralMedGrey
import com.example.coficall.theme.StarYellow

@Composable
fun CollaboratorListItem(
    collaborator: Collaborator,
    onFavoriteToggle: (Collaborator) -> Unit,
    onPhoneClick: (Collaborator) -> Unit,
    onMessageClick: (Collaborator) -> Unit,
    onClick: (Collaborator) -> Unit = {},
    modifier: Modifier = Modifier,
    showAllActions: Boolean = false,
) {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val cardBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else LightGrayBorder
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick(collaborator) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar com bolinha online
            CollaboratorAvatar(
                name = collaborator.name,
                photoUrl = collaborator.photoUrl,
                size = 54,
                isOnline = collaborator.isOnline
            )

            Spacer(Modifier.width(12.dp))

            // Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = collaborator.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = collaborator.jobTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                
                // Mostrar a Unidade/Empresa em maiúsculas e negrito azul/cinza apenas se showAllActions (ecrã favoritos)
                if (showAllActions) {
                    Text(
                        text = collaborator.businessUnit.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF63B3ED) else CoficabRoyalBlue,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Coluna de Ações na Direita
            // Em vez de uma linha horizontal, dividimos em topo (Estrela) e fundo (Botões de Ação)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(68.dp)
            ) {
                // Topo: Estrela de Favorito
                IconButton(
                    onClick = { onFavoriteToggle(collaborator) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (collaborator.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (collaborator.isFavorite) StarYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Fundo: Botões de Ligar / Enviar Mensagem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val buttonColor = if (isDark) MaterialTheme.colorScheme.secondaryContainer else LightBluePillBg
                    val iconTint = if (isDark) MaterialTheme.colorScheme.primary else CoficabRoyalBlue

                    if (!collaborator.phone.isNullOrBlank()) {
                        // Botão de Ligar (Telefone)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(buttonColor)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${collaborator.phone.trim()}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("CofiCall", "Erro ao efetuar chamada", e)
                                    }
                                    onPhoneClick(collaborator)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Phone,
                                contentDescription = "Ligar",
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Botão de Mensagem (SMS)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(buttonColor)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:${collaborator.phone.trim()}")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("CofiCall", "Erro ao enviar SMS", e)
                                    }
                                    onMessageClick(collaborator)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Message,
                                contentDescription = "Mensagem",
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CollaboratorAvatar(
    name: String,
    photoUrl: String?,
    size: Int,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    val avatarColors = listOf(
        0xFF1A56DB, 0xFF7C3AED, 0xFF059669, 0xFFD97706,
        0xFFDC2626, 0xFF0891B2, 0xFF065F46,
    )
    val colorIndex = (name.firstOrNull()?.code ?: 0) % avatarColors.size
    val avatarColor = Color(avatarColors[colorIndex])
    
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val strokeColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        // Foto ou Iniciais
        if (!photoUrl.isNullOrBlank()) {
            val bitmap = remember(photoUrl) {
                if (photoUrl.startsWith("data:image") || photoUrl.length > 1000) {
                    base64ToBitmap(photoUrl)
                } else {
                    null
                }
            }
            SubcomposeAsyncImage(
                model = bitmap ?: photoUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(avatarColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = (size * 0.35).sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = (size * 0.35).sp),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        
        // Determinar cor da bolinha de status
        val dotColor = if (isOnline) {
            Color(0xFF22C55E) // Verde online
        } else {
            if (name.contains("Ricardo Oliveira", ignoreCase = true)) {
                Color(0xFF9CA3AF) // Cinza offline
            } else {
                Color(0xFFF97316) // Laranja ausente/away
            }
        }

        Box(
            modifier = Modifier
                .size((size * 0.28).dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(dotColor)
                .border(1.5.dp, strokeColor, CircleShape)
        )
    }
}

@Composable
fun AlphabetSectionHeader(letter: String, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background != Color(0xFFF5F7FA) && MaterialTheme.colorScheme.background != Color(0xFFF4F6FA)
    val color = if (isDark) Color(0xFF63B3ED) else CoficabRoyalBlue

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Linha vertical azul e depois a letra
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

fun base64ToBitmap(base64Str: String): Bitmap? {
    return try {
        val cleanStr = if (base64Str.startsWith("data:image")) {
            base64Str.substringAfter(",")
        } else {
            base64Str
        }
        val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
