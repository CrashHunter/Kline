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
    var isCoinInFilter = false

    var purplePointBase = 0.5
    var redPointBase = 0.25
    var rate = 1

    var historyRange = 2

    var currentItemId = R.id.all

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
        candlestickIntervalList.add(CandlestickInterval.HOURLY)
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
            R.id.oneM -> {
                header.text = "ONE_MINUTE"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.ONE_MINUTE)

                getData()
            }
            R.id.oneH -> {
                header.text = "ONE_HOURLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.HOURLY)

                getData()
            }
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
        getCoinInfo("MKRUSDT")
        getCoinInfo("NEOUSDT")
        getCoinInfo("OMGUSDT")
        getCoinInfo("ONTUSDT")
        getCoinInfo("QTUMUSDT")
        getCoinInfo("RLCUSDT")
        getCoinInfo("SNXUSDT")
        getCoinInfo("SXPUSDT")
        getCoinInfo("THETAUSDT")
        getCoinInfo("TRXUSDT")
        getCoinInfo("VETUSDT")
        getCoinInfo("WAVESUSDT")
        getCoinInfo("XLMUSDT")
        getCoinInfo("XMRUSDT")
        getCoinInfo("XRPUSDT")
        getCoinInfo("XTZUSDT")
        getCoinInfo("ZECUSDT")
        getCoinInfo("ZILUSDT")
        getCoinInfo("ZRXUSDT")
    }

    private fun getCoinInfo(coin: String) {
        isCoinInFilter = false
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

                parseKLineData(coin, list, candlestickInterval)


            } else {
                var list = getCoinKlineData(coin)
                parseKLineData(coin, list, candlestickInterval)
            }
        }

        if (isCoinInFilter) {
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


        var itemStr = SpannableStringBuilder()
        for (index in list.indices) {
            var isLineInFilter = false

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

            var purplePoint = purplePointBase * rate
            var redPoint = redPointBase * rate

            if (divide < BigDecimal(purplePoint) && divide > -BigDecimal(purplePoint) && candlestickInterval != CandlestickInterval.HOURLY) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_purple)),
                    0,
                    divideRate.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                isCoinInFilter = true
                isLineInFilter = true
            }

            if (divide < BigDecimal(redPoint) && divide > -BigDecimal(redPoint)) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    divideRate.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                isCoinInFilter = true
                isLineInFilter = true
            }

            if (!isLineInFilter) {
                continue
            }
            itemStr.append(str)
            itemStr.append(rateSpan)
            if (index == list.size - 2) {
                var tagSpan = setTextColor(" -- UP TO DATE ", android.R.color.holo_orange_dark)
                itemStr.append(tagSpan)
            }
            itemStr.append("\n")

        }
        if (isCoinInFilter) {
            stringBuilder.append("$coin ${this.candlestickInterval.name}: \n")
            stringBuilder.append(itemStr)
        }

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
                historyRange = 2
            }

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

