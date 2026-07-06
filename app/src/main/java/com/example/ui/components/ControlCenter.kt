package com.example.ui.components

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassTheme
import com.example.ui.theme.glassmorphic
import com.example.utils.SystemInfoHelper

@Composable
fun ControlCenter(
    isOpen: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val screenWidth = 320.dp
    
    // Slide transition animation
    val offsetState by animateDpAsState(
        targetValue = if (isOpen) 0.dp else screenWidth,
        animationSpec = spring(stiffness = 250f),
        label = "ControlCenterSlide"
    )

    // DND state
    var isDndEnabled by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }

    // Volume & Brightness states
    var volume by remember { mutableStateOf(SystemInfoHelper.getVolumePercentage(context)) }
    var brightness by remember { mutableStateOf(0.6f) } // Local simulated brightness

    if (isOpen) {
        // Dimmed & Blurred backdrop overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable { onClose() }
                .testTag("control_center_backdrop")
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        // Panel
        Box(
            modifier = modifier
                .offset(x = offsetState)
                .width(screenWidth)
                .fillMaxHeight()
                .glassmorphic(
                    shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp),
                    blurRadius = 32.dp,
                    elevation = 24.dp,
                    isDark = true
                )
                .clickable(enabled = false) {} // block click propagation
                .windowInsetsPadding(WindowInsets.statusBars)
                .testTag("control_center_panel")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "控制中心",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Connection Shortcuts Grid (Wi-Fi & Bluetooth)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wi-Fi Box
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .testTag("cc_wifi_toggle"),
                        shape = RoundedCornerShape(20.dp),
                        isDark = false,
                        onClick = { SystemInfoHelper.openWifiSettings(context) }
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("Wi-Fi", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("連線已就緒", color = GlassTheme.TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Bluetooth Box
                    GlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp)
                            .testTag("cc_bluetooth_toggle"),
                        shape = RoundedCornerShape(20.dp),
                        isDark = false,
                        onClick = { SystemInfoHelper.openBluetoothSettings(context) }
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text("藍牙", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("開關點選", color = GlassTheme.TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggles Grid (DND, Flashlight, Brightness, Location)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // DND Toggle
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDndEnabled) GlassTheme.PrimaryAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { isDndEnabled = !isDndEnabled }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDndEnabled) Icons.Default.DoNotDisturbOn else Icons.Default.DoNotDisturbOff,
                            contentDescription = null,
                            tint = if (isDndEnabled) Color(0xFF10B981) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("勿擾模式", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Flashlight Simulated Toggle
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFlashlightOn) GlassTheme.PrimaryAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                            .clickable { isFlashlightOn = !isFlashlightOn }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = null,
                            tint = if (isFlashlightOn) Color(0xFFFBBF24) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("手電筒", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Volume slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("音量調校", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${(volume * 100).toInt()}%", color = GlassTheme.TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            SystemInfoHelper.setVolumePercentage(context, it)
                        },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF0EA5E9),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                            thumbColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brightness slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LightMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("顯示亮度", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${(brightness * 100).toInt()}%", color = GlassTheme.TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFFFBBF24),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                            thumbColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control Center System Settings shortcut
                Button(
                    onClick = { SystemInfoHelper.openDisplaySettings(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cc_system_settings_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassTheme.PrimaryAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("開啟系統偏好設定", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer branding
                Text(
                    text = "AURA OS Core v1.0",
                    color = Color.White.copy(alpha = 0.15f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
