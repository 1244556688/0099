package com.example.ui.launcher

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable? = null
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = LauncherDatabase.getDatabase(context)
    private val repository = LauncherRepository(database.launcherDao())

    // Installed apps cache
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // Observable Flows from DB
    val allLauncherItems: StateFlow<List<LauncherItem>> = repository.allLauncherItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderEntity>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val widgetConfigs: StateFlow<List<WidgetConfig>> = repository.widgetConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamic states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredApps: StateFlow<List<AppInfo>> = combine(_installedApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { it.label.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInstalledApps()
        checkAndPrepopulateDatabase()
    }

    /**
     * Asynchronously loads all installed apps that can be launched.
     */
    fun loadInstalledApps() {
        viewModelScope.launch {
            val appsList = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
                
                resolvedApps.map { info ->
                    val packageName = info.activityInfo.packageName
                    val className = info.activityInfo.name
                    val label = info.loadLabel(pm).toString()
                    val icon = try {
                        info.loadIcon(pm)
                    } catch (e: Exception) {
                        pm.defaultActivityIcon
                    }
                    AppInfo(packageName, className, label, icon)
                }.sortedBy { it.label.lowercase() }
            }
            _installedApps.value = appsList
        }
    }

    /**
     * Checks if Room database is empty. If so, pre-populates default dock items and widget settings.
     */
    private fun checkAndPrepopulateDatabase() {
        viewModelScope.launch {
            // Wait for DB flow to emit or fetch directly
            repository.allLauncherItems.first().let { items ->
                if (items.isEmpty()) {
                    prepopulateDefaults()
                }
            }
            
            repository.widgetConfigs.first().let { configs ->
                if (configs.isEmpty()) {
                    prepopulateWidgets()
                }
            }
        }
    }

    private suspend fun prepopulateDefaults() {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
        val installedPkgs = resolvedApps.map { it.activityInfo.packageName }.toSet()

        // Smart selection of default Dock apps
        val preferredDockPackages = listOf(
            "com.android.chrome", "com.google.android.youtube", 
            "com.android.settings", "com.android.contacts", 
            "com.google.android.dialer", "com.google.android.apps.messaging",
            "com.android.camera", "com.google.android.apps.photos"
        )

        val dockItemsToInsert = mutableListOf<LauncherItem>()
        var dockIndex = 0

        for (pkg in preferredDockPackages) {
            if (installedPkgs.contains(pkg) && dockIndex < 6) {
                val resolved = resolvedApps.firstOrNull { it.activityInfo.packageName == pkg }
                if (resolved != null) {
                    dockItemsToInsert.add(
                        LauncherItem(
                            packageName = pkg,
                            className = resolved.activityInfo.name,
                            label = resolved.loadLabel(pm).toString(),
                            containerType = 1, // DOCK
                            cellX = dockIndex,
                            cellY = 0
                        )
                    )
                    dockIndex++
                }
            }
        }

        // Fallback: If less than 4 dock apps were inserted, just grab the first few launcher apps
        if (dockItemsToInsert.size < 4) {
            val sortedList = resolvedApps.sortedBy { it.loadLabel(pm).toString().lowercase() }
            for (info in sortedList) {
                if (dockItemsToInsert.size >= 5) break
                val pkg = info.activityInfo.packageName
                if (dockItemsToInsert.none { it.packageName == pkg }) {
                    dockItemsToInsert.add(
                        LauncherItem(
                            packageName = pkg,
                            className = info.activityInfo.name,
                            label = info.loadLabel(pm).toString(),
                            containerType = 1,
                            cellX = dockIndex,
                            cellY = 0
                        )
                    )
                    dockIndex++
                }
            }
        }

        repository.insertItems(dockItemsToInsert)
    }

    private suspend fun prepopulateWidgets() {
        val defaultWidgets = listOf(
            WidgetConfig("clock", isVisible = true, orderIndex = 0),
            WidgetConfig("weather", isVisible = true, orderIndex = 1),
            WidgetConfig("system_stats", isVisible = true, orderIndex = 2),
            WidgetConfig("music", isVisible = true, orderIndex = 3),
            WidgetConfig("quick_settings", isVisible = true, orderIndex = 4)
        )
        repository.insertWidgetConfigs(defaultWidgets)
    }

    /**
     * Search apps query modification.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Launch application.
     */
    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Add app to desktop grid.
     */
    fun addAppToDesktop(app: AppInfo, cellX: Int, cellY: Int) {
        viewModelScope.launch {
            val item = LauncherItem(
                packageName = app.packageName,
                className = app.className,
                label = app.label,
                containerType = 0, // Desktop
                cellX = cellX,
                cellY = cellY
            )
            repository.insertItem(item)
        }
    }

    /**
     * Add app to dock toolbar.
     */
    fun addAppToDock(app: AppInfo) {
        viewModelScope.launch {
            val currentDockCount = allLauncherItems.value.filter { it.containerType == 1 }.size
            if (currentDockCount >= 8) return@launch // Max 8 dock apps
            
            val item = LauncherItem(
                packageName = app.packageName,
                className = app.className,
                label = app.label,
                containerType = 1, // Dock
                cellX = currentDockCount,
                cellY = 0
            )
            repository.insertItem(item)
        }
    }

    /**
     * Delete item from launcher.
     */
    fun removeItem(item: LauncherItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    /**
     * Reorders Dock apps list.
     */
    fun updateItemPosition(item: LauncherItem, cellX: Int, cellY: Int) {
        viewModelScope.launch {
            repository.updateItem(item.copy(cellX = cellX, cellY = cellY))
        }
    }

    /**
     * Folder Creation.
     */
    fun createFolder(folderName: String, firstApp: LauncherItem, secondApp: AppInfo) {
        viewModelScope.launch {
            val folderId = repository.insertFolder(FolderEntity(name = folderName)).toInt()
            
            // Move first item into folder
            repository.updateItem(firstApp.copy(containerType = 2, folderId = folderId))
            
            // Insert second item into folder
            repository.insertItem(
                LauncherItem(
                    packageName = secondApp.packageName,
                    className = secondApp.className,
                    label = secondApp.label,
                    containerType = 2,
                    folderId = folderId
                )
            )
        }
    }

    /**
     * Add an app to an existing folder.
     */
    fun addAppToFolder(app: AppInfo, folderId: Int) {
        viewModelScope.launch {
            val item = LauncherItem(
                packageName = app.packageName,
                className = app.className,
                label = app.label,
                containerType = 2,
                folderId = folderId
            )
            repository.insertItem(item)
        }
    }

    /**
     * Moves a desktop app inside a folder.
     */
    fun moveItemToFolder(item: LauncherItem, folderId: Int) {
        viewModelScope.launch {
            repository.updateItem(item.copy(containerType = 2, folderId = folderId))
        }
    }

    /**
     * Toggles Widget visibility preferences.
     */
    fun toggleWidget(id: String, isVisible: Boolean) {
        viewModelScope.launch {
            repository.insertWidgetConfig(WidgetConfig(id = id, isVisible = isVisible))
        }
    }
}
