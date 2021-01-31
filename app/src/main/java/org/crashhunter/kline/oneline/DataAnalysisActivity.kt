package org.crashhunter.kline.oneline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_data_analysis.*
import kotlinx.android.synthetic.main.activity_data_analysis.swipeRefresh
import kotlinx.android.synthetic.main.activity_data_analysis.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
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

    var forceRefresh = false

    var candlestickIntervalList = ArrayList<CandlestickInterval>()
    var stringBuilder = SpannableStringBuilder()
    var candlestickInterval = CandlestickInterval.DAILY

    //var purplePointBase = 0.5
    //var redPointBase = 0.25
    var rate = 1.0
    var historyRange = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_analysis)
        swipeRefresh.setOnRefreshListener(this)

        candlestickIntervalList.add(CandlestickInterval.MONTHLY)
        initAction()

        getData()

    }
    var currentItemId = R.id.oneDay
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
                //getAllInterval()
            }
            R.id.DA -> {
                startActivity(Intent(this, DataAnalysisActivity::class.java))
            }

            else -> {
            }
        }
    }


    private fun initAction() {
        request.setOnClickListener {

            rate = tvRate.text.toString().toDouble()
            getData()

        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                rate = progress.toDouble()
                tvRate.setText(rate.toString())
                getData()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun getData() {
        tvTitle.text = "Loading..."

        object : Thread() {
            override fun run() {
                super.run()

                getAllCoins()

            }
        }.start()
    }

    private fun getAllCoins() {
        total = 0;
        winCount = 0;
        totalWinRate = BigDecimal.ZERO;

        loseCount = 0;
        totalLoseRate = BigDecimal.ZERO;
        stringBuilder.clear()

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
            var finabuild = SpannableStringBuilder()
            if (total > 0) {
                finabuild.append("Total $total  Win $winCount  Rate: ${winCount / total.toDouble()}\n")
            } else {
                finabuild.append("Total $total  Win $winCount  Rate: --\n")
            }
            var avgwinrate = BigDecimal.ZERO
            if (winCount > 0) {
                avgwinrate = totalWinRate / BigDecimal(winCount)
            }

            var avgloserate = BigDecimal.ZERO
            if (loseCount > 0) {
                avgloserate = totalLoseRate / BigDecimal(loseCount)
            }

            finabuild.append("Avg.Win ${avgwinrate}%  Avg.Lose: ${avgloserate}%\n\n")
            finabuild.append(stringBuilder)
            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = finabuild
                forceRefresh = false
                swipeRefresh.isRefreshing = false
            }

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

    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
    )

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
    var winCount = 0;
    var totalWinRate = BigDecimal.ZERO;

    var loseCount = 0;
    var totalLoseRate = BigDecimal.ZERO;

    private fun getTarget(list: ArrayList<KeyLineCoin>) {

        for (index in list.indices) {

            if (index == list.size - 1) {
                break
            }

            var coin = list[index]

            var rateInc = coin.rateInc

            var itemStr = buildTxt(coin)


            if (rateInc < BigDecimal(rate) && rateInc > -BigDecimal(rate)) {

                total++

                var str = setTextColor(
                        "$itemStr",
                        android.R.color.holo_red_light
                )
                stringBuilder.append("No.${total}")
                stringBuilder.append(str)

                compareNext(index, list, coin)

            }
        }
    }

    private fun compareNext(index: Int, list: ArrayList<KeyLineCoin>, coin: KeyLineCoin) {
        if (index != list.size - 1) {
            var nextCoin = list[index + 1]
            var nextRateInc = list[index + 1].rateInc
            if (nextRateInc > BigDecimal.ZERO) {
                winCount++
                totalWinRate += nextRateInc
                var itemStr = buildTxt(nextCoin)

                stringBuilder.append("      ")
                stringBuilder.append(itemStr)
                stringBuilder.append("\n")
            } else {
                loseCount++
                totalLoseRate += nextRateInc

                var itemStr = buildTxt(nextCoin)

                stringBuilder.append("      ")
                stringBuilder.append(itemStr)
                stringBuilder.append("\n")
            }
        }
    }

    private fun buildTxt(keyLineCoin: KeyLineCoin): String {
        var ratePrec = "  ${keyLineCoin.rateInc.setScale(2, RoundingMode.HALF_UP)}%"
        var rangePrec = "  ${keyLineCoin.rangeInc.setScale(2, RoundingMode.HALF_UP)}%"


        val date = Date(keyLineCoin.openTime.toLong())
        var format = SimpleDateFormat("yyyy.MM.dd")
        var openTimeStr = format.format(date)

        var itemStr = " ${keyLineCoin.name} $rangePrec $ratePrec  $openTimeStr\n"
        return itemStr
    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {
        TimeUtils.stringToLong("2020-7-27 08:00", "yyyy-MM-dd HH:mm")

        //var startTimeLong: Long? = null
        //var startTimeStr = startTime.text.toString()
        //if (startTimeStr.isNotEmpty() && !startTimeStr.contains("XX")) {
        //    startTimeLong = TimeUtils.stringToLong(startTimeStr, "yyyy-MM-dd HH:mm")
        //}

        try {
            var list = syncRequestClient.getCandlestick(
                    coin,
                    candlestickInterval,
                    null,
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


    override fun onRefresh() {
        forceRefresh = true
        getData()
    }

}