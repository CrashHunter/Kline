package org.crashhunter.kline.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by CrashHunter on 2020/3/6.
 */

open class BaseSP {

    companion object {

        private val PREFERENCE_FILE_NAME = "klinesp"

        fun instance(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        }

        fun putString(context: Context, key: String, value: String) {
            val ed = instance(context).edit()
            ed.putString(key, value)
            ed.apply()
        }

        fun getString(context: Context, key: String, defValue: String): String {
            return instance(context).getString(key, defValue)
        }

        fun putInteger(context: Context, key: String, value: Int) {
            val ed = instance(context).edit()
            ed.putInt(key, value)
            ed.apply()
        }

        fun getInteger(context: Context, key: String, defValue: Int): Int {
            return instance(context).getInt(key, defValue)
        }

        fun putBoolean(context: Context, key: String, value: Boolean) {
            val ed = instance(context).edit()
            ed.putBoolean(key, value)
            ed.apply()
        }

        fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
            return instance(context).getBoolean(key, defValue)
        }

        fun remove(context: Context, key: String) {
            val ed = instance(context).edit()
            ed.remove(key)
            ed.apply()
        }
    }
}
