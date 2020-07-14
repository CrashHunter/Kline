package org.crashhunter.kline

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval


class KeyLineActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)

        initAction()

        object : Thread() {
            override fun run() {
                super.run()
                val options = RequestOptions()
                val syncRequestClient = SyncRequestClient.create(
                    PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                    options
                )
                Log.d(
                    "sss",
                    syncRequestClient.getCandlestick(
                        "BTCUSDT",
                        CandlestickInterval.THREE_DAILY,
                        null,
                        null,
                        5
                    ).toString()
                )
            }
        }.start()


    }


    private fun initAction() {


    }


}

