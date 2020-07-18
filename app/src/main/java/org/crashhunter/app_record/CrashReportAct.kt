package org.crashhunter.app_record

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_crash_report.*
import org.crashhunter.kline.R

class CrashReportAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_report)


        var exception = intent.getStringExtra("crash")

        tvReport.text = exception


    }
}
