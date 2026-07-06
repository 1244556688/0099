package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launcher_items")
data class LauncherItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val className: String,
    val label: String,
    val containerType: Int, // 0 = Desktop Grid, 1 = Dock, 2 = Folder
    val cellX: Int = 0,
    val cellY: Int = 0,
    val folderId: Int? = null,
    val isCustom: Boolean = false
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "widget_configs")
data class WidgetConfig(
    @PrimaryKey val id: String, // "clock", "weather", "calendar", "music", "battery", "system_stats", "quick_settings"
    val isVisible: Boolean = true,
    val orderIndex: Int = 0
)
