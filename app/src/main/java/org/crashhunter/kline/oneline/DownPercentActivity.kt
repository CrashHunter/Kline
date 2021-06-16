package org.crashhunter.kline.oneline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_down_percent.*
import kotlinx.android.synthetic.main.activity_down_percent.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import java.math.BigDecimal
import kotlin.system.measureTimeMillis

class DownPercentActivity : AppCompatActivity() {

    val options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    var currentItemId = R.id.oneDay

    var stringBuilder = SpannableStringBuilder()

    var dailyStringBuilder = SpannableStringBuilder()

    var resultList = ArrayList<DownPerItem>()

    var dailyResultList = ArrayList<DownPerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_down_percent)

        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )

        getAllCoins()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.downpercent_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection

        currentItemId = item.itemId
        routeItem()
        return true
    }

    private fun routeItem() {
        when (currentItemId) {
            R.id.sort -> {
                getCoinsDaily()
            }
            else -> {
            }
        }
    }


    private fun getCoinsDaily() {
        val list = resultList.filter { it.downPer >= BigDecimal(0.7) }
        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
                    for (coin in list) {
                        var n = async {
                            getCoinKlineDailyData(coin)
                        }

                    }
//                    getCoinKlineData("BTCUSDT")
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())

            processDailyData()

            runOnUiThread {
                tvDaily.text = ""
                tvDaily.text = dailyStringBuilder
            }
        }


    }

    private fun processData() {

        resultList.sortByDescending { it.downPer }


        for (index in resultList.indices) {

            val item = resultList[index]

            val max = item.max

            val current = item.current

            val coin = item.coin

            stringBuilder.append("${index + 1}. ")

            stringBuilder.append("$coin $max / $current / ")

            downPerColor(item, stringBuilder)
            if (Constant.ownCoinList.contains(coin.replace("USDT", ""))) {
                stringBuilder.append(" OWN")
            }

            stringBuilder.append("\n \n")
        }
    }

    private fun downPerColor(item: DownPerItem, stringBuilder: SpannableStringBuilder) {
        val downPer = item.downPer
        if (downPer > BigDecimal(0.8)) {
            val span = SpannableStringBuilder("$downPer")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                0,
                downPer.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else if (downPer > BigDecimal(0.6)) {
            val span = SpannableStringBuilder("$downPer")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
                0,
                downPer.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else if (downPer > BigDecimal(0.4)) {
            val span = SpannableStringBuilder("$downPer")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_blue_dark)),
                0,
                downPer.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else {
            stringBuilder.append("$downPer")
        }
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

            processData()

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
            }
        }


    }

    private fun processDailyData() {

        dailyResultList.sortBy { it.rateInc }


        for (index in dailyResultList.indices) {

            val item = dailyResultList[index]

            val current = item.current
            val rate = item.rateInc
            val coin = item.coin

            dailyStringBuilder.append("${index + 1}. ")

            dailyStringBuilder.append("$coin $current / ")

            dailyStringBuilder.append("$rate / ")

            downPerColor(item, dailyStringBuilder)

            dailyStringBuilder.append("\n \n")
        }
    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        runOnUiThread {
            tvTitle.text = "Loading... $coin "
        }

        try {
            var list = syncRequestClient.getSPOTCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                12
            )
            Log.d("sss", "showData:$coin")

            var max = BigDecimal.ZERO
            for (index in list.indices) {
                if (index == 0) {
                    continue
                }
                if (list.get(index).high > max) {
                    max = list.get(index).high
                }
            }

            var current = list[list.size - 1].close
            var downPer = BigDecimal.ONE.subtract(current.divide(max, 4, BigDecimal.ROUND_HALF_UP))
                .setScale(4, BigDecimal.ROUND_HALF_UP)

            var downPerItem = DownPerItem()
            downPerItem.coin = coin
            downPerItem.current = current
            downPerItem.max = max
            downPerItem.downPer = downPer
            resultList.add(downPerItem)


            Constant.downPerItemList = resultList
            return list
        } catch (e: Exception) {
            Log.e("sss", Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


    private fun getCoinKlineDailyData(coin: DownPerItem): List<Candlestick> {
        runOnUiThread {
            tvDaily.text = "Loading... $coin "
        }

        try {
            var list = syncRequestClient.getSPOTCandlestick(
                coin.coin,
                CandlestickInterval.DAILY,
                null,
                null,
                2
            )
            Log.d("sss", "showData:$coin")

            var open = list[0].open
            var close = list[0].close
            var diff = close.minus(open)

            var rateInc = diff.divide(open, 6, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)


            var downPerItem = DownPerItem()
            downPerItem.coin = coin.coin
            downPerItem.current = close
            downPerItem.rateInc = rateInc
            downPerItem.downPer = coin.downPer
            dailyResultList.add(downPerItem)


            return list


        } catch (e: Exception) {
            Log.e("sss", Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


}
