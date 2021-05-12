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
import java.math.BigDecimal
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
                    for (coin in Constant.coinList) {
                        var n = async {
                            getCoinKlineData(coin)
                        }

                    }
//                    getCoinKlineData("BTCUSDT")
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
            }
        }


    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        runOnUiThread {
            tvTitle.text = "Loading... $coin "
        }

        try {
            var list = syncRequestClient.getCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                12
            )
            Log.d("sss", "showData:$coin")

            var max = BigDecimal.ZERO
            for (item in list) {
                if (item.high > max) {
                    max = item.high
                }
            }

            var current = list[list.size - 1].close

            if (max.divide(BigDecimal(2), 4, BigDecimal.ROUND_HALF_UP) > current) {
                stringBuilder.append(
                    "$coin $max $current ${
                        (1 - current.divide(
                            max,
                            4,
                            BigDecimal.ROUND_HALF_UP
                        ).toDouble()).toString()
                    } \n \n"
                )
            }
            return list
        } catch (e: Exception) {
            Log.e("sss", Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


}