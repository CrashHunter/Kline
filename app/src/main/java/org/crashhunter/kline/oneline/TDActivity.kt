package org.crashhunter.kline.oneline

import android.os.Bundle
import android.text.SpannableStringBuilder
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
import kotlinx.android.synthetic.main.activity_td.swipeRefresh
import kotlinx.android.synthetic.main.activity_td.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.R
import org.crashhunter.kline.data.KeyLineCoin
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

    var forceRefresh = false
    var isCoinInFilter = false

    var rate = 1.0

    var historyRange = 30

    var currentItemId = R.id.oneDay

    var lastestCoinsRange = ArrayList<KeyLineCoin>()


    var openTimeList = ArrayList<Long>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_td)

//        options.url = "https://api.binance.com"
//        syncRequestClient = SyncRequestClient.create(
//            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
//            options
//        )


        getAllCoins()
    }


    private fun getAllCoins() {

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
                    getCoinKlineData("1INCHUSDT")
//                    for (coin in Constant.coinList) {
//                        var n = async {
//                            getCoinKlineData(coin)
//                        }
//                    }
                    amount
                }
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
            var list = syncRequestClient.getCandlestick(
                coin,
                candlestickInterval,
                null,
                null,
                historyRange
            )
            Log.d("TDtag", "showData: $coin")

            list.sortByDescending { it.closeTime }
            var format = SimpleDateFormat("MM.dd HH:mm")
            for (index in list.indices) {
                val item = list[index]

                val date = Date(item.closeTime.toLong())
                var day = format.format(date)
//                Log.d("TDtag", "${day} ${item.close}")

                var TDSum = 0;

                isLower(index, list, TDSum)


            }



            return list
        } catch (e: Exception) {
            Log.e("TDtag", Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }

    private fun isLower(index: Int, list: List<Candlestick>, TDSum: Int) {
        if (TDSum == 13) {
            var format = SimpleDateFormat("MM.dd")
            val date = Date(list[index-13].closeTime.toLong())
            var day = format.format(date)
            Log.d("TDtag", "133333 ${day} ${list[index-13].close}")
            return
        } else if (TDSum == 9) {
            var format = SimpleDateFormat("MM.dd")
            val date = Date(list[index-9].closeTime.toLong())
            var day = format.format(date)
            Log.d("TDtag", "99999 ${day} ${list[index-9].close}")
        }

        if (index > list.size - 1 || index + 4 > list.size - 1) {
            return
        }
        val close = list[index].close
        val close4 = list[index + 4].close

        if (close4 >= close) {
            isLower(index + 1, list, TDSum + 1)
        } else {
            return
        }
    }


}

