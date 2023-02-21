package org.crashhunter.kline.oneline

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bin.david.form.core.TableConfig
import com.bin.david.form.data.CellInfo
import com.bin.david.form.data.column.Column
import com.bin.david.form.data.format.IFormat
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat
import com.bin.david.form.data.format.bg.ICellBackgroundFormat
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
import org.crashhunter.kline.utils.NumberTools
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
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
    val multi = Column<BigDecimal>("multi", "multi")
    val volume_24h = Column<Double>("volume_24h", "volume_24h")
    val marketcap = Column<Double>("marketcap", "marketcap")


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


        val format = IFormat<Double> { NumberTools.amountConversion(it) }
        volume_24h.format = format
        marketcap.format = format

        val totalCostFormat =
            IFormat<BigDecimal> { it.setScale(3, BigDecimal.ROUND_HALF_UP).toString() }
        totalCost.format = totalCostFormat

        table.setOnColumnClickListener {
            table.setSortColumn(it.column, !it.column.isReverseSort)
        }
    }

    private fun showTable() {
        val tableData: TableData<HoldPriceItem> = TableData<HoldPriceItem>(
            "",
            Constant.holdPriceItemList,
            coin, costPrice, currentPrice, roi, multi,
            totalCost,
            volume_24h,
            marketcap
        )
        roi.isReverseSort = false
        tableData.sortColumn = roi
        table.tableData = tableData


        val backgroundFormat: ICellBackgroundFormat<CellInfo<*>> =
            object : BaseCellBackgroundFormat<CellInfo<*>>() {
                override fun getBackGroundColor(cellInfo: CellInfo<*>): Int {

                    var coin = cellInfo.data.toString().replace("USDT", "")

                    if (Constant.cleanCoinList.contains(coin)) {
                        return ContextCompat.getColor(this@ROIActivity, android.R.color.darker_gray)
                    }
                    if (Constant.badCoinList.contains(coin)) {
                        return ContextCompat.getColor(this@ROIActivity, R.color.brown)
                    }
                    if (Constant.ACoinList.contains(coin)) {
                        return ContextCompat.getColor(this@ROIActivity, R.color.blue)
                    }
                    return TableConfig.INVALID_COLOR
                }
            }

        table.getConfig().setContentCellBackgroundFormat(backgroundFormat)
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
            if (item.sumBuy > BigDecimal.ZERO) {
                totalSum += item.sumBuy
            }
            val holdPrice = item.holdPrice
            var currentPrice = BigDecimal.ZERO

            val coinName = item.coin
            //获取 currentPrice multi
            for (downPerItem in Constant.holdCoinItemList) {
                if (downPerItem.coin.equals(coinName.replace("USDT", ""))) {
                    currentPrice = downPerItem.current
                    item.currentPrice = currentPrice

                    item.multi =
                        item.holdPrice.divide(item.currentPrice, 2, BigDecimal.ROUND_HALF_UP)
                    break
                }
            }

            //获取24H交易量
            if (Constant.coinMarketList.isNotEmpty()) {

                for (coin in Constant.coinMarketList) {
                    if (coinName.equals(coin.symbol + "USDT")) {
                        item.volume_24h = coin.quote.USD.volume_24h.toDouble()
                        item.marketcap = coin.quote.USD.market_cap.toDouble()
                        break
                    }
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
                "$coinName: currentPrice:$currentPrice holdPrice:$holdPrice holdNum:${item.holdNum}"
            )
            var win = (currentPrice - holdPrice) * item.holdNum
            totalWin += win

            Log.d("Trades", "$coinName: win:$win ")

        }
        var totalROI = BigDecimal.ZERO
        if (totalSum > BigDecimal.ZERO) {
            totalROI = totalWin / totalSum
        }

        var jsonStr = Gson().toJson(list)
        latestAvgPriceItemListJsonStr = jsonStr

        roiStringBuilder.append(
            "总成本:${totalSum.setScale(2, BigDecimal.ROUND_HALF_UP)} " +
                    "/ 总盈亏:${totalWin.setScale(2, BigDecimal.ROUND_HALF_UP)} " +
                    "/ totalROI:${totalROI.setScale(2, BigDecimal.ROUND_HALF_UP)} "
        )

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
