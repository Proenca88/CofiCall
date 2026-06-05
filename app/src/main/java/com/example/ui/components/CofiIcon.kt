package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CofiIcon(
    name: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    when (name) {
        "corporate_fare", "building" -> {
            Canvas(modifier = modifier.size(24.dp)) {
                val strokeColor = if (tint == Color.Unspecified) Color(0xFF00488D) else tint
                
                // Left tall building
                drawRect(
                    color = strokeColor,
                    topLeft = Offset(2f, 4f),
                    size = Size(10f, 16f),
                    style = Stroke(width = 2f)
                )
                // Right smaller building
                drawRect(
                    color = strokeColor,
                    topLeft = Offset(12f, 8f),
                    size = Size(10f, 12f),
                    style = Stroke(width = 2f)
                )
                // Windows left building
                drawRect(color = strokeColor, topLeft = Offset(4f, 6f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(8f, 6f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(4f, 10f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(8f, 10f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(4f, 14f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(8f, 14f), size = Size(2f, 2f))

                // Windows right building
                drawRect(color = strokeColor, topLeft = Offset(14f, 10f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(18f, 10f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(14f, 14f), size = Size(2f, 2f))
                drawRect(color = strokeColor, topLeft = Offset(18f, 14f), size = Size(2f, 2f))
            }
        }
        "factory" -> {
            Canvas(modifier = modifier.size(24.dp)) {
                val fillColor = if (tint == Color.Unspecified) Color(0xFF00488D) else tint
                val path = Path().apply {
                    // Sawtooth roof building
                    moveTo(2f, 20f)
                    lineTo(2f, 10f)
                    lineTo(7f, 14f)
                    lineTo(7f, 10f)
                    lineTo(12f, 14f)
                    lineTo(12f, 10f)
                    lineTo(17f, 14f)
                    lineTo(17f, 6f) // Stack chimney
                    lineTo(20f, 6f)
                    lineTo(20f, 20f)
                    close()
                }
                drawPath(path = path, color = fillColor)
                
                // Chimney smoke line
                drawLine(
                    color = fillColor,
                    start = Offset(18.5f, 4f),
                    end = Offset(18.5f, 2f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        }
        "hub" -> {
            Canvas(modifier = modifier.size(24.dp)) {
                val strokeColor = if (tint == Color.Unspecified) Color(0xFF565F71) else tint
                // Central circle
                drawCircle(color = strokeColor, radius = 3.5f, center = Offset(12f, 12f))
                
                // External circles
                drawCircle(color = strokeColor, radius = 2.5f, center = Offset(6f, 6f))
                drawCircle(color = strokeColor, radius = 2.5f, center = Offset(18f, 6f))
                drawCircle(color = strokeColor, radius = 2.5f, center = Offset(12f, 19f))
                drawCircle(color = strokeColor, radius = 2.5f, center = Offset(5f, 15f))
                drawCircle(color = strokeColor, radius = 2.5f, center = Offset(19f, 15f))

                // Connect lines
                drawLine(color = strokeColor, start = Offset(12f, 12f), end = Offset(6f, 6f), strokeWidth = 1.5f)
                drawLine(color = strokeColor, start = Offset(12f, 12f), end = Offset(18f, 6f), strokeWidth = 1.5f)
                drawLine(color = strokeColor, start = Offset(12f, 12f), end = Offset(12f, 19f), strokeWidth = 1.5f)
                drawLine(color = strokeColor, start = Offset(12f, 12f), end = Offset(5f, 15f), strokeWidth = 1.5f)
                drawLine(color = strokeColor, start = Offset(12f, 12f), end = Offset(19f, 15f), strokeWidth = 1.5f)
            }
        }
        "cloud_off" -> {
            Canvas(modifier = modifier.size(24.dp)) {
                val strokeColor = if (tint == Color.Unspecified) Color(0xFFB07100) else tint
                
                // Cloud base outline (simplified)
                val path = Path().apply {
                    moveTo(19.35f, 10.04f)
                    quadraticTo(18.6f, 6.51f, 15.5f, 5.5f)
                    // We just draw a cloud shape outline
                    moveTo(6f, 13f)
                    lineTo(6f, 15f)
                }
                
                // Draw simplified cross-line & offline cloud indication
                drawCircle(color = strokeColor, radius = 5f, center = Offset(12f, 10f), style = Stroke(width = 2f))
                drawRect(color = strokeColor, topLeft = Offset(6f, 12f), size = Size(12f, 5f), style = Stroke(width = 2f))
                
                // Cross line
                drawLine(
                    color = strokeColor,
                    start = Offset(4f, 4f),
                    end = Offset(20f, 20f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        "sync" -> {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "home" -> {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "groups" -> {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Groups",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "star" -> {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "star_outline" -> {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = "Star Outline",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "settings" -> {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "call" -> {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Call",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "chat" -> {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Chat",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "person_search" -> {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "person_add" -> {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Person",
                tint = if (tint == Color.Unspecified) Color.White else tint,
                modifier = modifier
            )
        }
        "filter_list" -> {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Filter List",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "chevron_right" -> {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Chevron Right",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        "exit_to_app" -> {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit App",
                tint = if (tint == Color.Unspecified) Color.Red else tint,
                modifier = modifier
            )
        }
        "language" -> {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Language Info",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Face",
                tint = if (tint == Color.Unspecified) Color.Gray else tint,
                modifier = modifier
            )
        }
    }
}
