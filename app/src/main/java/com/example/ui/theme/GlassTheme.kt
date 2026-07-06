package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTheme {
    // Pure glass colors
    val WhiteGlassBg = Color(0x22FFFFFF) // 13% white
    val WhiteGlassBorder = Color(0x44FFFFFF) // 26% white
    val DarkGlassBg = Color(0x33000000) // 20% black
    val DarkGlassBorder = Color(0x22FFFFFF) // 13% white border

    // Vibrant accent colors
    val PrimaryAccent = Color(0xFF0D9488) // Teal 600
    val SecondaryAccent = Color(0xFF0ea5e9) // Sky 500
    val GlassGlow = Color(0x1538BDF8) // Sky glow

    // Text colors
    val TextPrimary = Color(0xFFF1F5F9) // Slate 100
    val TextSecondary = Color(0xFF94A3B8) // Slate 400

    /**
     * Gradients for Glass effects
     */
    val GlassGradientLight = Brush.linearGradient(
        colors = listOf(
            Color(0x33FFFFFF), // Brighter top-left
            Color(0x11FFFFFF)  // Translucent bottom-right
        )
    )

    val GlassGradientDark = Brush.linearGradient(
        colors = listOf(
            Color(0x2AFFFFFF),
            Color(0x05000000)
        )
    )

    val GlassBorderGradient = Brush.linearGradient(
        colors = listOf(
            Color(0x66FFFFFF), // Bright top-left reflection
            Color(0x11FFFFFF), // Soft fade
            Color(0x22000000)  // Dark contrast bottom-right
        )
    )

    val AuroraBackground = Brush.radialGradient(
        colors = listOf(
            Color(0xFF0F172A), // Slate 900
            Color(0xFF020617), // Slate 950
            Color(0xFF1E1B4B)  // Indigo 950
        )
    )
}

/**
 * Custom Modifier extension to apply real-time Glassmorphic styles.
 */
fun Modifier.glassmorphic(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    isDark: Boolean = true
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = Color.Black.copy(alpha = 0.25f),
        spotColor = Color.Black.copy(alpha = 0.35f)
    )
    .then(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Apply background blur natively on Android 12+
            this.blur(blurRadius)
        } else this
    )
    .background(
        brush = if (isDark) GlassTheme.GlassGradientDark else GlassTheme.GlassGradientLight,
        shape = shape
    )
    .border(
        border = BorderStroke(borderWidth, GlassTheme.GlassBorderGradient),
        shape = shape
    )
    .clip(shape)
