package com.example.data

import kotlinx.coroutines.flow.Flow

class LauncherRepository(private val launcherDao: LauncherDao) {

    val allLauncherItems: Flow<List<LauncherItem>> = launcherDao.getAllLauncherItems()
    val allFolders: Flow<List<FolderEntity>> = launcherDao.getAllFolders()
    val widgetConfigs: Flow<List<WidgetConfig>> = launcherDao.getWidgetConfigs()

    suspend fun insertItem(item: LauncherItem): Long {
        return launcherDao.insertItem(item)
    }

    suspend fun insertItems(items: List<LauncherItem>) {
        launcherDao.insertItems(items)
    }

    suspend fun updateItem(item: LauncherItem) {
        launcherDao.updateItem(item)
    }

    suspend fun deleteItem(item: LauncherItem) {
        launcherDao.deleteItem(item)
    }

    suspend fun deleteItemByPackage(packageName: String) {
        launcherDao.deleteItemByPackageName(packageName)
    }

    suspend fun insertFolder(folder: FolderEntity): Long {
        return launcherDao.insertFolder(folder)
    }

    suspend fun deleteFolder(folder: FolderEntity) {
        launcherDao.deleteFolder(folder)
    }

    suspend fun insertWidgetConfig(config: WidgetConfig) {
        launcherDao.insertWidgetConfig(config)
    }

    suspend fun insertWidgetConfigs(configs: List<WidgetConfig>) {
        launcherDao.insertWidgetConfigs(configs)
    }
}
