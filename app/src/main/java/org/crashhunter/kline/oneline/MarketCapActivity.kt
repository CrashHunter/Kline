package org.crashhunter.kline.oneline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
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
import com.binance.client.model.custom.MarketCapItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_marketcap.*
import kotlinx.android.synthetic.main.activity_marketcap.table
import kotlinx.android.synthetic.main.activity_roi_percent.*
import org.crashhunter.kline.AppController
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import org.crashhunter.kline.data.*
import org.crashhunter.kline.test.CoinMarketAPI
import org.crashhunter.kline.utils.NumberTools
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import kotlin.collections.ArrayList

//合约的市值
class MarketCapActivity : AppCompatActivity() {
    val options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    var marketCapItemList = java.util.ArrayList<MarketCapItem>()


    val coin = Column<String>("coin", "coin")
    val marketcap = Column<Double>("marketcap", "marketcap")
    val volume_24h = Column<Double>("volume_24h", "volume_24h")


    //coinmarket 前500
    private var latestCoinListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATEST_COIN_LIST,
        ""
    )
    var coinMarketList: List<Data> = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_marketcap)

        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )


        object : Thread() {
            override fun run() {
                super.run()

                //获取币安持有现货列表
                var data = syncRequestClient.getAccountSPOT()
                Constant.ownCoinListName.clear()
                Constant.ownCoinList.clear()
                var filter =
                    data.balances.filter { it.free.toBigDecimal() > BigDecimal.ZERO || it.locked.toBigDecimal() > BigDecimal.ZERO }
                for (item in filter) {
                    Log.d("sss", "${item.asset}:${item.free}")
                    Constant.ownCoinListName.add(item.asset)
                    Constant.ownCoinList.add(item)
                }
                if (latestCoinListJsonStr.isNotEmpty()) {
                    coinMarketList =
                        Gson().fromJson(latestCoinListJsonStr, object : TypeToken<List<Data>>() {}
                            .type) as List<Data>
                    showInfo(coinMarketList)
                } else {
                    getFromAPi()
                }

            }
        }.start()


        val format = IFormat<Double> { NumberTools.amountConversion(it) }
        volume_24h.format = format
        marketcap.format = format

        table.setOnColumnClickListener {
            table.setSortColumn(it.column, !it.column.isReverseSort)
        }
    }

    private fun showTable() {
        val tableData: TableData<MarketCapItem> = TableData<MarketCapItem>(
            "",
            Constant.marketcapItemList,
            coin,
            marketcap,
            volume_24h
        )
        marketcap.isReverseSort = true
        tableData.sortColumn = marketcap
        table.tableData = tableData



        val backgroundFormat: ICellBackgroundFormat<CellInfo<*>> =
            object : BaseCellBackgroundFormat<CellInfo<*>>() {
                override fun getBackGroundColor(cellInfo: CellInfo<*>): Int {

                    var coin = cellInfo.data.toString().replace("USDT", "")
                    if (Constant.ownCoinListName.contains(coin)) {
                        return ContextCompat.getColor(this@MarketCapActivity, R.color.blue)
                    } else {
                        return TableConfig.INVALID_COLOR
                    }
                }
            }
        table.getConfig().setContentCellBackgroundFormat(backgroundFormat)
    }

    private fun getFromAPi() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pro-api.coinmarketcap.com/")
            .client(CoinMarketAPI.genericClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val service = retrofit.create(CoinMarketAPI.ListService::class.java)

        val call: Call<CoinMarketList?>? = service.queryList()


        call!!.enqueue(object : Callback<CoinMarketList?> {

            override fun onResponse(
                call: Call<CoinMarketList?>,
                response: Response<CoinMarketList?>
            ) {
                var datas = response.body()?.data!!
                var coinVolumeJsonStr = Gson().toJson(response.body())

                showInfo(datas)
            }

            override fun onFailure(call: Call<CoinMarketList?>, t: Throwable) {

            }
        })
    }

    private fun showInfo(datas: List<Data>) {
        //只展示合约的币
        var filterList =
            datas.filter {
                Constant.contractCoins.contains(it.symbol.toUpperCase())
            }
        for (data in filterList) {
            var item = MarketCapItem();
            item.coin = data.symbol
            item.marketcap = data.quote.USD.market_cap.toDouble()
            item.volume_24h = data.quote.USD.volume_24h.toDouble()
            marketCapItemList.add(item)
        }
        Constant.marketcapItemList = marketCapItemList

        showTable()
    }

}

