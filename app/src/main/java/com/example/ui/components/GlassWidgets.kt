package com.example.ui.components

import android.content.Context
import android.os.BatteryManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassTheme
import com.example.utils.SystemInfoHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1. Clock & Date Widget
 */
@Composable
fun ClockWidget(modifier: Modifier = Modifier) {
    var timeString by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf("") }
    var dayOfWeekString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        while (true) {
            val calendar = Calendar.getInstance()
            timeString = timeFormat.format(calendar.time)
            dateString = dateFormat.format(calendar.time)
            dayOfWeekString = dayFormat.format(calendar.time)
            delay(1000)
        }
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_clock"),
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayOfWeekString,
                color = GlassTheme.SecondaryAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateString,
                color = GlassTheme.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 2. Weather Widget (Interactive, cycles cities)
 */
data class MockWeather(val city: String, val temp: Int, val desc: String, val humidity: Int, val wind: Int)

@Composable
fun WeatherWidget(modifier: Modifier = Modifier) {
    val cities = listOf(
        MockWeather("Taipei", 28, "下午短暫雷陣雨", 84, 12),
        MockWeather("Tokyo", 21, "多雲時晴", 60, 8),
        MockWeather("New York", 18, "微風有雨", 90, 15),
        MockWeather("London", 14, "大霧轉晴", 95, 10),
        MockWeather("Paris", 19, "晴朗微風", 50, 6)
    )
    var cityIndex by remember { mutableStateOf(0) }
    val currentWeather = cities[cityIndex]

    // Rotate icon slowly
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherRotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRotate"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_weather")
            .clickable { cityIndex = (cityIndex + 1) % cities.size },
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = currentWeather.city,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentWeather.temp}°C",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = currentWeather.desc,
                    color = GlassTheme.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = when {
                        currentWeather.desc.contains("晴") -> Icons.Default.WbSunny
                        currentWeather.desc.contains("雨") -> Icons.Default.CloudQueue
                        else -> Icons.Default.Cloud
                    },
                    contentDescription = null,
                    tint = if (currentWeather.desc.contains("晴")) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                    modifier = Modifier
                        .size(44.dp)
                        .rotate(if (currentWeather.desc.contains("晴")) angle else 0f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Air, contentDescription = null, tint = GlassTheme.TextSecondary, modifier = Modifier.size(12.dp))
                    Text(
                        text = " ${currentWeather.wind} km/h",
                        color = GlassTheme.TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * 3. Calendar Month View Widget
 */
@Composable
fun CalendarWidget(modifier: Modifier = Modifier) {
    val calendar = remember { Calendar.getInstance() }
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
    val year = calendar.get(Calendar.YEAR)

    // Calculate days of current month
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = remember {
        val temp = calendar.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        temp.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed Sun
    }

    val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_calendar"),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "$monthName $year",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Day Headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        color = GlassTheme.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Grid Layout for Days
            var currentGridCell = 0
            val totalCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7

            Column {
                for (row in 0 until (totalCells / 7)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayNumber = currentGridCell - firstDayOfWeek + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNumber in 1..daysInMonth) {
                                    val isCurrent = dayNumber == currentDay
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrent) GlassTheme.PrimaryAccent else Color.Transparent
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            color = if (isCurrent) Color.White else GlassTheme.TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            currentGridCell++
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Music Player Widget
 */
@Composable
fun MusicWidget(modifier: Modifier = Modifier) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentTrack by remember { mutableStateOf("夜空中最亮的星") }
    var artist by remember { mutableStateOf("逃跑計劃") }

    val trackList = listOf(
        "夜空中最亮的星" to "逃跑計劃",
        "稻香" to "周杰倫",
        "Glassy Heart" to "Aura Ambient",
        "起風了" to "買辣椒也用券"
    )
    var trackIndex by remember { mutableStateOf(0) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_music"),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spinning Disc Mock
                val infiniteTransition = rememberInfiniteTransition(label = "MusicDisc")
                val discRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(6000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "DiscAngle"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .rotate(if (isPlaying) discRotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            Brush.sweepGradient(
                                colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF334155))
                            )
                        )
                        drawCircle(Color.Black, radius = size.minDimension / 4f)
                        drawCircle(Color.White, radius = size.minDimension / 8f)
                    }
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = currentTrack,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = artist,
                        color = GlassTheme.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    trackIndex = if (trackIndex > 0) trackIndex - 1 else trackList.size - 1
                    currentTrack = trackList[trackIndex].first
                    artist = trackList[trackIndex].second
                }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White)
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassTheme.PrimaryAccent.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(onClick = {
                    trackIndex = (trackIndex + 1) % trackList.size
                    currentTrack = trackList[trackIndex].first
                    artist = trackList[trackIndex].second
                }) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

/**
 * 5. Battery Status Widget
 */
@Composable
fun BatteryWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val batteryInfo by SystemInfoHelper.rememberBatteryInfo(context)

    // Animated Charging Glow
    val infiniteTransition = rememberInfiniteTransition(label = "BatteryPulse")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_battery"),
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "電池資訊",
                    color = GlassTheme.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${batteryInfo.percentage}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "%",
                        color = GlassTheme.TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
                Text(
                    text = if (batteryInfo.isCharging) "閃充中..." else "發電中",
                    color = if (batteryInfo.isCharging) Color(0xFF10B981) else GlassTheme.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Visual Battery Cell Custom drawing
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 2.dp.toPx()
                    val cornerRadius = 6.dp.toPx()

                    // Battery outline
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        size = size.copy(width = size.width - 6.dp.toPx()),
                        style = Stroke(width = strokeWidth),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                    )

                    // Battery tip
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.6f),
                        topLeft = androidx.compose.ui.geometry.Offset(size.width - 4.dp.toPx(), size.height / 3f),
                        size = size.copy(width = 4.dp.toPx(), height = size.height / 3f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )

                    // Battery level fill
                    val padding = 4.dp.toPx()
                    val maxFillWidth = size.width - 6.dp.toPx() - (padding * 2)
                    val fillWidth = maxFillWidth * (batteryInfo.percentage / 100f)
                    val fillColor = when {
                        batteryInfo.isCharging -> Color(0xFF10B981).copy(alpha = alphaGlow)
                        batteryInfo.percentage < 20 -> Color(0xFFEF4444)
                        else -> Color(0xFF38BDF8)
                    }

                    drawRoundRect(
                        color = fillColor,
                        topLeft = androidx.compose.ui.geometry.Offset(padding, padding),
                        size = size.copy(width = fillWidth, height = size.height - (padding * 2)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
                if (batteryInfo.isCharging) {
                    Icon(
                        Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 6. System Hardware (CPU & Memory) Gauge Widget
 */
@Composable
fun SystemStatsWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cpuUsage by SystemInfoHelper.rememberCpuUsage()
    var memoryStats by remember { mutableStateOf(SystemInfoHelper.getMemoryStats(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            memoryStats = SystemInfoHelper.getMemoryStats(context)
            delay(3000)
        }
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_system_stats"),
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // CPU Progress ring
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    val animatedCpu by animateFloatAsState(targetValue = cpuUsage.toFloat() / 100f, label = "CpuRing")
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.1f), style = Stroke(width = 6.dp.toPx()))
                        drawArc(
                            color = Color(0xFF0EA5E9),
                            startAngle = -90f,
                            sweepAngle = animatedCpu * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(text = "$cpuUsage%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "CPU 使用率", color = GlassTheme.TextSecondary, fontSize = 10.sp)
            }

            // RAM Progress ring
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(60.dp)) {
                    val animatedRam by animateFloatAsState(targetValue = memoryStats.percentage.toFloat() / 100f, label = "RamRing")
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.1f), style = Stroke(width = 6.dp.toPx()))
                        drawArc(
                            color = Color(0xFF10B981),
                            startAngle = -90f,
                            sweepAngle = animatedRam * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(text = "${memoryStats.percentage}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "RAM (${memoryStats.usedGB}G/${memoryStats.totalGB}G)", color = GlassTheme.TextSecondary, fontSize = 9.sp)
            }
        }
    }
}

/**
 * 7. Quick Settings Controls Widget
 */
@Composable
fun QuickSettingsWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isWifiEnabled by remember { mutableStateOf(SystemInfoHelper.isWifiConnected(context)) }
    var volume by remember { mutableStateOf(SystemInfoHelper.getVolumePercentage(context)) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_quick_settings"),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "快捷設定",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wi-Fi Quick Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWifiEnabled) GlassTheme.PrimaryAccent.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
                        .clickable {
                            SystemInfoHelper.openWifiSettings(context)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWifiEnabled) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = if (isWifiEnabled) Color(0xFF38BDF8) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Wi-Fi", color = Color.White, fontSize = 11.sp)
                }

                // Bluetooth Trigger
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable {
                            SystemInfoHelper.openBluetoothSettings(context)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "藍牙", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume adjustment slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        SystemInfoHelper.setVolumePercentage(context, it)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF38BDF8),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                        thumbColor = Color.White
                    )
                )
            }
        }
    }
}
