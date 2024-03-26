package com.funny.translation.translate.utils

import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.Log
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.BuildConfig

/**
 * 应用更新时的一次性任务管理器
 */
object AppUpgradeOneTimeJobManager {
    // 版本号 -> 任务列表，存为浮点数是为了在两次版本升级间，开发阶段可以多次加入新任务
    private val jobs: HashMap<Float, ArrayList<SimpleAction>> = hashMapOf()
    private const val DATA_SAVER_KEY = "AppUpgradeOneTimeJobVersion"

    fun executeIfNeeded() {
        val executedVersion = DataSaverUtils.readData(DATA_SAVER_KEY, 0.0f)
        val currentVersion = BuildConfig.VERSION_CODE

        if (executedVersion < currentVersion) {
            var maxVersion = 0.0f
            jobs.forEach { (version: Float, action: ArrayList<SimpleAction>) ->
                if (version > executedVersion) {
                    maxVersion = maxOf(maxVersion, version)
                    Log.d("AppUpgradeOneTimeJobManager", "execute job [version = $version]")
                    action.forEach { it.invoke() }
                }
            }
            DataSaverUtils.saveData(DATA_SAVER_KEY, maxVersion)
        }
    }

    fun addJob(version: Float, action: SimpleAction) {
        if (jobs.containsKey(version)) {
            jobs[version]!!.add(action)
        } else {
            jobs[version] = arrayListOf(action)
        }
    }
}