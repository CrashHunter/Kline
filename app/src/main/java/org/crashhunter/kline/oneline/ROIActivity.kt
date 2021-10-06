package org.crashhunter.kline.oneline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.custom.AvgPriceItem
import com.binance.client.model.trade.MyTrade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_down_percent.*
import kotlinx.android.synthetic.main.activity_roi_percent.tvRoi
import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import org.crashhunter.kline.data.BaseSharedPreference
import org.crashhunter.kline.data.LATESTAVGPRICEITEMLISTJSONSTR
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis

class ROIActivity : AppCompatActivity() {

    val options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    var currentItemId = R.id.oneDay

    var roiStringBuilder = SpannableStringBuilder()

    var avgPriceItemList: List<AvgPriceItem> = ArrayList<AvgPriceItem>()
    var avgList = ArrayList<AvgPriceItem>()

    var totalSum = BigDecimal.ZERO
    var totalWin = BigDecimal.ZERO

    private var latestAvgPriceItemListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATESTAVGPRICEITEMLISTJSONSTR,
        ""
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roi_percent)

        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )



        if (latestAvgPriceItemListJsonStr.isNotEmpty()) {

            avgPriceItemList =
                Gson().fromJson(
                    latestAvgPriceItemListJsonStr,
                    object : TypeToken<List<AvgPriceItem>>() {}
                        .type) as List<AvgPriceItem>


            var list = avgPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }

    }

    private fun getSPOTAccountTrades(coin: String): List<MyTrade> {

        runOnUiThread {

            tvRoi.text = "loading $coin"

        }
        try {
            var list = syncRequestClient.getSPOTAccountTrades(
                coin,
                null,
                null,
                null,
                null
            )
            Log.d(
                "Trades",
                "getSPOTAccountTrades:------------------------------------------ $coin"
            )
            var sum = BigDecimal.ZERO
            var holdNum = BigDecimal.ZERO
            //获取当前持有数
            for (item in Constant.ownCoinList) {
                if (coin.equals(item.asset + "USDT")) {
                    holdNum = BigDecimal(item.free) + BigDecimal(item.locked)

                    Log.d(
                        "Trades",
                        "$coin free:${item.free} locked:${item.locked}  holdNum:$holdNum"
                    )
                    break
                }
            }
            list.sortByDescending { it.time }

            var tempHoldNum = BigDecimal.ZERO
            for (item in list) {
                if (item.isBuyer) {
                    tempHoldNum += item.qty
                    sum += item.quoteQty
                } else {
                    tempHoldNum -= item.qty
                    sum -= item.quoteQty
                }

                val date = Date(item.time)
                var format = SimpleDateFormat("yyyy.MM.dd HH:mm")
                var tradeTime = format.format(date)
                Log.d(
                    "Trades",
                    "$coin: ${item.isBuyer} price:${item.price} qty:${item.qty} quoteQty:${item.quoteQty} $tradeTime"
                )

                //找到最近满足持仓的记录
                if (tempHoldNum >= holdNum) {
                    break
                }

            }


            if (holdNum != BigDecimal.ZERO) {
                var avgPrice = BigDecimal.ZERO
                if (sum > BigDecimal.ZERO) {
                    avgPrice = sum / holdNum
                } else {
                    avgPrice = BigDecimal.ZERO
                }
                Log.d("Trades", "$coin: sum:$sum holdNum:$holdNum avgPrice:${avgPrice} ")

                var avgPriceItem = AvgPriceItem()
                avgPriceItem.coin = coin
                avgPriceItem.avgPrice = avgPrice
                avgPriceItem.sumBuy = sum
                avgPriceItem.holdNum = holdNum

                avgList.add(avgPriceItem)
            } else {
                Log.d("Trades", "EMPTY")
            }


            return list
        } catch (e: Exception) {
            Log.e("Trades", "$coin: " + Log.getStackTraceString(e))
        }
        return ArrayList<MyTrade>(0)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.roipercent_menu, menu)
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
            R.id.Alpha -> {
                var list = avgPriceItemList.sortedBy { it.coin }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }
            R.id.DownPer -> {

                var list = avgPriceItemList.sortedBy { it.roi }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }

            R.id.SumBuy -> {
                var list = avgPriceItemList.sortedByDescending { it.sumBuy }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }
            R.id.ROI -> {

                getAllCoinsAvg()

            }
            else -> {
            }
        }
    }


    private fun getAllCoinsAvg() {
        avgPriceItemList = ArrayList<AvgPriceItem>()
        avgList = ArrayList<AvgPriceItem>()
        if (Constant.ownCoinListName.isEmpty()) {
            Toast.makeText(applicationContext, "no ownCoinListName", Toast.LENGTH_LONG).show()
            return
        }

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
//                    getSPOTAccountTrades("SXPUSDT")
                    for (coin in Constant.coinList) {
                        if (Constant.ownCoinListName.contains(coin.replace("USDT", ""))) {
                            Thread.sleep(100)
                            getSPOTAccountTrades(coin)
                        }
                    }
                }
            }

            avgPriceItemList = avgList

            var jsonStr = Gson().toJson(avgPriceItemList)
            latestAvgPriceItemListJsonStr = jsonStr

            var list = avgPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }

    }


    private fun processROIData(list: List<AvgPriceItem>) {




        roiStringBuilder.clear()
        var totalSum = BigDecimal.ZERO
        var totalWin = BigDecimal.ZERO

        for (index in list.indices) {

            val item = list[index]

            totalSum += item.sumBuy

            val avgPrice = item.avgPrice
            var currentPrice = BigDecimal.ZERO

            val coin = item.coin
            for (downPerItem in Constant.downPerItemList) {
                if (downPerItem.coin.equals(coin)) {
                    currentPrice = downPerItem.current
                    break
                }
            }
            if (avgPrice <= BigDecimal.ZERO) {
                //optimize
                item.roi = BigDecimal(99999)
            } else if (currentPrice >= avgPrice) {
                item.roi = currentPrice / avgPrice
            } else {
                item.roi = -(BigDecimal.ONE.minus(currentPrice / avgPrice)).setScale(
                    4,
                    BigDecimal.ROUND_HALF_UP
                )
            }
            val roi = item.roi

            var win = (currentPrice - avgPrice) * item.holdNum
            totalWin += win

            roiStringBuilder.append("${index + 1}. ")

            roiStringBuilder.append(" $coin ${item.sumBuy} $currentPrice / $avgPrice /")

            //roiStringBuilder.append("$roi / ")

            ROIColor(roi, roiStringBuilder)

            roiStringBuilder.append("\n")
        }
        var totalROI = BigDecimal.ZERO
        if (totalSum > BigDecimal.ZERO) {
            totalROI = totalWin / totalSum
        }
        roiStringBuilder.append("totalSum:$totalSum \ntotalWin:$totalWin \ntotalROI:${totalROI} \n ")
    }

    private fun ROIColor(roi: BigDecimal, stringBuilder: SpannableStringBuilder) {
        if (roi > BigDecimal(0)) {
            val span = SpannableStringBuilder("$roi")
            span.setSpan(
                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                0,
                roi.toString().length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            stringBuilder.append(span)
        } else {
            stringBuilder.append("$roi")
        }
    }


}
