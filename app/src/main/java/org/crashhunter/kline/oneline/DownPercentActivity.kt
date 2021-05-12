package org.crashhunter.kline.oneline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_down_percent.*
import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import org.crashhunter.kline.data.SharedPreferenceUtil
import org.crashhunter.kline.utils.TimeUtils
import kotlin.system.measureTimeMillis

class DownPercentActivity : AppCompatActivity() {

    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    var stringBuilder = SpannableStringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_down_percent)

        getAllCoins()
    }


    private fun getAllCoins() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
//                    for (coin in Constant.coinList) {
//                        var n = async {
//                            getCoinInfo(coin)
//                        }
//
//                    }
                    getCoinInfo("BTCUSDT")
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())
        }

        runOnUiThread {
            tvTitle.text = ""
            tvTitle.text = stringBuilder
        }

    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {

        try {
            var list = syncRequestClient.getCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                12
            )
            Log.d("sss", "showData:$coin")

            return list
        } catch (e: Exception) {
            Log.e("sss", Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


    private suspend fun getCoinInfo(coin: String) {
//        isCoinInFilter = false

        runOnUiThread {
            tvTitle.text = "Loading... $coin "
        }

        var list = getCoinKlineData(coin)


    }

}