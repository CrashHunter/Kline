package org.crashhunter.kline

import androidx.multidex.MultiDexApplication


/**
 * App controller
 */
class AppController : MultiDexApplication() {

    companion object {
        lateinit var instance: AppController
    }
    override fun onCreate() {
        super.onCreate()
        instance = this

        CelerExceptionHandler()
    }
}


