package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LauncherDao {
    @Query("SELECT * FROM launcher_items")
    fun getAllLauncherItems(): Flow<List<LauncherItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LauncherItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LauncherItem>)

    @Update
    suspend fun updateItem(item: LauncherItem)

    @Delete
    suspend fun deleteItem(item: LauncherItem)

    @Query("DELETE FROM launcher_items WHERE packageName = :packageName")
    suspend fun deleteItemByPackageName(packageName: String)

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("SELECT * FROM widget_configs ORDER BY orderIndex ASC")
    fun getWidgetConfigs(): Flow<List<WidgetConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfig(config: WidgetConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidgetConfigs(configs: List<WidgetConfig>)
}
