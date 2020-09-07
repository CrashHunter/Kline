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
import org.crashhunter.kline.data.KeyLineCoin
import org.crashhunter.kline.data.SharedPreferenceUtil
import org.crashhunter.kline.utils.TimeUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


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

    var lastestCoinsRange = ArrayList<KeyLineCoin>()


    var openTimeList = ArrayList<Long>()


    var coinList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_key_line)

        swipeRefresh.setOnRefreshListener(this)

        initAction()

        setCoinList()

        getAllInterval()


    }

    private fun setCoinList() {

        coinList.add("ADAUSDT")
        coinList.add("ALGOUSDT")
        coinList.add("ATOMUSDT")
        coinList.add("BALUSDT")
        coinList.add("BANDUSDT")
        coinList.add("BATUSDT")
        coinList.add("BCHUSDT")
        coinList.add("BNBUSDT")
        coinList.add("BTCUSDT")
        coinList.add("BZRXUSDT")
        coinList.add("COMPUSDT")
        coinList.add("CRVUSDT")
        coinList.add("DASHUSDT")
        coinList.add("DOGEUSDT")
        coinList.add("DOTUSDT")
        coinList.add("EOSUSDT")
        coinList.add("ETCUSDT")
        coinList.add("ETHUSDT")
        coinList.add("IOSTUSDT")
        coinList.add("IOTAUSDT")
        coinList.add("KAVAUSDT")
        coinList.add("KNCUSDT")
        coinList.add("LENDUSDT")
        coinList.add("LINKUSDT")
        coinList.add("LTCUSDT")
        coinList.add("MKRUSDT")
        coinList.add("NEOUSDT")
        coinList.add("OMGUSDT")
        coinList.add("ONTUSDT")
        coinList.add("QTUMUSDT")
        coinList.add("RLCUSDT")
        coinList.add("SNXUSDT")
        coinList.add("SRMUSDT")
        coinList.add("SXPUSDT")
        coinList.add("THETAUSDT")
        coinList.add("TRBUSDT")
        coinList.add("TRXUSDT")
        coinList.add("VETUSDT")
        coinList.add("WAVESUSDT")
        coinList.add("XLMUSDT")
        coinList.add("XMRUSDT")
        coinList.add("XRPUSDT")
        coinList.add("XTZUSDT")
        coinList.add("YFIUSDT")
        coinList.add("YFIIUSDT")
        coinList.add("ZECUSDT")
        coinList.add("ZILUSDT")
        coinList.add("ZRXUSDT")
    }

    override fun onRefresh() {

        forceRefresh = true
        routeItem()

    }

    private fun getAllInterval() {
        candlestickIntervalList.add(CandlestickInterval.WEEKLY)
        candlestickIntervalList.add(CandlestickInterval.THREE_DAILY)
        candlestickIntervalList.add(CandlestickInterval.DAILY)
        candlestickIntervalList.add(CandlestickInterval.TWELVE_HOURLY)
        candlestickIntervalList.add(CandlestickInterval.SIX_HOURLY)
        candlestickIntervalList.add(CandlestickInterval.HOURLY)
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

        lastestCoinsRange.clear()
        openTimeList.clear()
        object : Thread() {
            override fun run() {
                super.run()

                getAllCoins()

                getOX()

                if (currentItemId != R.id.all) {
                    getRank()
                }




                runOnUiThread {
                    tvTitle.text = ""
                    tvTitle.text = stringBuilder
                    forceRefresh = false
                    swipeRefresh.isRefreshing = false
                }
            }
        }.start()
    }

    private fun getOX() {

        var OXbase = ArrayList<Int>()

        var btcCoin = lastestCoinsRange.filter { it.name == "BTCUSDT" }
        for (item in btcCoin) {
            if (item.divide >= BigDecimal.ZERO) {
                OXbase.add(1)
            } else {
                OXbase.add(-1)
            }
        }
        var itemStr = SpannableStringBuilder()
        for (coin in coinList) {

            var coinHistory = lastestCoinsRange.filter { it.name == coin }

            itemStr.append("${coin} ")
            for (index in coinHistory.indices) {
                var history = coinHistory[index]
                var base = OXbase[index]

                if (base == 1 && history.divide >= BigDecimal.ZERO || base == -1 && history.divide < BigDecimal.ZERO) {
                    itemStr.append("-")

                } else {
                    if (history.divide >= BigDecimal.ZERO ){
                        itemStr.append("O")
                    }else {
                        itemStr.append("X")
                    }


                }
            }

            itemStr.append("\n")


        }
        stringBuilder.append(itemStr)
        addDivideLine()
    }

    private fun getRank() {

        for (openTime in openTimeList.sortedDescending()) {

            var filterList = lastestCoinsRange.filter { it.openTime == openTime }
            var list = ArrayList(filterList)

            list.sortByDescending { it.divide }
            var itemStr = SpannableStringBuilder()

            val date = Date(openTime.toLong())
            var format = SimpleDateFormat("MM.dd HH:mm")
            var openTimeStr = format.format(date)

            itemStr.append("${openTimeStr} \n")
            for (coin in list) {
                var divideRate = "  ${coin.divide.setScale(2, RoundingMode.HALF_UP)}%"

                if (coin.name == "BTCUSDT") {
                    var str =
                        setTextColor("${coin.name} $divideRate \n", android.R.color.holo_red_light)
                    itemStr.append(str)
                } else {
                    itemStr.append("${coin.name} $divideRate \n")
                }


            }

            stringBuilder.append(itemStr)
            addDivideLine()
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


    private fun getAllCoins() {
        for (coin in coinList) {
            getCoinInfo(coin)
        }
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
        var lastComboIndex = 0
        for (index in list.indices) {
            var isLineInFilter = false

//            if (index == list.size - 1) {
//                break
//            }
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

            var str = ""

//            if (candlestickInterval == CandlestickInterval.ONE_MINUTE) {
//                str = "${day} O:${item.open} C:${item.close}"
//            } else {
//                str = "${day} O:${item.open} C:${item.close} diff:${diff}"
//            }
            str = "${day} O:${item.open} C:${item.close}"

            // 默认行业是涨幅计算公式是=（今收-昨收）/昨收  但我是(close-open)/open 来计算单个k柱的涨跌幅  当前一个close和当前open有偏差时会不一样

            var divide = diff.divide(open, 6, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)

//            if (index == list.size - 2) {
            var coinrange = KeyLineCoin()
            coinrange.name = coin
            coinrange.divide = divide
            coinrange.candlestickInterval = candlestickInterval
            coinrange.openTime = item.openTime
            coinrange.closeTime = item.closeTime
            lastestCoinsRange.add(coinrange)
//            }
            if (!openTimeList.contains(item.openTime)) {
                openTimeList.add(item.openTime)
            }


            var divideRate = "  ${divide.setScale(2, RoundingMode.HALF_UP)}%"
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

            if (lastComboIndex == index - 1) {
                var tagSpan = setTextColor(" Combo ", android.R.color.holo_orange_dark)
                itemStr.append(tagSpan)

            }
            lastComboIndex = index

            if (index == list.size - 2) {
                var tagSpan = setTextColor(" -- UP TO DATE ", android.R.color.holo_green_dark)
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
                historyRange = 7
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


        var list = syncRequestClient.getCandlestick(
            coin,
            candlestickInterval,
            startTimeLong,
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

