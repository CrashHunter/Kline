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
import org.crashhunter.kline.data.SharedPreferenceUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*


class VolumeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)


        initAction()
//        OKHTTPRequest()

        tvTitle.text = "loading"
        getData("usdt")

    }

    fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        return calendar.time.time
    }

    private fun getStartTimeOfDay(timeZone: String): Long {
        val tz = if (TextUtils.isEmpty(timeZone)) "GMT+8" else timeZone
        val curTimeZone = TimeZone.getTimeZone(tz)
        val calendar = Calendar.getInstance(curTimeZone)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar[Calendar.HOUR_OF_DAY] = 24
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.timeInMillis
    }


    private fun getData(coin: String) {
        var coinVolumeJsonStr =
            SharedPreferenceUtil.loadData(AppController.instance.applicationContext, coin)
        if (coinVolumeJsonStr.isNotEmpty()) {
            var coinVolumeSpData = Gson().fromJson(
                coinVolumeJsonStr, CoinVolume::class.java
            )


            val date = Date(coinVolumeSpData.timeTo.toLong() * 1000)
            val format = SimpleDateFormat("yyyy.MM.dd")
            var spDayStr = format.format(date)

            val currentDay = Date(getTodayStartTime())
            var currentDayStr = format.format(currentDay)


            if (spDayStr == currentDayStr) {
                showData(coin, coinVolumeSpData.data)
                return
            }
        }


        getFromAPi(coin)
    }

    private fun getFromAPi(coin: String) {
        tvTitle.text = "loading"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://min-api.cryptocompare.com/data/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(KlineService::class.java)


        val call: Call<CoinVolume?>? = service.queryVolume(coin)


        call!!.enqueue(object : Callback<CoinVolume?> {

            override fun onResponse(call: Call<CoinVolume?>, response: Response<CoinVolume?>) {
                var datas = response.body()?.data!!
                var coinVolumeJsonStr = Gson().toJson(response.body())
                SharedPreferenceUtil.saveData(
                    AppController.instance.applicationContext,
                    coin,
                    coinVolumeJsonStr
                )
                showData(coin, datas)

            }

            override fun onFailure(call: Call<CoinVolume?>, t: Throwable) {

                tvTitle.text = t.localizedMessage

            }
        })
    }

    private fun showData(coin: String, datas: List<Data>) {
        tvTitle.text = ""
        var str = SpannableStringBuilder()
        //                str.append(response.raw().body?.string() + " \n")

        str.append("$coin: \n")

        var lastMaxStr = SharedPreferenceUtil.loadData(
            AppController.instance.applicationContext,
            "${coin}-Max"
        )
        var lastMinStr = SharedPreferenceUtil.loadData(
            AppController.instance.applicationContext,
            "${coin}-Min"
        )

        if (lastMaxStr.isNotEmpty() && lastMinStr.isNotEmpty()) {
            var lastMax = Gson().fromJson(lastMaxStr, Data::class.java)
            var lastMin = Gson().fromJson(lastMinStr, Data::class.java)

            val date = Date(lastMax.time.toLong() * 1000)
            val format = SimpleDateFormat("yyyy.MM.dd")
            var day = format.format(date)

            str.append("Max: ${day} : ${lastMax.totalVolumeTotal.getMoneyFormat()} \n")

            val date2 = Date(lastMin.time.toLong() * 1000)
            var day2 = format.format(date2)

            str.append("Min: ${day2} : ${lastMin.totalVolumeTotal.getMoneyFormat()} \n")
        }


        var preVolumeStr = ""


        for (index in datas.indices) {
            var data = datas.get(index)
            var rateSpan = SpannableStringBuilder("")
            var volumeStr = data.totalVolumeTotal

            if (index < datas.size - 1) {
                if (lastMaxStr.isEmpty()) {
                    lastMaxStr = Gson().toJson(data)

                } else {
                    var lastMax = Gson().fromJson(lastMaxStr, Data::class.java)

                    if (BigDecimal(lastMax.totalVolumeTotal) < BigDecimal(volumeStr)) {
                        lastMaxStr = Gson().toJson(data)
                    }

                }
                if (lastMinStr.isEmpty() && BigDecimal(volumeStr) > BigDecimal.ZERO
                ) {
                    lastMinStr = Gson().toJson(data)

                } else {
                    var lastMin = Gson().fromJson(lastMinStr, Data::class.java)

                    if (BigDecimal(lastMin.totalVolumeTotal) > BigDecimal(volumeStr) && BigDecimal(volumeStr) > BigDecimal.ZERO
                    ) {
                        lastMinStr = Gson().toJson(data)
                    }
                }
            }




            if (!preVolumeStr.isEmpty() && preVolumeStr.toBigDecimal() > BigDecimal.ZERO) {

                var preValue = preVolumeStr.toBigDecimal()
                var currentValue = volumeStr.toBigDecimal()

                var diff = currentValue.minus(preValue)


                var divide = diff.divide(preValue, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)

                var divideRate = "  $divide%"
                rateSpan = SpannableStringBuilder(divideRate)

                if (divide > BigDecimal.ZERO) {
                    rateSpan.setSpan(
                        ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                        0,
                        divideRate.length - 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                } else {
                    rateSpan.setSpan(
                        ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                        0,
                        divideRate.length - 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )


                }


            }
            preVolumeStr = volumeStr

            val date = Date(data.time.toLong() * 1000)
            val format = SimpleDateFormat("yyyy.MM.dd")
            var day = format.format(date)

            str.append("${day} : ${volumeStr.getMoneyFormat()} ")
            str.append(rateSpan)
            str.append("\n")
        }


        SharedPreferenceUtil.saveData(
            AppController.instance.applicationContext,
            "${coin}-Max", lastMaxStr
        )
        SharedPreferenceUtil.saveData(
            AppController.instance.applicationContext,
            "${coin}-Min", lastMinStr
        )

        tvTitle.text = str
    }

    private fun findMaxAndMin(coin: String, volumeStr: String) {
        var lastMax = SharedPreferenceUtil.loadData(
            AppController.instance.applicationContext,
            "${coin}-Max", "0"
        )
        var lastMin = SharedPreferenceUtil.loadData(
            AppController.instance.applicationContext,
            "${coin}-Min", "9999"
        )

        if (BigDecimal(lastMax) < BigDecimal(volumeStr)) {
            SharedPreferenceUtil.saveData(
                AppController.instance.applicationContext,
                "${coin}-Max", volumeStr
            )
        }

        if (BigDecimal(lastMin) > BigDecimal(volumeStr)) {
            SharedPreferenceUtil.saveData(
                AppController.instance.applicationContext,
                "${coin}-Min", volumeStr
            )
        }
    }


    private fun initAction() {

        request.setOnClickListener {

            var coin = etCoin.text.toString()

            if (coin.isNotEmpty()) {

                getData(coin)
            }


        }

    }


    interface KlineService {
        //https://min-api.cryptocompare.com/data/symbol/histoday?fsym=BTC&tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d
        @GET("symbol/histoday?tsym=USD&limit=90&api_key=e607037237ae0f8fce5397d2dd69253d0321f07a05aa88c0206bd36b5e3fbec7")
        fun queryVolume(@Query("fsym") fsymCoin: String): Call<CoinVolume?>?
    }
}

