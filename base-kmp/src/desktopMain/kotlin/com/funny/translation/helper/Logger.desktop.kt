package com.funny.translation.helper

import com.funny.translation.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging

actual object Logger {
    private val logDir = CacheManager.baseDir.resolve("logs")

    init {
        // kotlin-logging-to-jul
//        System.setProperty("kotlin-logging-to-jul", "true")
        if (BuildConfig.DEBUG) {
            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "console")
            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
            System.setProperty(org.slf4j.simple.SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss:SSS")
            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_THREAD_NAME_KEY, "false")
            System.setProperty(org.slf4j.simple.SimpleLogger.SHOW_LOG_NAME_KEY, "false")

            logDir.mkdirs()
            // 输出到文件
            System.setProperty(
                "org.slf4j.simpleLogger.logFile",
                logDir.resolve("log_${System.currentTimeMillis()}.log").absolutePath
            )

            // 同时输出到控制台

        } else {
            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "warn")
        }
    }

    // KotlinLogging.logger {} 在控制台看不到输出啊，只好改成最朴素的 System.out.println 了
    private val logger = KotlinLogging.logger {

//        override val name: String = "TranstationLog"
//
//        override fun at(level: Level, marker: Marker?, block: KLoggingEventBuilder.() -> Unit) {
//            KLoggingEventBuilder().apply(block).run {
//                if (BuildConfig.DEBUG) {
//                    println("[$level] $message")
//                }
//            }
//        }
//
//        override fun isLoggingEnabledFor(level: Level, marker: Marker?): Boolean {
//            return true
//        }
    }

    actual fun d(msg: String) = logger.debug { msg }
    actual fun d(tag: String, msg: String) = logger.debug { "$tag $msg" }
    actual fun d(tag: String, msg: String, throwable: Throwable) = logger.debug(throwable) { "$tag $msg" }

    actual fun i(msg: String) = logger.info { msg }
    actual fun i(tag: String, msg: String) = logger.info { "$tag $msg" }
    actual fun i(tag: String, msg: String, throwable: Throwable) = logger.info(throwable) { "$tag $msg" }

    actual fun e(msg: String) = logger.error { msg }
    actual fun e(tag: String, msg: String) = logger.error { "$tag $msg" }
    actual fun e(tag: String, msg: String, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

    actual fun w(msg: String) = logger.warn { msg }
    actual fun w(tag: String, msg: String) = logger.warn { "$tag $msg" }
    actual fun w(tag: String, msg: String, throwable: Throwable) = logger.warn(throwable) { "$tag $msg" }

    actual fun v(msg: String) = logger.trace { msg }
    actual fun v(tag: String, msg: String) = logger.trace { "$tag $msg" }
    actual fun v(tag: String, msg: String, throwable: Throwable) = logger.trace(throwable) { "$tag $msg" }

    actual fun wtf(msg: String) = logger.error { msg }
    actual fun wtf(tag: String, msg: String) = logger.error { "$tag $msg" }
    actual fun wtf(tag: String, msg: String, throwable: Throwable) = logger.error(throwable) { "$tag $msg" }

}