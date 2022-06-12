package org.crashhunter.kline.oneline

import android.content.Intent
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
import com.binance.client.model.custom.CostPriceItem
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import com.binance.client.model.trade.MyTrade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_roi_percent.tvRoi
import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.CalculateActivity
import org.crashhunter.kline.Constant
import org.crashhunter.kline.Constant.costPriceItemList
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

    var avgList = ArrayList<CostPriceItem>()

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

            costPriceItemList =
                Gson().fromJson(
                    latestAvgPriceItemListJsonStr,
                    object : TypeToken<List<CostPriceItem>>() {}
                        .type) as List<CostPriceItem>


            var list = costPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }
        getAllCoinsAvgs()
    }


    private fun getAllCoinsAvgs() {
        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0

                    for (coin in Constant.ownCoinListName) {
                        var n = async {
                            getCoinKlineData(coin + "USDT")
                        }
                    }
                    amount
                }
            }
        }
    }

    private fun getCoinKlineData(coin: String): List<Candlestick> {

        try {
            //没有YEAR的维度，最大到月
            var list = syncRequestClient.getSPOTCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                36
            )
            Log.d("sss", "showData:$coin")

            var max = BigDecimal.ZERO
            var min = BigDecimal(9999999999)
            for (index in list.indices) {
                if (index == 0) {
                    continue
                }
                if (list.get(index).high > max) {
                    max = list.get(index).high
                }

                if (list.get(index).low < min
                    && list.get(index).low > BigDecimal.ZERO
                    && list.get(index).low != BigDecimal(0.0001)
                ) {
                    min = list.get(index).low
                }
            }

            var current = list[list.size - 1].close
            var downPer = BigDecimal.ONE.subtract(current.divide(max, 4, BigDecimal.ROUND_HALF_UP))
                .setScale(4, BigDecimal.ROUND_HALF_UP)

            var upPer = current.divide(min, 4, BigDecimal.ROUND_HALF_UP)


            var downPerItem = DownPerItem()
            downPerItem.coin = coin
            downPerItem.current = current
            downPerItem.max = max
            downPerItem.min = min
            downPerItem.downPer = downPer
            downPerItem.upPer = upPer
            Constant.downPerItemList.add(downPerItem)
            return list
        } catch (e: Exception) {
            Log.e("sss", "Error Coin $coin: : " + Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


    //获取交易记录
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

                var avgPriceItem =
                    CostPriceItem()
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
                var list = costPriceItemList.sortedBy { it.coin }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }
            R.id.DownPer -> {

                var list = costPriceItemList.sortedBy { it.roi }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }

            R.id.SumBuy -> {
                var list = costPriceItemList.sortedByDescending { it.sumBuy }

                processROIData(list)

                runOnUiThread {
                    tvRoi.text = ""
                    tvRoi.text = roiStringBuilder
                }
            }
            R.id.ROI -> {

                getAllCoinsAvgs()
                getOwnCoinsCost()

            }
            R.id.calculate -> {

                startActivity(Intent(this, CalculateActivity::class.java))

            }
            else -> {
            }
        }
    }


    //获取成本价
    private fun getOwnCoinsCost() {
        costPriceItemList = ArrayList<CostPriceItem>()
        avgList = ArrayList<CostPriceItem>()
        if (Constant.ownCoinListName.isEmpty()) {
            Toast.makeText(applicationContext, "no ownCoinListName", Toast.LENGTH_LONG).show()
            return
        }

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
//                    getSPOTAccountTrades("SXPUSDT")
                    for (coin in Constant.ownCoinListName.sorted()) {
                        Thread.sleep(10)
                        getSPOTAccountTrades(coin + "USDT")
                    }
                }
            }

            costPriceItemList = avgList

            var jsonStr = Gson().toJson(costPriceItemList)
            latestAvgPriceItemListJsonStr = jsonStr

            var list = costPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }

    }


    private fun processROIData(list: List<CostPriceItem>) {

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
                if (downPerItem.coin.contains(coin.replace("USDT", ""))) {
                    currentPrice = downPerItem.current
                    break
                }
            }
            if (avgPrice <= BigDecimal.ZERO) {
                //optimize
                item.roi = BigDecimal(99999)
            } else if (currentPrice >= avgPrice) {
                item.roi = currentPrice.divide(avgPrice, 4, BigDecimal.ROUND_HALF_UP)
            } else {
                item.roi = -(BigDecimal.ONE.minus(
                    currentPrice.divide(
                        avgPrice,
                        4,
                        BigDecimal.ROUND_HALF_UP
                    )
                )).setScale(
                    4,
                    BigDecimal.ROUND_HALF_UP
                )
            }
            val roi = item.roi

            var win = (currentPrice - avgPrice) * item.holdNum
            totalWin += win

            roiStringBuilder.append("${index + 1}. ")

            roiStringBuilder.append(" $coin ${item.sumBuy} $avgPrice / $currentPrice /")

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
