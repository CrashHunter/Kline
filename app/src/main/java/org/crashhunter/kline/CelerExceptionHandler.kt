package org.crashhunter.kline

import android.os.Process
import android.util.Log
import org.crashhunter.app_record.AppRecord

/**
 * Created by CrashHunter on 2020/7/10.
 */

class CelerExceptionHandler : Thread.UncaughtExceptionHandler {
    private val originalDefaultExceptionHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.e(
            "uncaughtExceTag",
            "default exception: \n Thread: ${t.name} \n Cause: ${Log.getStackTraceString(e)}"
        )


        if (BuildConfig.DEBUG) {
            AppRecord.showCrashReport(AppController.instance, t, e)
        }

        if (originalDefaultExceptionHandler != null) {
            // completed exception processing. Invoking default exception handler.
            originalDefaultExceptionHandler.uncaughtException(t, e)
        } else {
            killProcessAndExit()
        }
    }

    private fun killProcessAndExit() {
        try {
            Thread.sleep(SLEEP_TIMEOUT_MS.toLong())
        } catch (e1: InterruptedException) {
            e1.printStackTrace()
        }
        Process.killProcess(Process.myPid())
        System.exit(10)
    }

    companion object {
        private const val TAG = "CelerExceptionHandler"
        private const val SLEEP_TIMEOUT_MS = 400
        private var sInstance: CelerExceptionHandler? = null
        fun init() {
            if (sInstance == null) {
                synchronized(CelerExceptionHandler::class.java) {
                    if (sInstance == null) {
                        sInstance = CelerExceptionHandler()
                    }
                }
            }
        }
    }


}
