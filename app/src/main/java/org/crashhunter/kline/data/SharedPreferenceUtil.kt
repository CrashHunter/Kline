package org.crashhunter.kline.data

import android.content.Context

class SharedPreferenceUtil : BaseSP() {

    companion object {

        fun saveUserToken(context: Context, key: String, value: String) {
            putString(context, key, value)
        }

        fun loadUserToken(context: Context, key: String): String {
            return getString(context, key, "")
        }
    }

}