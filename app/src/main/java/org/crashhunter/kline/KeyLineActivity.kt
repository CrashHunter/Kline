package org.crashhunter.kline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

    var forceRefresh = false
    var addTxt = false

    val purplePoint = 0.3
    val redPoint = 0.1

    val historyRange = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_key_line)
        initAction()

//        candlestickIntervalList.add(CandlestickInterval.HOURLY)
        getAllInterval()


    }

    private fun getAllInterval() {
        candlestickIntervalList.add(CandlestickInterval.SIX_HOURLY)
        candlestickIntervalList.add(CandlestickInterval.TWELVE_HOURLY)
        candlestickIntervalList.add(CandlestickInterval.DAILY)
        candlestickIntervalList.add(CandlestickInterval.THREE_DAILY)
        candlestickIntervalList.add(CandlestickInterval.WEEKLY)
        getData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.keyline_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.sixH -> {
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.SIX_HOURLY)

                getData()
                return true
            }
            R.id.twelveH -> {
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.TWELVE_HOURLY)

                getData()
                return true
            }
            R.id.oneDay -> {
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.DAILY)

                getData()
                return true
            }
            R.id.threeDay -> {
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.THREE_DAILY)

                getData()
                return true
            }
            R.id.oneWeek -> {
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.WEEKLY)

                getData()
                return true
            }
            R.id.refresh -> {
                forceRefresh = true
                candlestickIntervalList.clear()
                getAllInterval()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun getData() {
        stringBuilder = SpannableStringBuilder()
        tvTitle.text = "Loading..."

        object : Thread() {
            override fun run() {
                super.run()

                getAllCoins()

                runOnUiThread {
                    tvTitle.text = ""
                    tvTitle.text = stringBuilder
                    forceRefresh = false
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
        addTxt = false

        for (item in candlestickIntervalList) {
            candlestickInterval = item
            var jsonList =
                SharedPreferenceUtil.loadData(
                    AppController.instance.applicationContext,
                    "KeyLine-${coin}-$candlestickInterval"
                )

            if (jsonList.isNotEmpty() && !forceRefresh) {
                var list = JSON.parseArray(jsonList, Candlestick::class.java)

                parseKLineData(coin, list)


            } else {
                var list = getCoinKlineData(coin)
                parseKLineData(coin, list)
            }
        }

        if (addTxt) {
            addDivideLine()
        }


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

        var itemStr = SpannableStringBuilder()
        for (index in list.indices) {

            if (index == list.size - 1) {
                break
            }
            var item = list[index]

            val date = Date(item.openTime.toLong())
            val format = SimpleDateFormat("MM.dd HH")
            var day = format.format(date)

            var open = item.open
            var close = item.close
            var diff = close.minus(open)

            var str = "${day} open:${item.open} close:${item.close} diff:${diff}"


            // 默认行业是涨幅计算公式是=（今收-昨收）/昨收  但我是(close-open)/open 来计算单个k柱的涨跌幅  当前一个close和当前open有偏差时会不一样

            var divide = diff.divide(open, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)


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


            if (divide < BigDecimal(purplePoint) && divide > -BigDecimal(purplePoint)) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_purple)),
                    0,
                    divideRate.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                addTxt = true
            }

            if (divide < BigDecimal(redPoint) && divide > -BigDecimal(redPoint)) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    divideRate.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                addTxt = true
            }

            itemStr.append(str)
            itemStr.append(rateSpan)
            itemStr.append("\n")

        }
        if (addTxt) {
            stringBuilder.append("$coin ${candlestickInterval.name}: \n")
            stringBuilder.append(itemStr)
        }

    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        var list = syncRequestClient.getCandlestick(
            coin,
            candlestickInterval,
            null,
            null,
            historyRange
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

    }


}

