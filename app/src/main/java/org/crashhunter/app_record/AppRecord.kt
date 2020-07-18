package org.crashhunter.app_record

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by CrashHunter on 2020/4/13.
 */
object AppRecord {

    fun showCrashReport(context: Context, thread: Thread, e: Throwable) {

        var exceptionStr = " Thread: ${thread.name} \n Cause: ${Log.getStackTraceString(e)}"

        var intent = Intent(context, CrashReportAct::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("crash", exceptionStr)

        context.startActivity(intent)

    }


}