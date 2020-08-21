package org.crashhunter.kline.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class KeyLineService : Service() {

    override fun onBind(intent: Intent): IBinder? {

        return null

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

        Log.d("service","onStartCommand")
    }

}
