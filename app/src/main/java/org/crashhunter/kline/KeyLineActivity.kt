package org.crashhunter.kline

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_data_analysis.*
import kotlinx.android.synthetic.main.activity_key_line.*
import kotlinx.android.synthetic.main.activity_key_line.header
import kotlinx.android.synthetic.main.activity_key_line.seekBar
import kotlinx.android.synthetic.main.activity_key_line.swipeRefresh
import kotlinx.android.synthetic.main.activity_key_line.tvTitle
import kotlinx.android.synthetic.main.activity_volume.*
import kotlinx.coroutines.*
import org.crashhunter.kline.data.KeyLineCoin
import org.crashhunter.kline.data.SharedPreferenceUtil
import org.crashhunter.kline.utils.StringUtils
import org.crashhunter.kline.utils.TimeUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis


class KeyLineActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    val minimum = 10_000_000L
    var volumMin = minimum

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

    var rate = 1.0

    var historyRange = 2

    var currentItemId = R.id.oneDay

    var lastestCoinsRange = ArrayList<KeyLineCoin>()


    var openTimeList = ArrayList<Long>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_key_line)

        swipeRefresh.setOnRefreshListener(this)



        initAction()


//        getAllInterval()

    }


    override fun onRefresh() {

        forceRefresh = true
        routeItem()

    }

    private fun getAllInterval() {
        candlestickIntervalList.add(CandlestickInterval.MONTHLY)
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
            R.id.oneMonth -> {
                header.text = "MONTHLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.MONTHLY)

                getData()
            }
            R.id.all -> {
                header.text = "All"
                candlestickIntervalList.clear()
                getAllInterval()
            }
            R.id.DA -> {
                startActivity(Intent(this, DataAnalysisActivity::class.java))
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
//                getCoinFilter()
//                getOX()

            }
        }.start()
    }


    private fun printSortedCoinList(
        list: ArrayList<KeyLineCoin>,
        itemStr: SpannableStringBuilder,
        type: String
    ) {
        var i = 0;
        for (index in list.indices) {
            var coin = list[index]

            var close = coin.close
            var rateInc = coin.rateInc
            var ratePrec = "  ${coin.rateInc.setScale(2, RoundingMode.HALF_UP)}%"
            var rangePrec = "  ${coin.rangeInc.setScale(2, RoundingMode.HALF_UP)}%"


            if (type == "Volume") {
                if (index != 0) {
                    if (coin.quoteAssetVolume.toLong() < 1_000_000 && list[index - 1].quoteAssetVolume.toLong() >= 1_000_000) {
                        itemStr.append("-----------------------1_000_000-------------------------------\n")
                    }
                    if (coin.quoteAssetVolume.toLong() < 10_000_000 && list[index - 1].quoteAssetVolume.toLong() >= 10_000_000) {
                        itemStr.append("------------------------10_000_000------------------------------\n")
                    }
                    if (coin.quoteAssetVolume.toLong() < 100_000_000 && list[index - 1].quoteAssetVolume.toLong() >= 100_000_000) {
                        itemStr.append("------------------------100_000_000------------------------------\n")
                    }
                }
            }

//            if (coin.quoteAssetVolume.toLong() < volumMin && candlestickInterval == CandlestickInterval.DAILY) {
//                continue
//            }
            i++;

            var header = "No.${i} ${coin.name} $rangePrec $ratePrec\n"


            if (coin.name == "BTCUSDT" || rateInc < BigDecimal(rate) && rateInc > -BigDecimal(
                    rate
                )
            ) {
                var str = setTextColor(
                    "$header",
                    android.R.color.holo_red_light
                )
                itemStr.append(str)
            } else {
                if (type == "Volume") {
                    continue
                } else {
                    itemStr.append(header)
                }


            }

//            var volumeStr = StringUtils.getFormattedVolume(coin.takerBuyBaseAssetVolume.toString()) + "\n"
//            if (coin.quoteAssetVolume.toLong() < volumMin) {
//                var str = setTextColor(
//                    "$volumeStr",
//                    android.R.color.darker_gray
//                )
//                itemStr.append(str)
//            } else {
//                itemStr.append(volumeStr)
//            }
            var volumeStr2 =
                "           --$close | " + StringUtils.getFormattedVolume(coin.quoteAssetVolume.toString()) + "\n"
            itemStr.append(volumeStr2)


        }
    }

    private fun getOX() {

        var OXbase = ArrayList<Int>()

        var btcCoin = lastestCoinsRange.filter { it.name == "BTCUSDT" }
        for (item in btcCoin) {
            if (item.rateInc >= BigDecimal.ZERO) {
                OXbase.add(1)
            } else {
                OXbase.add(-1)
            }
        }
        var itemStr = SpannableStringBuilder()
        for (coin in Constant.coinList) {

            var coinHistory = lastestCoinsRange.filter { it.name == coin }

            itemStr.append("${coin} ")
            for (index in coinHistory.indices) {
                var history = coinHistory[index]
                var base = OXbase[index]

                if (base == 1 && history.rateInc >= BigDecimal.ZERO || base == -1 && history.rateInc < BigDecimal.ZERO) {
                    itemStr.append("-")

                } else {
                    if (history.rateInc >= BigDecimal.ZERO) {
                        itemStr.append("O")
                    } else {
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

            list.sortBy { it.rangeInc }
            var itemStr = SpannableStringBuilder()

            val date = Date(openTime.toLong())
            var format = SimpleDateFormat("MM.dd HH:mm")
            var openTimeStr = format.format(date)

            itemStr.append("${openTimeStr} \n")
            for (coin in list) {
                var rateInc = coin.rateInc
                var ratePrec = "  ${coin.rateInc.setScale(2, RoundingMode.HALF_UP)}%"
                var rangePrec = "  ${coin.rangeInc.setScale(2, RoundingMode.HALF_UP)}%"

                if (coin.name == "BTCUSDT") {
                    var str =
                        setTextColor(
                            "${coin.name} $rangePrec $ratePrec \n",
                            android.R.color.holo_red_light
                        )
                    itemStr.append(str)
                } else {
                    var str = SpannableStringBuilder()
                    str.append("${coin.name} $rangePrec $ratePrec \n")


                    if (rateInc < BigDecimal(rate) && rateInc > -BigDecimal(rate)) {
                        str = setTextColor(
                            "${coin.name} $rangePrec $ratePrec \n",
                            android.R.color.holo_red_light
                        )
                    }

                    itemStr.append(str)
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

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
//                    var n  = ArrayList<Deferred<Int>>(10)
//                    n.add(Deferred)
                    for (coin in Constant.coinList) {
                        var n = async {
                            getCoinInfo(coin)
                        }
//                        getCoinInfo(coin)

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

            if (currentItemId != R.id.all) {
                stringBuilder.append("-------------- Volume --------------\n")
                getLastestRank("Volume")
                stringBuilder.append("-------------- Rate --------------\n")
                getLastestRank("Rate")
                stringBuilder.append("-------------- Range --------------\n")
                getLastestRank("Range")


//                    getRank()
            }

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
                forceRefresh = false
                swipeRefresh.isRefreshing = false
            }

        }

        //for (coin in coinList) {
        //    getCoinInfo(coin)
        //}
    }

    private fun getLastestRank(type: String) {

        var sorted = openTimeList.sortedDescending()
        for (index in sorted.indices) {
            if (index == 0 && type == "Volume") {
                continue
            }
            if (index > 1) {
                break
            }

            var openTime = sorted[index]
            var filterList = lastestCoinsRange.filter { it.openTime == openTime }
            var list = ArrayList(filterList)

            if (type == "Range") {
                list.sortBy { it.rangeInc }
            } else if (type == "Rate") {
                list.sortBy { it.rateInc }
            } else if (type == "Volume") {
                list.sortByDescending { it.quoteAssetVolume }
            }

            var itemStr = SpannableStringBuilder()

            val date = Date(openTime.toLong())
            var format = SimpleDateFormat("MM.dd HH:mm")
            var openTimeStr = format.format(date)

            itemStr.append("${openTimeStr} \n")
            printSortedCoinList(list, itemStr, type)

            stringBuilder.append(itemStr)
            addDivideLine()
        }


    }

    private suspend fun getCoinInfo(coin: String) {
//        isCoinInFilter = false

        for (item in candlestickIntervalList) {
            candlestickInterval = item
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

//                parseKLineData(coin, list, candlestickInterval)
                collectCoinInfo(coin, list, candlestickInterval)

            } else {
                var list = getCoinKlineData(coin)
//                parseKLineData(coin, list, candlestickInterval)
                collectCoinInfo(coin, list, candlestickInterval)
            }
        }

//        if (isCoinInFilter) {
//            addDivideLine()
//        }


    }

    private fun getCoinFilter() {

        for (coin in Constant.coinList) {
            isCoinInFilter = false
            var jsonList =
                SharedPreferenceUtil.loadData(
                    AppController.instance.applicationContext,
                    "KeyLine-${coin}-$candlestickInterval"
                )
            var list = JSON.parseArray(jsonList, Candlestick::class.java)
            parseKLineData(coin, list, candlestickInterval)

            if (isCoinInFilter) {
                addDivideLine()
            }
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


    private fun collectCoinInfo(
        coin: String,
        list: List<Candlestick>,
        candlestickInterval: CandlestickInterval
    ) {

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


        }

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


            var ratePrec = "  ${rateInc.setScale(2, RoundingMode.HALF_UP)}%"
            var rangePrec = "  ${rangeInc.setScale(2, RoundingMode.HALF_UP)}%"
            var rateSpan = SpannableStringBuilder(ratePrec)


            if (rateInc < BigDecimal(rate) && rateInc > -BigDecimal(rate) && candlestickInterval != CandlestickInterval.HOURLY) {
                rateSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_purple)),
                    0,
                    ratePrec.length - 1,
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
            itemStr.append("${rangePrec}")

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


    private fun initAction() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rate = progress.toDouble()
                //tvRate.setText(rate.toString())
                getData()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }


}

