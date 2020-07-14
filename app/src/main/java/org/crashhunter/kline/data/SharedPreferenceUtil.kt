package org.crashhunter.kline.data

import android.content.Context

class SharedPreferenceUtil : BaseSP() {

    companion object {

        fun saveData(context: Context, key: String, value: String) {
            putString(context, key, value)
        }

        fun loadData(context: Context, key: String): String {
            return getString(context, key, "")
        }
    }

}