package com.example.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HarmonicAudioEngine
import kotlinx.coroutines.delay

@Composable
fun IntroLandscapeScreen(
    onIntroFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val alphaAnim = remember { Animatable(0f) }
    val gamesAlpha = remember { Animatable(0f) }
    val shineTransition = rememberInfiniteTransition(label = "shine")
    val shineX = shineTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shineX"
    )

    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playIntroJingle()
        // Entrance zoom & fade
        alphaAnim.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        scaleAnim.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        delay(200)
        gamesAlpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        // Total intro playback time ~3.5 seconds
        delay(2500)
        onIntroFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD61A1A), // Vivid Crimson center
                        Color(0xFF8B0000), // Rich Dark Crimson edges
                        Color(0xFF5A0000)  // Deep shadow perimeter
                    )
                )
            )
            .testTag("intro_landscape_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic decorative background aura
        Box(
            modifier = Modifier
                .size(500.dp, 260.dp)
                .blur(40.dp)
                .background(Color(0x33FF6B6B), RoundedCornerShape(130.dp))
        )

        // Center Brand Identity
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
        ) {
            // 3D Metallic "ATAGUL" Logo with depth layering
            Box(contentAlignment = Alignment.Center) {
                // Layer 1: Deep shadow offset
                Text(
                    text = "ATAGUL",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 5.sp,
                    color = Color(0xFF350000),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )

                // Layer 2: Extruded bevel bevel
                Text(
                    text = "ATAGUL",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 5.sp,
                    color = Color(0xFFB0BEC5),
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )

                // Layer 3: Specular bright white surface
                Text(
                    text = "ATAGUL",
                    fontSize = 58.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 5.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // "GAMES" subtitle with wide tracking
            Text(
                text = "G A M E S",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 10.sp,
                color = Color.White.copy(alpha = 0.95f),
                modifier = Modifier
                    .alpha(gamesAlpha.value)
                    .padding(top = 2.dp)
            )
        }

        // Top-right Skip Button ("Atla")
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { onIntroFinished() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("skip_intro_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Atla",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Atla",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
