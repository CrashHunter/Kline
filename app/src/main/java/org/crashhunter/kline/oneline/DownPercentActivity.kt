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
import com.binance.client.model.trade.MyTrade
import kotlinx.android.synthetic.main.activity_down_percent.*
import kotlinx.android.synthetic.main.activity_down_percent.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
        getAllCoinsAvg()
    }

    private fun getSPOTAccountTrades(coin: String): List<MyTrade> {

        try {
            //没有YEAR的维度，最大到月
            var list = syncRequestClient.getSPOTAccountTrades(
                coin,
                null,
                null,
                null,
                null
            )
            Log.d(
                "Trades",
                "showData getSPOTAccountTrades:------------------------------------------"
            )
            var sum = BigDecimal.ZERO
            var holdNum = BigDecimal.ZERO
            var start = false
            for (item in list) {
                val date = Date(item.time)
                var format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                var tradeTime = format.format(date)
//                Log.d(
//                    "Trades",
//                    "$coin: ${item.isBuyer} ${item.price} ${item.qty} ${item.quoteQty} $tradeTime"
//                )

                if (!start && !item.isBuyer) {
                    continue
                } else {
                    start = true
                }

                if (item.isBuyer) {
                    holdNum += item.qty
                    sum += item.quoteQty
                } else {
                    holdNum -= item.qty
                    sum -= item.quoteQty
                }
            }
            if (holdNum > BigDecimal.ZERO){
                var avgPrice = sum / holdNum
                Log.d("Trades", "$coin: $sum $holdNum ${avgPrice} ")
            }else{
                Log.d("Trades", "EMPTY")
            }

            return list
        } catch (e: Exception) {
            Log.e("Trades", "$coin: " + Log.getStackTraceString(e))
        }
        return ArrayList<MyTrade>(0)
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
            R.id.Daily -> {
                getCoinsInterval(CandlestickInterval.DAILY)
            }
            R.id.Week -> {
                getCoinsInterval(CandlestickInterval.WEEKLY)
            }
            R.id.Month -> {
                getCoinsInterval(CandlestickInterval.MONTHLY)
            }
            R.id.UpPer -> {
                processUpData()

                runOnUiThread {
                    tvTitle.text = ""
                    tvTitle.text = stringBuilder
                }
            }
            R.id.DownPer -> {
                processDownData()

                runOnUiThread {
                    tvTitle.text = ""
                    tvTitle.text = stringBuilder
                }
            }
            else -> {
            }
        }
    }


    private fun getCoinsInterval(interval: CandlestickInterval) {

        var sortLine = tvRate.text.toString()

        val list = resultList.filter { it.downPer >= BigDecimal(sortLine) }
        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
                    for (coin in list) {
                        var n = async {
                            getCoinKlineIntervalData(coin, interval)
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

    private fun processUpData() {
        stringBuilder.clear()
        resultList.sortBy { it.upPer }


        for (index in resultList.indices) {

            val item = resultList[index]

            val min = item.min

            val current = item.current

            val coin = item.coin

            stringBuilder.append("${index + 1}. ")

            stringBuilder.append("$coin $min / $current / ")

            upPerColor(item, stringBuilder)
            if (Constant.ownCoinList.contains(coin.replace("USDT", ""))) {
                stringBuilder.append(" OWN")
            }

            stringBuilder.append("\n \n")
        }
    }

    private fun upPerColor(item: DownPerItem, stringBuilder: SpannableStringBuilder) {
        val upPer = item.upPer
//        if (downPer > BigDecimal(0.8)) {
//            val span = SpannableStringBuilder("$downPer")
//            span.setSpan(
//                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
//                0,
//                downPer.toString().length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//            stringBuilder.append(span)
//        } else if (downPer > BigDecimal(0.6)) {
//            val span = SpannableStringBuilder("$downPer")
//            span.setSpan(
//                ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
//                0,
//                downPer.toString().length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//            stringBuilder.append(span)
//        } else if (downPer > BigDecimal(0.4)) {
//            val span = SpannableStringBuilder("$downPer")
//            span.setSpan(
//                ForegroundColorSpan(getColor(android.R.color.holo_blue_dark)),
//                0,
//                downPer.toString().length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//            stringBuilder.append(span)
//        } else {
//            stringBuilder.append("$downPer")
//        }

        stringBuilder.append("$upPer")
    }

    private fun processDownData() {
        stringBuilder.clear()
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

    private fun getAllCoinsAvg() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    getSPOTAccountTrades("BTCUSDT")
                    for (coin in Constant.coinList) {
                        if (coin.contains("SHIB")) {
                            getSPOTAccountTrades("SHIBUSDT")
                        } else {
                            getSPOTAccountTrades(coin)
                        }

                    }
                }
            }
        }

    }

    private fun getAllCoins() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
                    for (coin in Constant.coinList) {
                        var n = async {

                            if (coin.contains("SHIB")) {
                                getCoinKlineData("SHIBUSDT")
                            } else {
                                getCoinKlineData(coin)
                            }

                        }

                    }
//                    getCoinKlineData("BTCUSDT")
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())

            processDownData()

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
            }
        }


    }

    private fun processDailyData() {

        dailyResultList.sortBy { it.rateInc }
        dailyStringBuilder.clear()

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
            //没有YEAR的维度，最大到月
            var list = syncRequestClient.getSPOTCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                36
            )
            Log.d("sss", "showData:$coin")

            var max = BigDecimal.ZERO
            var min = BigDecimal(9999999999)
            for (index in list.indices) {
                if (index == 0) {
                    continue
                }
                if (list.get(index).high > max) {
                    max = list.get(index).high
                }

                if (list.get(index).low < min
                    && list.get(index).low > BigDecimal.ZERO
                    && list.get(index).low != BigDecimal(0.0001)
                ) {
                    min = list.get(index).low
                }
            }

            var current = list[list.size - 1].close
            var downPer = BigDecimal.ONE.subtract(current.divide(max, 4, BigDecimal.ROUND_HALF_UP))
                .setScale(4, BigDecimal.ROUND_HALF_UP)

            var upPer = current.divide(min, 4, BigDecimal.ROUND_HALF_UP)


            var downPerItem = DownPerItem()
            downPerItem.coin = coin
            downPerItem.current = current
            downPerItem.max = max
            downPerItem.min = min
            downPerItem.downPer = downPer
            downPerItem.upPer = upPer
            resultList.add(downPerItem)


            Constant.downPerItemList = resultList
            return list
        } catch (e: Exception) {
            Log.e("sss", "$coin: " + Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


    private fun getCoinKlineIntervalData(
        coin: DownPerItem,
        interval: CandlestickInterval
    ): List<Candlestick> {
        dailyResultList.clear()
        runOnUiThread {
            tvDaily.text = "Loading... ${coin.coin} $interval"
        }

        try {
            var list = syncRequestClient.getSPOTCandlestick(
                coin.coin,
                interval,
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
