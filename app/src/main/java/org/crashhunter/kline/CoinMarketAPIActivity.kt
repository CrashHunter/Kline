package org.crashhunter.kline

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_volume.tvTitle
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


class CoinMarketAPIActivity : AppCompatActivity() {
    val options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    var ownCoinList = ArrayList<String>()

    private var latestCoinListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATEST_COIN_LIST,
        ""
    )
    var coinMarketList: List<Data> = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)

        tvTitle.text = "loading  getAccountSPOT"
        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )


        object : Thread() {
            override fun run() {
                super.run()

                var data = syncRequestClient.getAccountSPOT()

                var filter = data.balances.filter { it.free.toBigDecimal() > BigDecimal.ZERO }
                for (item in filter) {
                    Log.d("sss", "${item.asset}:${item.free}")
                    ownCoinList.add(item.asset)
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
    }


    private fun getFromAPi() {
        runOnUiThread {

            tvTitle.text = "loading getFromAPi"
        }
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

                Log.d("sss", coinVolumeJsonStr);


                showInfo(datas)


            }

            override fun onFailure(call: Call<CoinMarketList?>, t: Throwable) {
                Log.d("sss", Log.getStackTraceString(t));

            }
        })
    }

    private fun showInfo(datas: List<Data>) {
        var filterList =
            datas.filter { Constant.coinList.contains(it.symbol.toUpperCase() + "USDT") }
        var sortedList =
            filterList.sortedByDescending { it.quote.USD.market_cap.toBigDecimal() }
        var str = SpannableStringBuilder()

        for (index in sortedList.indices) {
            val item = sortedList[index]

            if (index != 0) {
                if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(100_000_000) && sortedList[index - 1].quote.USD.market_cap.toBigDecimal() >= BigDecimal(
                        100_000_000
                    )
                ) {
                    str.append("-----------------------一亿-------------------------------\n")
                }
                if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(1_000_000_000) && sortedList[index - 1].quote.USD.market_cap.toBigDecimal() >= BigDecimal(
                        1_000_000_000
                    )
                ) {
                    str.append("------------------------十亿------------------------------\n")
                }
                if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(10_000_000_000) && sortedList[index - 1].quote.USD.market_cap.toBigDecimal() >= BigDecimal(
                        10_000_000_000
                    )
                ) {
                    str.append("------------------------一百亿------------------------------\n")
                }
            }


            str.append("${index + 1}. ")


            var symbol = item.symbol + " "
            if (ownCoinList.contains(item.symbol)) {
                var symbolSpan = SpannableStringBuilder(symbol)
                symbolSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    symbol.length - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                str.append(symbolSpan)
            } else {
                str.append(symbol)
            }


    //                    str.append(StringUtils.getFormattedVolume(item.quote.USD.market_cap))

            str.append(" " + NumberTools.amountConversion(item.quote.USD.market_cap.toDouble()))

            for (item in Constant.downPerItemList){
                if (item.coin.contains(symbol.trim())){
                    val max = item.max
                    val current = item.current
                    val downPer = item.downPer

                    str.append("  ")

                    if (downPer > BigDecimal(0.8)) {
                        val span = SpannableStringBuilder("$downPer")
                        span.setSpan(
                            ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                            0,
                            downPer.toString().length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        str.append(span)
                    } else if (downPer > BigDecimal(0.6)) {
                        val span = SpannableStringBuilder("$downPer")
                        span.setSpan(
                            ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
                            0,
                            downPer.toString().length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        str.append(span)
                    } else if (downPer > BigDecimal(0.4)) {
                        val span = SpannableStringBuilder("$downPer")
                        span.setSpan(
                            ForegroundColorSpan(getColor(android.R.color.holo_blue_dark)),
                            0,
                            downPer.toString().length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        str.append(span)
                    } else {
                        str.append("$downPer")
                    }

                    break
                }
            }

            str.append("\n")
        }

        runOnUiThread {

            tvTitle.text = str

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

}

