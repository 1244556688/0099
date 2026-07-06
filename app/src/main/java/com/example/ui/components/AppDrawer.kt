package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.FolderEntity
import com.example.ui.launcher.AppInfo
import com.example.ui.theme.GlassTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    isOpen: Boolean,
    onClose: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    apps: List<AppInfo>,
    folders: List<FolderEntity>,
    onLaunchApp: (packageName: String) -> Unit,
    onAddToDesktop: (app: AppInfo) -> Unit,
    onAddToDock: (app: AppInfo) -> Unit,
    onCreateFolder: (folderName: String, firstApp: AppInfo) -> Unit,
    onAddAppToFolder: (app: AppInfo, folderId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var selectedAppForFolder by remember { mutableStateOf<AppInfo?>(null) }
    var showFolderCreationDialog by remember { mutableStateOf(false) }
    var showFolderListDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Full screen blurred overlay for App Drawer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() }
            .testTag("app_drawer_backdrop"),
        contentAlignment = Alignment.Center
    ) {
        // App Drawer Main Board
        GlassCard(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {} // block close click propagation
                .testTag("app_drawer_card"),
            shape = RoundedCornerShape(32.dp),
            blurRadius = 32.dp,
            elevation = 16.dp,
            isDark = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Drag Handle / Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "所有應用程式",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Translucent Search Text Field
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("app_drawer_search_input"),
                    placeholder = { Text("搜尋已安裝的 App...", color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.08f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // App Grid list
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("找不到符合的應用程式", color = GlassTheme.TextSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 74.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(apps) { app ->
                            var isMenuExpanded by remember { mutableStateOf(false) }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { onLaunchApp(app.packageName) },
                                        onLongClick = { isMenuExpanded = true }
                                    )
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (app.icon != null) {
                                        val bitmap = remember(app.icon) {
                                            app.icon.toBitmap().asImageBitmap()
                                        }
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = app.label,
                                            modifier = Modifier.size(48.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = app.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                // Custom interactive contextual menu
                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false },
                                    modifier = Modifier.background(Color(0xFF0F172A))
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("新增至桌面", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.AddHome, contentDescription = null, tint = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            onAddToDesktop(app)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("新增至 Dock", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.AddToPhotos, contentDescription = null, tint = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            onAddToDock(app)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("放入新資料夾", color = Color.White) },
                                        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White) },
                                        onClick = {
                                            isMenuExpanded = false
                                            selectedAppForFolder = app
                                            showFolderCreationDialog = true
                                        }
                                    )
                                    if (folders.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("加入現有資料夾", color = Color.White) },
                                            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White) },
                                            onClick = {
                                                isMenuExpanded = false
                                                selectedAppForFolder = app
                                                showFolderListDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Folder Dialog
    if (showFolderCreationDialog && selectedAppForFolder != null) {
        AlertDialog(
            onDismissRequest = { showFolderCreationDialog = false },
            title = { Text("建立新資料夾", color = Color.White) },
            text = {
                Column {
                    Text("請輸入自訂資料夾名稱：", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassTheme.PrimaryAccent,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = { Text("例如：社群、遊戲...", color = Color.Gray) }
                    )
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        val name = newFolderName.trim().ifBlank { "未命名資料夾" }
                        onCreateFolder(name, selectedAppForFolder!!)
                        newFolderName = ""
                        showFolderCreationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlassTheme.PrimaryAccent)
                ) {
                    Text("建立")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFolderCreationDialog = false }) {
                    Text("取消", color = Color.White)
                }
            }
        )
    }

    // Existing Folder List Dialog
    if (showFolderListDialog && selectedAppForFolder != null) {
        AlertDialog(
            onDismissRequest = { showFolderListDialog = false },
            title = { Text("選擇要加入的資料夾", color = Color.White) },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(folders) { folder ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    onAddAppToFolder(selectedAppForFolder!!, folder.id)
                                    showFolderListDialog = false
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder.name, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF1E293B),
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFolderListDialog = false }) {
                    Text("返回", color = Color.White)
                }
            }
        )
    }
}
