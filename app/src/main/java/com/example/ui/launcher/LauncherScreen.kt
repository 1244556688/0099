package com.example.ui.launcher

import android.app.WallpaperManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FolderEntity
import com.example.data.LauncherItem
import com.example.ui.components.*
import com.example.ui.theme.GlassTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // UI States observed from ViewModel
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val allLauncherItems by viewModel.allLauncherItems.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val widgetConfigs by viewModel.widgetConfigs.collectAsStateWithLifecycle()

    val filteredApps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // Layout filtering
    val dockItems = remember(allLauncherItems) {
        allLauncherItems.filter { it.containerType == 1 }.sortedBy { it.cellX }
    }
    val desktopItems = remember(allLauncherItems) {
        allLauncherItems.filter { it.containerType == 0 && it.folderId == null }
    }

    // Interactive overlays state
    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var isControlCenterOpen by remember { mutableStateOf(false) }
    var activeFolderToShow by remember { mutableStateOf<FolderEntity?>(null) }

    // Read system wallpaper safely
    val wallpaperManager = remember { WallpaperManager.getInstance(context) }
    val wallpaperDrawable = remember {
        try {
            wallpaperManager.drawable
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }
    val wallpaperBitmap = remember(wallpaperDrawable) {
        try {
            wallpaperDrawable?.toBitmap()?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // --- 1. Wallpaper Backdrop ---
        if (wallpaperBitmap != null) {
            Image(
                bitmap = wallpaperBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // High-end drifting Aurora backup gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlassTheme.AuroraBackground)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "BackgroundOrbs")
                
                val orb1X by infiniteTransition.animateFloat(
                    initialValue = -50f,
                    targetValue = 280f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(15000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Orb1X"
                )
                val orb1Y by infiniteTransition.animateFloat(
                    initialValue = 100f,
                    targetValue = 500f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(18000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Orb1Y"
                )

                val orb2X by infiniteTransition.animateFloat(
                    initialValue = 300f,
                    targetValue = -20f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(22000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Orb2X"
                )
                val orb2Y by infiniteTransition.animateFloat(
                    initialValue = 700f,
                    targetValue = 300f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(16000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Orb2Y"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2238BDF8), Color.Transparent),
                            radius = 500f
                        ),
                        center = androidx.compose.ui.geometry.Offset(orb1X.dp.toPx(), orb1Y.dp.toPx())
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x2210B981), Color.Transparent),
                            radius = 600f
                        ),
                        center = androidx.compose.ui.geometry.Offset(orb2X.dp.toPx(), orb2Y.dp.toPx())
                    )
                }
            }
        }

        // --- 2. Main Desktop System ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Status bar buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Brand Logo / Clock indicator
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.BlurOn,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AURA DESKTOP",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Right Control Center Trigger Button
                IconButton(
                    onClick = { isControlCenterOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .testTag("control_center_trigger")
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Control Center",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Scrollable Content Grid split: Widgets column on left, App Icons on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // WIDGET PANELS COLUMN (Take 45% width on wide layouts)
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Render Active Glass Widgets
                    ClockWidget()
                    WeatherWidget()
                    SystemStatsWidget()
                    BatteryWidget()
                    MusicWidget()
                    QuickSettingsWidget()
                }

                // DESKTOP GRID PANEL (Take remaining 55% width)
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                ) {
                    // Title index
                    Text(
                        text = "常用桌面",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
                    )

                    // Desktop shortcuts + folders list
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("desktop_items_grid"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Folders Render
                        items(folders) { folder ->
                            FolderGridCard(
                                folder = folder,
                                onClick = { activeFolderToShow = folder }
                            )
                        }

                        // 2. Apps Shortcuts Render
                        items(desktopItems) { item ->
                            val appInfo = installedApps.firstOrNull { it.packageName == item.packageName }
                            DesktopIconCard(
                                item = item,
                                appInfo = appInfo,
                                onLaunch = { viewModel.launchApp(context, item.packageName) },
                                onDelete = { viewModel.removeItem(item) }
                            )
                        }
                    }
                }
            }

            // Central Drawer Trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { isAppDrawerOpen = true },
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("app_drawer_trigger"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("應用程式", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- 3. Bottom Mac-style Dock ---
            MacDock(
                dockItems = dockItems,
                installedApps = installedApps,
                onLaunchApp = { pkg -> viewModel.launchApp(context, pkg) },
                onRemoveFromDock = { item -> viewModel.removeItem(item) }
            )
        }

        // --- 4. Control Center Drawer Overlay ---
        ControlCenter(
            isOpen = isControlCenterOpen,
            onClose = { isControlCenterOpen = false }
        )

        // --- 5. All Apps Drawer Overlay ---
        AppDrawer(
            isOpen = isAppDrawerOpen,
            onClose = { isAppDrawerOpen = false },
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            apps = filteredApps,
            folders = folders,
            onLaunchApp = { pkg ->
                viewModel.launchApp(context, pkg)
                isAppDrawerOpen = false
            },
            onAddToDesktop = { app ->
                viewModel.addAppToDesktop(app, 0, 0)
                isAppDrawerOpen = false
            },
            onAddToDock = { app ->
                viewModel.addAppToDock(app)
                isAppDrawerOpen = false
            },
            onCreateFolder = { name, firstApp ->
                // Look for second app, or make a standalone folder
                viewModel.createFolder(name, LauncherItem(packageName = firstApp.packageName, className = firstApp.className, label = firstApp.label, containerType = 0), firstApp)
                isAppDrawerOpen = false
            },
            onAddAppToFolder = { app, folderId ->
                viewModel.addAppToFolder(app, folderId)
                isAppDrawerOpen = false
            }
        )

        // --- 6. Active Folder Display Dialog ---
        if (activeFolderToShow != null) {
            val folderApps = remember(allLauncherItems, activeFolderToShow) {
                allLauncherItems.filter { it.containerType == 2 && it.folderId == activeFolderToShow!!.id }
            }

            AlertDialog(
                onDismissRequest = { activeFolderToShow = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(activeFolderToShow!!.name, color = Color.White, fontWeight = FontWeight.Black)
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        if (folderApps.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("資料夾是空的，長按 App 新增進來", color = Color.LightGray, fontSize = 12.sp)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(folderApps) { appItem ->
                                    val appInfo = installedApps.firstOrNull { it.packageName == appItem.packageName }
                                    var isItemMenuOpen by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .combinedClickable(
                                                onClick = {
                                                    viewModel.launchApp(context, appItem.packageName)
                                                    activeFolderToShow = null
                                                },
                                                onLongClick = { isItemMenuOpen = true }
                                            )
                                            .padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (appInfo?.icon != null) {
                                            val bitmap = remember(appInfo.icon) {
                                                appInfo.icon.toBitmap().asImageBitmap()
                                            }
                                            Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(44.dp))
                                        } else {
                                            Box(modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = appItem.label,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        DropdownMenu(
                                            expanded = isItemMenuOpen,
                                            onDismissRequest = { isItemMenuOpen = false },
                                            modifier = Modifier.background(Color(0xFF0F172A))
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("移出資料夾", color = Color.White) },
                                                leadingIcon = { Icon(Icons.Default.FolderOff, contentDescription = null, tint = Color.Red) },
                                                onClick = {
                                                    isItemMenuOpen = false
                                                    viewModel.removeItem(appItem)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                containerColor = Color(0xFF1E293B),
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { activeFolderToShow = null }) {
                        Text("關閉", color = Color.White)
                    }
                }
            )
        }
    }
}

/**
 * Single Desktop Icon Card
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DesktopIconCard(
    item: LauncherItem,
    appInfo: AppInfo?,
    onLaunch: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onLaunch,
                onLongClick = { isMenuExpanded = true }
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (appInfo?.icon != null) {
                val bitmap = remember(appInfo.icon) {
                    appInfo.icon.toBitmap().asImageBitmap()
                }
                Image(
                    bitmap = bitmap,
                    contentDescription = item.label,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            modifier = Modifier.background(Color(0xFF0F172A))
        ) {
            DropdownMenuItem(
                text = { Text("從桌面移除", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                onClick = {
                    isMenuExpanded = false
                    onDelete()
                }
            )
        }
    }
}

/**
 * Folder desktop visualization Card
 */
@Composable
fun FolderGridCard(
    folder: FolderEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = folder.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
