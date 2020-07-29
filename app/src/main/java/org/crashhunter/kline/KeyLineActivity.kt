package org.crashhunter.kline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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


class KeyLineActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

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

    var purplePoint = 0.3
    var redPoint = 0.1

    val historyRange = 2

    var currentItemId = R.id.refresh

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_key_line)

        swipeRefresh.setOnRefreshListener(this)

        initAction()

        getAllInterval()


    }

    override fun onRefresh() {

        forceRefresh = true
        routeItem()

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

        currentItemId = item.itemId
        routeItem()
        return true
    }

    private fun routeItem() {
        when (currentItemId) {
            R.id.sixH -> {
                header.text = "SIX_HOURLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.SIX_HOURLY)

                getData()
            }
            R.id.twelveH -> {
                header.text = "TWELVE_HOURLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.TWELVE_HOURLY)

                getData()
            }
            R.id.oneDay -> {
                header.text = "DAILY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.DAILY)

                getData()
            }
            R.id.threeDay -> {
                header.text = "THREE_DAILY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.THREE_DAILY)

                getData()
            }
            R.id.oneWeek -> {
                header.text = "WEEKLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.WEEKLY)

                getData()
            }
            R.id.all -> {
                header.text = "All"
                candlestickIntervalList.clear()
                getAllInterval()
            }

            else -> {
            }
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
                    swipeRefresh.isRefreshing = false
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
        getCoinInfo("BANDUSDT")
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
        getCoinInfo("KAVAUSDT")
        getCoinInfo("KNCUSDT")
        getCoinInfo("LENDUSDT")
        getCoinInfo("LINKUSDT")
        getCoinInfo("LTCUSDT")
        getCoinInfo("NEOUSDT")
        getCoinInfo("OMGUSDT")
        getCoinInfo("ONTUSDT")
        getCoinInfo("QTUMUSDT")
        getCoinInfo("SXPUSDT")
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

                parseKLineData(coin, list, candlestickInterval)


            } else {
                var list = getCoinKlineData(coin)
                parseKLineData(coin, list, candlestickInterval)
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
        list: List<Candlestick>,
        candlestickInterval: CandlestickInterval
    ) {

        setPoint(candlestickInterval)


        var itemStr = SpannableStringBuilder()
        for (index in list.indices) {

            if (index == list.size - 1) {
                break
            }
            var item = list[index]

            val date = Date(item.closeTime.toLong())
            val format = SimpleDateFormat("MM.dd HH:mm")
            var day = format.format(date)

            var open = item.open
            var close = item.close
            var diff = close.minus(open)

            var str = "${day} open:${item.open} close:${item.close} diff:${diff}"


            // 默认行业是涨幅计算公式是=（今收-昨收）/昨收  但我是(close-open)/open 来计算单个k柱的涨跌幅  当前一个close和当前open有偏差时会不一样

            var divide = diff.divide(open, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)


            var divideRate = "  $divide%"
            var rateSpan = SpannableStringBuilder(divideRate)

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
            stringBuilder.append("$coin ${this.candlestickInterval.name}: \n")
            stringBuilder.append(itemStr)
        }

    }

    private fun setPoint(candlestickInterval: CandlestickInterval) {
        if (candlestickInterval == CandlestickInterval.THREE_DAILY || candlestickInterval == CandlestickInterval.WEEKLY) {
            purplePoint = 1.0
            redPoint = 0.5
        } else {
            purplePoint = 0.5
            redPoint = 0.25
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

