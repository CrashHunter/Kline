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
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.table.TableData
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.custom.HoldPriceItem
import com.binance.client.model.trade.MyTrade
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_roi_percent.*

import kotlinx.coroutines.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.CalculateActivity
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

    var avgList = ArrayList<HoldPriceItem>()

    var totalSum = BigDecimal.ZERO
    var totalWin = BigDecimal.ZERO

    private var latestAvgPriceItemListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATESTAVGPRICEITEMLISTJSONSTR,
        ""
    )


    val coin = Column<String>("coin", "coin")
    val totalCost = Column<BigDecimal>("成本", "sumBuy")
    val costPrice = Column<BigDecimal>("成本价", "holdPrice")
    val currentPrice = Column<BigDecimal>("当前价", "currentPrice")
    val roi = Column<BigDecimal>("roi", "roi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_roi_percent)

        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )



        if (latestAvgPriceItemListJsonStr.isNotEmpty()) {

            Constant.holdPriceItemList = Gson().fromJson(
                latestAvgPriceItemListJsonStr,
                object : TypeToken<List<HoldPriceItem>>() {}
                    .type) as List<HoldPriceItem>


            var list = Constant.holdPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }




        table.setOnColumnClickListener {
            table.setSortColumn(it.column, !it.column.isReverseSort)
        }
    }

    private fun showTable() {
        val tableData: TableData<HoldPriceItem> = TableData<HoldPriceItem>(
            "",
            Constant.holdPriceItemList,
            coin, roi,
            totalCost, costPrice,
            currentPrice
        )
        roi.isReverseSort = false
        tableData.sortColumn = roi
        table.tableData = tableData
        //        table.getConfig().setContentStyle(FontStyle(50, Color.BLUE))
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
                var holdPrice = BigDecimal.ZERO
                if (sum > BigDecimal.ZERO) {
                    holdPrice = sum / holdNum
                } else {
                    holdPrice = BigDecimal.ZERO
                }
                Log.d("Trades", "$coin: sum:$sum holdNum:$holdNum avgPrice:${holdPrice} ")

                var holdPriceItem =
                    HoldPriceItem()
                holdPriceItem.coin = coin
                holdPriceItem.holdPrice = holdPrice
                holdPriceItem.sumBuy = sum
                holdPriceItem.holdNum = holdNum

                avgList.add(holdPriceItem)
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
            R.id.ROI -> {
                //获取持有币成本
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
        Constant.holdPriceItemList = ArrayList<HoldPriceItem>()
        avgList = ArrayList<HoldPriceItem>()
        if (Constant.ownCoinListName.isEmpty()) {
            Toast.makeText(applicationContext, "no ownCoinListName", Toast.LENGTH_LONG).show()
            return
        }

        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
//                    getSPOTAccountTrades("FILUSDT")
                    for (coin in Constant.ownCoinListName.sorted()) {
                        Thread.sleep(10)
                        getSPOTAccountTrades(coin + "USDT")
                    }
                }
            }

            Constant.holdPriceItemList = avgList

            var list = Constant.holdPriceItemList.sortedBy { it.roi }
            processROIData(list)

            runOnUiThread {
                tvRoi.text = ""
                tvRoi.text = roiStringBuilder
            }
        }

    }


    private fun processROIData(list: List<HoldPriceItem>) {

        roiStringBuilder.clear()
        var totalSum = BigDecimal.ZERO
        var totalWin = BigDecimal.ZERO

        for (index in list.indices) {

            val item = list[index]
            if (item.coin.equals("BUSDUSDT")) {
                continue
            }
            if (item.sumBuy> BigDecimal.ZERO){
                totalSum += item.sumBuy
            }
            val holdPrice = item.holdPrice
            var currentPrice = BigDecimal.ZERO

            val coin = item.coin
            for (downPerItem in Constant.holdCoinItemList) {
                if (downPerItem.coin.equals(coin.replace("USDT",""))) {
                    currentPrice = downPerItem.current
                    item.currentPrice = currentPrice
                    break
                }
            }
            if (holdPrice <= BigDecimal.ZERO) {
                //optimize
                item.roi = BigDecimal(99999)
            } else if (currentPrice >= holdPrice) {
                item.roi = currentPrice.divide(holdPrice, 4, BigDecimal.ROUND_HALF_UP)
            } else {
                item.roi = -(BigDecimal.ONE.minus(
                    currentPrice.divide(
                        holdPrice,
                        4,
                        BigDecimal.ROUND_HALF_UP
                    )
                )).setScale(
                    4,
                    BigDecimal.ROUND_HALF_UP
                )
            }
            Log.d(
                "Trades",
                "$coin: currentPrice:$currentPrice holdPrice:$holdPrice holdNum:${item.holdNum}"
            )
            var win = (currentPrice - holdPrice) * item.holdNum
            totalWin += win

            Log.d("Trades", "$coin: win:$win ")

        }
        var totalROI = BigDecimal.ZERO
        if (totalSum > BigDecimal.ZERO) {
            totalROI = totalWin / totalSum
        }

        var jsonStr = Gson().toJson(list)
        latestAvgPriceItemListJsonStr = jsonStr

        roiStringBuilder.append("总成本:$totalSum \n总盈亏:$totalWin \ntotalROI:${totalROI} \n ")

        showTable()
    }

//    private fun ROIColor(roi: BigDecimal, stringBuilder: SpannableStringBuilder) {
//        if (roi > BigDecimal(0)) {
//            val span = SpannableStringBuilder("$roi")
//            span.setSpan(
//                ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
//                0,
//                roi.toString().length,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//
//            stringBuilder.append(span)
//        } else {
//            stringBuilder.append("$roi")
//        }
//    }


}
