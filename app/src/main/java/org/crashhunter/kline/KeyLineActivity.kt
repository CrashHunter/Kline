package org.crashhunter.kline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_key_line.*
import org.crashhunter.kline.data.SharedPreferenceUtil
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class KeyLineActivity : AppCompatActivity() {

    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )
    var stringBuilder = SpannableStringBuilder()

    var candlestickInterval = CandlestickInterval.DAILY

    var candlestickIntervalList = ArrayList<CandlestickInterval>()

    var refresh = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_key_line)

//        candlestickIntervalList.add(CandlestickInterval.HOURLY)
//        candlestickIntervalList.add(CandlestickInterval.FOUR_HOURLY)
//        candlestickIntervalList.add(CandlestickInterval.TWELVE_HOURLY)
        candlestickIntervalList.add(CandlestickInterval.DAILY)
        candlestickIntervalList.add(CandlestickInterval.THREE_DAILY)

        initAction()

        getData()


    }

    private fun getData() {
        object : Thread() {
            override fun run() {
                super.run()

                //                candlestickInterval = CandlestickInterval.FOUR_HOURLY
                //                stringBuilder.append("${candlestickInterval.name}: \n")
                //                getAllCoins()
                //                addDivideLine()
                //
                //                candlestickInterval = CandlestickInterval.TWELVE_HOURLY
                //                stringBuilder.append("${candlestickInterval.name}: \n")
                //                getAllCoins()
                //                addDivideLine()
                //
                //                candlestickInterval = CandlestickInterval.DAILY
                //                stringBuilder.append("${candlestickInterval.name}: \n")
                //                getAllCoins()
                //                addDivideLine()
                //
                //                candlestickInterval = CandlestickInterval.THREE_DAILY
                //                stringBuilder.append("${candlestickInterval.name}: \n")
                //                getAllCoins()
                //                addDivideLine()

                getAllCoins()

                runOnUiThread {
                    tvTitle.text = ""
                    tvTitle.text = stringBuilder
                    refresh = false
                }
            }
        }.start()
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


    private fun getAllCoins() {
        getCoinInfo("ADAUSDT")
        getCoinInfo("ALGOUSDT")
        getCoinInfo("ATOMUSDT")
        getCoinInfo("BATUSDT")
        getCoinInfo("BCHUSDT")
        getCoinInfo("BNBUSDT")
        getCoinInfo("BTCUSDT")
        getCoinInfo("COMPUSDT")
        getCoinInfo("DASHUSDT")
        getCoinInfo("DOGEUSDT")
        getCoinInfo("EOSUSDT")
        getCoinInfo("ETCUSDT")
        getCoinInfo("ETHUSDT")
        getCoinInfo("IOSTUSDT")
        getCoinInfo("IOTAUSDT")
        getCoinInfo("KNCUSDT")
        getCoinInfo("LINKUSDT")
        getCoinInfo("LTCUSDT")
        getCoinInfo("NEOUSDT")
        getCoinInfo("OMGUSDT")
        getCoinInfo("ONTUSDT")
        getCoinInfo("QTUMUSDT")
        getCoinInfo("THETAUSDT")
        getCoinInfo("TRXUSDT")
        getCoinInfo("VETUSDT")
        getCoinInfo("XLMUSDT")
        getCoinInfo("XMRUSDT")
        getCoinInfo("XRPUSDT")
        getCoinInfo("XTZUSDT")
        getCoinInfo("ZECUSDT")
        getCoinInfo("ZILUSDT")
        getCoinInfo("ZRXUSDT")
    }

    private fun getCoinInfo(coin: String) {


        for (item in candlestickIntervalList) {
            candlestickInterval = item
            var jsonList =
                SharedPreferenceUtil.loadData(
                    AppController.instance.applicationContext,
                    "KeyLine-${coin}-$candlestickInterval"
                )

            if (jsonList.isNotEmpty() && !refresh) {
                var list = JSON.parseArray(jsonList, Candlestick::class.java)
//
//                var endDay = list[list.size - 1]
//                val date = Date(endDay.openTime.toLong())
//                val format = SimpleDateFormat("yyyy.MM.dd")
//                var spDayStr = format.format(date)
//
//                val currentDay = Date(getTodayStartTime())
//                var currentDayStr = format.format(currentDay)
//
//
//                if (spDayStr == currentDayStr) {
//                    parseKLineData(coin, list)
//                } else {
//                    var list = getCoinKlineData(coin)
//                    parseKLineData(coin, list)
//                }

                parseKLineData(coin, list)


            } else {
                var list = getCoinKlineData(coin)
                parseKLineData(coin, list)
            }
        }

        addDivideLine()


    }

    fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        return calendar.time.time
    }

    private fun parseKLineData(
        coin: String,
        list: List<Candlestick>
    ) {
        stringBuilder.append("$coin ${candlestickInterval.name}: \n")

        for (item in list) {
            val date = Date(item.openTime.toLong())
            val format = SimpleDateFormat("MM.dd HH")
            var day = format.format(date)
            var str = "${day} open:${item.open} close:${item.close} "


            var open = item.open
            var close = item.close

            var rate = close.divide(open, 5, BigDecimal.ROUND_HALF_UP)
            var divide = (rate.minus(BigDecimal.ONE)) * BigDecimal(100)

            var divideRate = "  $divide%"
            var rateSpan = SpannableStringBuilder(divideRate)

//            if (divide > BigDecimal.ZERO) {
//                rateSpan.setSpan(
//                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
//                    0,
//                    divideRate.length - 1,
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                )
//            } else {
//                rateSpan.setSpan(
//                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
//                    0,
//                    divideRate.length - 1,
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                )
//            }

//            if (divide < BigDecimal.ONE && divide > -BigDecimal.ONE) {
//                rateSpan.setSpan(
//                    ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
//                    0,
//                    divideRate.length - 1,
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                )
//            }

            if (divide < BigDecimal(0.3) && divide > -BigDecimal(0.3)) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_purple)),
                    0,
                    divideRate.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
//                stringBuilder.append(str)
//                stringBuilder.append(rateSpan)
//                stringBuilder.append("\n")
            }

            stringBuilder.append(str)
            stringBuilder.append(rateSpan)
            stringBuilder.append("\n")
        }
    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        var list = syncRequestClient.getCandlestick(
            coin,
            candlestickInterval,
            null,
            null,
            5
        )
        Log.d(
            "sss",
            list.toString()
        )
        SharedPreferenceUtil.saveData(
            AppController.instance.applicationContext,
            "KeyLine-${coin}-$candlestickInterval",
            JSON.toJSONString(list)
        )
        return list
    }


    private fun initAction() {
        btnRefresh?.setOnClickListener {

            refresh = true
            tvTitle.text = "Loading..."
            getData()
        }

    }


}

