package org.crashhunter.kline.oneline

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.custom.AvgPriceItem
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import com.binance.client.model.trade.MyTrade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_down_percent.*
import kotlinx.android.synthetic.main.activity_down_percent.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import org.crashhunter.kline.data.BaseSharedPreference
import org.crashhunter.kline.data.LATESTAVGPRICEITEMLISTJSONSTR
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

    var roiStringBuilder = SpannableStringBuilder()

    var resultList = ArrayList<DownPerItem>()

    var dailyResultList = ArrayList<DownPerItem>()

    var avgPriceItemList: List<AvgPriceItem> = ArrayList<AvgPriceItem>()
    var avgList = ArrayList<AvgPriceItem>()

    var totalSum = BigDecimal.ZERO
    var totalWin = BigDecimal.ZERO

    private var latestAvgPriceItemListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATESTAVGPRICEITEMLISTJSONSTR,
        ""
    )


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

    private fun getSPOTAccountTrades(coin: String): List<MyTrade> {

        runOnUiThread {

            tvRoi.text = "loading $coin"

        }
        try {
            var list = syncRequestClient.getSPOTAccountTrades(
                coin,
                null,
                null,
                null,
                null
            )
            Log.d(
                "Trades",
                "getSPOTAccountTrades:------------------------------------------ $coin"
            )
            var sum = BigDecimal.ZERO
            var holdNum = BigDecimal.ZERO
            //获取当前持有数
            for (item in Constant.ownCoinList) {
                if (coin.equals(item.asset + "USDT")) {
                    holdNum = BigDecimal(item.free)
                    Log.d(
                        "Trades",
                        "$coin holdNum:$holdNum"
                    )
                    break
                }
            }
            list.sortByDescending { it.time }

            var tempHoldNum = BigDecimal.ZERO
            for (item in list) {
                if (item.isBuyer) {
                    tempHoldNum += item.qty
                    sum += item.quoteQty
                } else {
                    tempHoldNum -= item.qty
                    sum -= item.quoteQty
                }

                val date = Date(item.time)
                var format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                var tradeTime = format.format(date)
                Log.d(
                    "Trades",
                    "$coin: ${item.isBuyer} price:${item.price} qty:${item.qty} quoteQty:${item.quoteQty} $tradeTime"
                )

                //找到最近满足持仓的记录
                if (tempHoldNum >= holdNum) {
                    break
                }

            }


            if (holdNum != BigDecimal.ZERO) {
                var avgPrice = BigDecimal.ZERO
                if (sum > BigDecimal.ZERO) {
                    avgPrice = sum / holdNum
                } else {
                    avgPrice = BigDecimal.ZERO
                }
                Log.d("Trades", "$coin: sum:$sum holdNum:$holdNum avgPrice:${avgPrice} ")

                var avgPriceItem = AvgPriceItem()
                avgPriceItem.coin = coin
                avgPriceItem.avgPrice = avgPrice
                avgPriceItem.sumBuy = sum
                avgPriceItem.holdNum = holdNum

                avgList.add(avgPriceItem)
            } else {
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
            R.id.ROI -> {

//                getAllCoinsAvg()
                startActivity(Intent(this,ROIActivity::class.java))

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
            if (Constant.ownCoinListName.contains(coin.replace("USDT", ""))) {
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
            if (Constant.ownCoinListName.contains(coin.replace("USDT", ""))) {
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
        avgPriceItemList = ArrayList<AvgPriceItem>()
        avgList = ArrayList<AvgPriceItem>()
        if (Constant.ownCoinListName.isEmpty()) {
            Toast.makeText(applicationContext, "no ownCoinListName", Toast.LENGTH_LONG).show()
            return
        }

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
//                    getSPOTAccountTrades("SXPUSDT")
                    for (coin in Constant.coinList) {
                        if (Constant.ownCoinListName.contains(coin.replace("USDT", ""))) {
                            Thread.sleep(200)
                            if (coin.contains("SHIB")) {
                                getSPOTAccountTrades("SHIBUSDT")
                            } else {
                                getSPOTAccountTrades(coin)
                            }
                        }
                    }
                }
            }

            avgPriceItemList = avgList

            var jsonStr = Gson().toJson(avgPriceItemList)
            latestAvgPriceItemListJsonStr = jsonStr

            processROIData()

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }

    }


    private fun processROIData() {

        var list = avgPriceItemList.sortedBy { it.roi }
        roiStringBuilder.clear()
        var totalSum = BigDecimal.ZERO
        var totalWin = BigDecimal.ZERO

        for (index in list.indices) {

            val item = list[index]

            totalSum += item.sumBuy

            val avgPrice = item.avgPrice
            var currentPrice = BigDecimal.ZERO

            val coin = item.coin
            for (downPerItem in resultList) {
                if (downPerItem.coin.equals(coin)) {
                    currentPrice = downPerItem.current
                    break
                }
            }
            if (avgPrice <= BigDecimal.ZERO) {
                //optimize
                item.roi = BigDecimal(100)
            } else if (currentPrice >= avgPrice) {
                item.roi = currentPrice / avgPrice
            } else {
                item.roi = -(BigDecimal.ONE.minus(currentPrice / avgPrice)).setScale(
                    4,
                    BigDecimal.ROUND_HALF_UP
                )
            }
            val roi = item.roi

            var win = (currentPrice - avgPrice) * item.holdNum
            totalWin += win

            roiStringBuilder.append("${index + 1}. ")

            roiStringBuilder.append("$coin $currentPrice / $avgPrice /")

            //roiStringBuilder.append("$roi / ")

            ROIColor(roi, roiStringBuilder)

            roiStringBuilder.append("\n")
        }
        var totalROI = BigDecimal.ZERO
        if (totalSum > BigDecimal.ZERO) {
            totalROI = totalWin / totalSum
        }
        roiStringBuilder.append("totalSum:$totalSum \ntotalWin:$totalWin \ntotalROI:${totalROI} \n ")
    }

    private fun ROIColor(roi: BigDecimal, stringBuilder: SpannableStringBuilder) {
        if (roi > BigDecimal(0)) {
            val span = SpannableStringBuilder("$roi")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                0,
                roi.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else {
            stringBuilder.append("$roi")
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
