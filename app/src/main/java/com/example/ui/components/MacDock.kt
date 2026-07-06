package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.LauncherItem
import com.example.ui.launcher.AppInfo
import com.example.ui.theme.GlassTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun MacDock(
    dockItems: List<LauncherItem>,
    installedApps: List<AppInfo>,
    onLaunchApp: (packageName: String) -> Unit,
    onRemoveFromDock: (item: LauncherItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keeps track of the currently hovered/dragged index for macOS-style scaling
    var activeTouchIndex by remember { mutableStateOf<Int?>(null) }
    var bouncingPkg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.Center
    ) {
        // macOS Dock Card
        GlassCard(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .testTag("mac_dock"),
            shape = RoundedCornerShape(28.dp),
            elevation = 12.dp,
            blurRadius = 24.dp,
            borderWidth = 1.2.dp,
            isDark = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                // Calculate which icon is closest to the pointer position
                                val containerWidth = size.width
                                val itemWidth = containerWidth / (dockItems.size.coerceAtLeast(1))
                                val index = (change.position.x / itemWidth).toInt()
                                activeTouchIndex = index.coerceIn(0, dockItems.size - 1)
                            },
                            onDragEnd = { activeTouchIndex = null },
                            onDragCancel = { activeTouchIndex = null }
                        )
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dockItems.forEachIndexed { index, item ->
                    val appInfo = installedApps.firstOrNull { it.packageName == item.packageName }
                    
                    // Proximity-based macOS scaling calculations
                    val isNear = activeTouchIndex != null && abs(index - activeTouchIndex!!) <= 1
                    val scaleFactor by animateFloatAsState(
                        targetValue = when {
                            activeTouchIndex == null -> 1.0f
                            index == activeTouchIndex -> 1.45f
                            isNear -> 1.25f
                            else -> 0.95f
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "DockMagnification"
                    )

                    // Bouncing animation state for launching
                    val isBouncing = bouncingPkg == item.packageName
                    val bounceOffset by animateFloatAsState(
                        targetValue = if (isBouncing) -24f else 0f,
                        animationSpec = if (isBouncing) {
                            infiniteRepeatable(
                                animation = keyframes {
                                    durationMillis = 500
                                    -24f at 250 with FastOutSlowInEasing
                                    0f at 500 with FastOutSlowInEasing
                                },
                                repeatMode = RepeatMode.Restart
                            )
                        } else {
                            spring(dampingRatio = Spring.DampingRatioNoBouncy)
                        },
                        label = "DockBounce"
                    )

                    var isMenuExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .offset(y = bounceOffset.dp)
                            .scale(scaleFactor)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        isMenuExpanded = true
                                    },
                                    onTap = {
                                        coroutineScope.launch {
                                            bouncingPkg = item.packageName
                                            delay(1000) // let it bounce delightfully
                                            onLaunchApp(item.packageName)
                                            bouncingPkg = null
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (appInfo != null && appInfo.icon != null) {
                            val bitmap = remember(appInfo.icon) {
                                appInfo.icon.toBitmap().asImageBitmap()
                            }
                            Image(
                                bitmap = bitmap,
                                contentDescription = appInfo.label,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("dock_app_icon_${item.packageName}"),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            // Placeholder
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            )
                        }

                        // Long-press options
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B)) // dark glass menu base
                        ) {
                            DropdownMenuItem(
                                text = { Text("從 Dock 移除", color = Color.White, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    isMenuExpanded = false
                                    onRemoveFromDock(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
