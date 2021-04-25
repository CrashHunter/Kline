package org.crashhunter.kline

import CoinVolume
import Data
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_volume.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.crashhunter.kline.data.CoinMarketList
import org.crashhunter.kline.data.SharedPreferenceUtil
import org.crashhunter.kline.test.CoinMarketAPI
import org.crashhunter.kline.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class CoinMarketAPIActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)

        tvTitle.text = "loading"
        getFromAPi()
    }



    private fun getFromAPi() {
        tvTitle.text = "loading"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pro-api.coinmarketcap.com/")
            .client(CoinMarketAPI.genericClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val service = retrofit.create(CoinMarketAPI.ListService::class.java)

        val call: Call<CoinMarketList?>? = service.queryList()


        call!!.enqueue(object : Callback<CoinMarketList?> {

            override fun onResponse(call: Call<CoinMarketList?>, response: Response<CoinMarketList?>) {
                var datas = response.body()?.data!!
                var coinVolumeJsonStr = Gson().toJson(response.body())

                Log.d("sss", coinVolumeJsonStr);


               var filterList = datas.filter { Constant.coinList.contains(it.symbol.toUpperCase()+"USDT") }

                var str = SpannableStringBuilder()

                for(index in filterList.indices){
                    val item = filterList[index]

                    if (index != 0) {
                        if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(100_000_000) && filterList[index - 1].quote.USD.market_cap.toBigDecimal() >= BigDecimal(100_000_000)) {
                            str.append("-----------------------一亿-------------------------------\n")
                        }
                        if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(1_000_000_000) && filterList[index - 1].quote.USD.market_cap.toBigDecimal() >= BigDecimal(1_000_000_000) ) {
                            str.append("------------------------十亿------------------------------\n")
                        }
                        if (item.quote.USD.market_cap.toBigDecimal() < BigDecimal(10_000_000_000) && filterList[index - 1].quote.USD.market_cap.toBigDecimal() >=BigDecimal(10_000_000_000) ) {
                            str.append("------------------------一百亿------------------------------\n")
                        }
                    }


                    str.append("${index+1}. ")

                    str.append("${item.symbol} ")

                    str.append(StringUtils.getFormattedVolume(item.quote.USD.market_cap))

                    str.append("\n")
                }

                runOnUiThread {

                    tvTitle.text = str.toString()

                }


            }

            override fun onFailure(call: Call<CoinMarketList?>, t: Throwable) {
                Log.d("sss", Log.getStackTraceString(t));

            }
        })
    }

}

