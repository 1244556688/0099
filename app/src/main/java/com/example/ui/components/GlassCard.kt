package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.glassmorphic

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 8.dp,
    isDark: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .glassmorphic(
                shape = shape,
                blurRadius = blurRadius,
                borderWidth = borderWidth,
                elevation = elevation,
                isDark = isDark
            )
            .then(clickModifier)
            .padding(16.dp)
    ) {
        content()
    }
}
