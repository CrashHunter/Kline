package org.crashhunter.kline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_data_analysis.*
import kotlinx.coroutines.*
import org.crashhunter.kline.data.KeyLineCoin
import org.crashhunter.kline.data.SharedPreferenceUtil
import org.crashhunter.kline.utils.TimeUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis

class DataAnalysisActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    var candlestickIntervalList = ArrayList<CandlestickInterval>()
    var stringBuilder =
            SpannableStringBuilder()
    var candlestickInterval = CandlestickInterval.DAILY
    var purplePointBase = 0.5
    var redPointBase = 0.25
    var rate = 1

    var historyRange = 24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_analysis)


        candlestickIntervalList.add(CandlestickInterval.MONTHLY)

        getData()

    }

    private fun getData() {
        stringBuilder = SpannableStringBuilder()
        tvTitle.text = "Loading..."

        object : Thread() {
            override fun run() {
                super.run()

                getAllCoins()

            }
        }.start()
    }

    private fun getAllCoins() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
//                    var n  = ArrayList<Deferred<Int>>(10)
//                    n.add(Deferred)
                    for (coin in Constant.coinList) {
                        //var n = async {
                        //    getCoinInfo(coin)
                        //}
                        getCoinInfo(coin)
                    }
//                    for(x in n){
//                        amount+=x.await()
//                    }
//                    amount+=x.await()
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())

            stringBuilder.append("Total $total  Win $positive  Rate: ${positive / total.toDouble()}")

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
                forceRefresh = false
                swipeRefresh.isRefreshing = false
            }

        }

    }


    private fun addDivideLine() {
        var divideLine =
                SpannableStringBuilder("------------------------------------------------------------------------\n")
        divideLine.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
                0,
                divideLine.length - 1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        stringBuilder.append(divideLine)
    }


    private fun setTextColor(txt: String, color: Int): SpannableStringBuilder {
        var tagSpan = SpannableStringBuilder(txt)
        tagSpan.setSpan(
                ForegroundColorSpan(getColor(color)),
                0,
                txt.length - 1,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        return tagSpan
    }

    var forceRefresh = false
    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
    )

    private suspend fun getCoinInfo(coin: String) {
//        isCoinInFilter = false

        for (item in candlestickIntervalList) {
            candlestickInterval = item
            setPointAndRange(candlestickInterval)
            runOnUiThread {
                tvTitle.text = "Loading... $coin $candlestickInterval"
            }

            var jsonList =
                    SharedPreferenceUtil.loadData(
                            AppController.instance.applicationContext,
                            "KeyLine-${coin}-$candlestickInterval"
                    )

            if (jsonList.isNotEmpty() && !forceRefresh) {
                var list = JSON.parseArray(jsonList, Candlestick::class.java)

                collectCoinInfo(coin, list, candlestickInterval)


            } else {
                var list = getCoinKlineData(coin)
                collectCoinInfo(coin, list, candlestickInterval)
            }
        }


    }

    var lastestCoinsRange = ArrayList<KeyLineCoin>()
    var openTimeList = ArrayList<Long>()

    private fun collectCoinInfo(
            coin: String,
            list: List<Candlestick>,
            candlestickInterval: CandlestickInterval
    ) {

        var keyLineCoins = ArrayList<KeyLineCoin>()
        for (index in list.indices) {
            var item = list[index]

            val date = Date(item.closeTime.toLong())
            if (candlestickInterval == CandlestickInterval.THREE_DAILY || candlestickInterval == CandlestickInterval.WEEKLY) {
                date.time = item.closeTime
            } else {
                date.time = item.openTime
            }

            var format = SimpleDateFormat("MM.dd HH:mm")
            if (candlestickInterval == CandlestickInterval.ONE_MINUTE) {
                format = SimpleDateFormat("HH:mm")
            } else {
                format = SimpleDateFormat("MM.dd HH:mm")
            }
            var day = format.format(date)

            var open = item.open
            var close = item.close
            var diff = close.minus(open)

            var high = item.high
            var low = item.low
            var rangeDiff = high.minus(low)

            var str = ""

//            if (candlestickInterval == CandlestickInterval.ONE_MINUTE) {
//                str = "${day} O:${item.open} C:${item.close}"
//            } else {
//                str = "${day} O:${item.open} C:${item.close} diff:${diff}"
//            }
            str = "${day} O:${item.open} C:${item.close}"

            // 默认行业是涨幅计算公式是=（今收-昨收）/昨收  但我是(close-open)/open 来计算单个k柱的涨跌幅  当前一个close和当前open有偏差时会不一样

            var rateInc = diff.divide(open, 6, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)
            var rangeInc = rangeDiff.divide(low, 6, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)

//            if (index == list.size - 2) {
            var coinrange = KeyLineCoin()
            coinrange.name = coin
            coinrange.close = item.close
            coinrange.rateInc = rateInc
            coinrange.rangeInc = rangeInc
            coinrange.candlestickInterval = candlestickInterval
            coinrange.openTime = item.openTime
            coinrange.closeTime = item.closeTime
            coinrange.quoteAssetVolume = item.quoteAssetVolume
            coinrange.takerBuyQuoteAssetVolume = item.takerBuyQuoteAssetVolume
            coinrange.takerBuyBaseAssetVolume = item.takerBuyBaseAssetVolume
            lastestCoinsRange.add(coinrange)
//            }
            if (!openTimeList.contains(item.openTime)) {
                openTimeList.add(item.openTime)
            }

            keyLineCoins.add(coinrange);

        }

        getTarget(keyLineCoins)

    }


    var total = 0;
    var positive = 0;
    private fun getTarget(list: ArrayList<KeyLineCoin>) {

        for (index in list.indices) {

            if (index==list.size-1){
                break
            }

            var coin = list[index]

            var rateInc = coin.rateInc

            var ratePrec = "  ${coin.rateInc.setScale(2, RoundingMode.HALF_UP)}%"
            var rangePrec = "  ${coin.rangeInc.setScale(2, RoundingMode.HALF_UP)}%"


            val date = Date(coin.openTime.toLong())
            var format = SimpleDateFormat("MM.dd HH:mm")
            var openTimeStr = format.format(date)

            var itemStr = " ${coin.name} $rangePrec $ratePrec  $openTimeStr\n"
            var purplePoint = purplePointBase * rate
            var redPoint = redPointBase * rate



            if (rateInc < BigDecimal(redPoint) && rateInc > -BigDecimal(redPoint)) {
                var str = setTextColor(
                        "$itemStr",
                        android.R.color.holo_red_light
                )
                stringBuilder.append(str)

                total++

                compareNext(index, list, coin)

            } else if (rateInc < BigDecimal(purplePoint) && rateInc > -BigDecimal(purplePoint) && candlestickInterval != CandlestickInterval.HOURLY) {
                var str = setTextColor(
                        "$itemStr",
                        android.R.color.holo_purple
                )
                stringBuilder.append(str)

                total++

                compareNext(index, list, coin)
            }
        }
    }

    private fun compareNext(index: Int, list: ArrayList<KeyLineCoin>, coin: KeyLineCoin) {
        if (index != list.size - 1) {
            var nextCoin = list[index + 1]
            var nextRateInc = list[index + 1].rateInc
            if (nextRateInc > BigDecimal.ZERO) {
                positive++

                var ratePrec = "  ${nextCoin.rateInc.setScale(2, RoundingMode.HALF_UP)}%"
                var rangePrec = "  ${nextCoin.rangeInc.setScale(2, RoundingMode.HALF_UP)}%"


                val date = Date(nextCoin.openTime.toLong())
                var format = SimpleDateFormat("MM.dd HH:mm")
                var openTimeStr = format.format(date)

                var itemStr = "No.${total} ${coin.name} $rangePrec $ratePrec  $openTimeStr\n"

                stringBuilder.append(itemStr)
            }
        }
    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        TimeUtils.stringToLong("2020-7-27 08:00", "yyyy-MM-dd HH:mm")

        var startTimeLong: Long? = null
        var startTimeStr = startTime.text.toString()
        if (startTimeStr.isNotEmpty() && !startTimeStr.contains("XX")) {
            startTimeLong = TimeUtils.stringToLong(startTimeStr, "yyyy-MM-dd HH:mm")
        }

        try {
            var list = syncRequestClient.getCandlestick(
                    coin,
                    candlestickInterval,
                    startTimeLong,
                    null,
                    historyRange
            )
            Log.d("sss", "showData:$coin")

            //Log.d("sss", list.toString())
            SharedPreferenceUtil.saveData(
                    AppController.instance.applicationContext,
                    "KeyLine-${coin}-$candlestickInterval",
                    JSON.toJSONString(list)
            )
            return list
        } catch (e: Exception) {
            Log.e("sss", e.printStackTrace().toString())
        }
        return ArrayList<Candlestick>(0)
    }

    private fun setPointAndRange(candlestickInterval: CandlestickInterval) {
        purplePointBase = 0.5
        redPointBase = 0.25
        rate = 1
        when (candlestickInterval) {
            CandlestickInterval.ONE_MINUTE -> {
                purplePointBase = 0.03
                redPointBase = 0.01
                historyRange = 60
            }
            CandlestickInterval.HOURLY -> {
                rate = 1
                historyRange = 7
            }
            CandlestickInterval.SIX_HOURLY -> {
                rate = 1
                historyRange = 7
            }
            CandlestickInterval.TWELVE_HOURLY -> {
                rate = 1
                historyRange = 7
            }
            CandlestickInterval.DAILY -> {
                rate = 2
                historyRange = 7
            }
            CandlestickInterval.THREE_DAILY -> {
                rate = 3
                historyRange = 7
            }
            CandlestickInterval.WEEKLY -> {
                rate = 4
                historyRange = 7
            }
            CandlestickInterval.MONTHLY -> {
                rate = 5
                historyRange = 24
            }

        }
    }

    override fun onRefresh() {
        getData()
    }

}