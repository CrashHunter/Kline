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
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import kotlinx.android.synthetic.main.activity_td.header
import kotlinx.android.synthetic.main.activity_td.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis


class TDActivity : AppCompatActivity() {

    val minimum = 10_000_000L
    var volumMin = minimum

    var options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )
    var stringBuilder = SpannableStringBuilder()

    var candlestickInterval = CandlestickInterval.DAILY

    var candlestickIntervalList = ArrayList<CandlestickInterval>()


    var rate = 1.0

    var historyRange = 20

    var currentItemId = R.id.oneDay

    var TDList = ArrayList<Candlestick>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_td)

        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )


        getAllCoins()
    }


    private fun getAllCoins() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
//                    getCoinKlineData("XLMUSDT")
                    for (coin in Constant.coinList) {
                        var n = async {
                            getCoinKlineData(coin)
                        }
                    }
                    amount
                }
            }

            processData()

            runOnUiThread {
                tvTitle.text = ""
                tvTitle.text = stringBuilder
            }

        }
    }

    private fun processData() {

        var list = TDList.sortedBy { it.symbol }
        stringBuilder.clear()

        for (index in list.indices) {

            val item = list[index]


            stringBuilder.append("${index + 1}. ")

            stringBuilder.append("${item.symbol} / H ")


            highColor(item, stringBuilder)

            stringBuilder.append(" / L ")

            lowColor(item, stringBuilder)

            stringBuilder.append("\n")
        }
    }

    private fun highColor(item: Candlestick, stringBuilder: SpannableStringBuilder) {
        if (item.tDhigh >= 9) {
            val span = SpannableStringBuilder("${item.tDhigh}")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                0,
                item.tDhigh.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else {
            stringBuilder.append("${item.tDhigh}")
        }
    }

    private fun lowColor(item: Candlestick, stringBuilder: SpannableStringBuilder) {
        if (item.tDlow >= 9) {
            val span = SpannableStringBuilder("${item.tDlow}")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                0,
                item.tDlow.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else {
            stringBuilder.append("${item.tDlow}")
        }
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
            R.id.oneDay -> {
                header.text = "DAILY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.DAILY)

            }
            R.id.oneWeek -> {
                header.text = "WEEKLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.WEEKLY)

            }
            R.id.oneMonth -> {
                header.text = "MONTHLY"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.MONTHLY)

            }
            R.id.oneyear -> {
                header.text = "YEAR"
                candlestickIntervalList.clear()
                candlestickIntervalList.add(CandlestickInterval.YEAR)

            }

            else -> {
            }
        }
    }


    private fun getCoinKlineData(coin: String): List<Candlestick> {

        try {
            var list = syncRequestClient.getSPOTCandlestick(
                coin,
                candlestickInterval,
                null,
                null,
                historyRange
            )
            Log.d("TDtag", "showData: $coin")

            list.sortByDescending { it.closeTime }
//            var format = SimpleDateFormat("MM.dd HH:mm")
//            for (index in list.indices) {
//                val item = list[index]
//
//                val date = Date(item.closeTime.toLong())
//                var day = format.format(date)
////                Log.d("TDtag", "${day} ${item.close}")
//
//            }

            var item = list[1]
            val high = isHigher(1, list, 0, coin)
            val low = isLower(1, list, 0, coin)
            item.tDhigh = high
            item.tDlow = low
            item.symbol = coin
            TDList.add(item)

            return list
        } catch (e: Exception) {
            Log.e("TDtag", " $coin: " + Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }

    private fun isHigher(index: Int, list: List<Candlestick>, TDSum: Int, coin: String): Int {

        if (index > list.size - 1 || index + 4 > list.size - 1) {
            return 0
        }
        val close = list[index].close
        val close4 = list[index + 4].close

        if (close4 <= close) {
            return isHigher(index + 1, list, TDSum + 1, coin)
        } else {
            var item = list[index]
            var format = SimpleDateFormat("MM.dd")
            val date = Date(item.closeTime.toLong())
            var day = format.format(date)
            Log.d("TDtag", "TD HIGHER ${TDSum} ${day} ${item.close}")
            return TDSum
        }
    }

    private fun isLower(index: Int, list: List<Candlestick>, TDSum: Int, coin: String): Int {

        if (index > list.size - 1 || index + 4 > list.size - 1) {
            return 0
        }
        val close = list[index].close
        val close4 = list[index + 4].close

        if (close4 >= close) {
            return isLower(index + 1, list, TDSum + 1, coin)
        } else {
            var item = list[index]
            var format = SimpleDateFormat("MM.dd")
            val date = Date(item.closeTime.toLong())
            var day = format.format(date)
            Log.d("TDtag", "TD LOWER ${TDSum} ${day} ${item.close}")
            return TDSum
        }
    }


}

