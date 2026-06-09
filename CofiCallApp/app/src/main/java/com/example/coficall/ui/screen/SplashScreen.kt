package com.example.coficall.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coficall.R
import com.example.coficall.theme.CoficabBlue
import com.example.coficall.theme.CoficabBlueMid
import com.example.coficall.theme.CoficabYellow

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
                    // OvershootInterpolator natural
                    val t = it - 1.0f
                    t * t * ((2.0f + 1.0f) * t + 2.0f) + 1.0f
                }
            )
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CoficabBlueMid,
                        CoficabBlue,
                        Color(0xFF070E20)
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
            // Container do Logótipo com efeito Glassmorphism sutil e elevação
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale.value)
                    .alpha(alpha.value),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                shadowElevation = 16.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "CofiCall Logo",
                        modifier = Modifier.fillMaxSize()
                    )

                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Nome da App
            Text(
                text = "CofiCall",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slogan Institucional
            Text(
                text = "Conectando Colaboradores, Fortalecendo a Equipa",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Indicador de Carregamento Premium Dourado
            CircularProgressIndicator(
                color = CoficabYellow,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .alpha(alpha.value)
            )
        }
    }
}
