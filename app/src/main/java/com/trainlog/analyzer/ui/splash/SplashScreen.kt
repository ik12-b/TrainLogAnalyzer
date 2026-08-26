package com.trainlog.analyzer.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trainlog.analyzer.R
import com.trainlog.analyzer.ui.theme.InkBg
import com.trainlog.analyzer.ui.theme.TealGlow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.88f) }
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseA"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        delay(1500)
        alpha.animateTo(0f, tween(400))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(InkBg, Color(0xFF0E2A32), InkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .background(TealGlow.copy(alpha = 0.15f * pulseAlpha))
                )
                Image(
                    painter = painterResource(R.drawable.app_icon),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(28.dp))
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "TrainLog",
                color = Color(0xFFF4FAFB),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Lab notebook untuk training model",
                color = Color(0xFFF4FAFB).copy(alpha = 0.65f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                TealGlow.copy(alpha = if (i == 1) pulseAlpha else 0.35f)
                            )
                    )
                }
            }
        }
    }
}
