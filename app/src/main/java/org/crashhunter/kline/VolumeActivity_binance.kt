package org.crashhunter.kline

import CoinVolume
import CoinVolume2
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
import com.binance.client.model.market.Candlestick
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_volume.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


class VolumeActivity_binance : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)


        initAction()
//        OKHTTPRequest()

        tvTitle.text = "loading"
        getData("btcusdt")

    }

    fun getTodayStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        return calendar.time.time
    }


    private fun getData(coin: String) {
        var coinVolumeJsonStr =
            SharedPreferenceUtil.loadData(AppController.instance.applicationContext, coin)
        if (coinVolumeJsonStr.isNotEmpty()) {
            var coinVolumeSpData = Gson().fromJson(
                coinVolumeJsonStr, CoinVolume2::class.java
            )


            val date = Date(coinVolumeSpData.data[coinVolumeSpData.data.size - 1].openTime)
            val format = SimpleDateFormat("yyyy.MM.dd")
            var spDayStr = format.format(date)

            val currentDay = Date(getTodayStartTime())
            var currentDayStr = format.format(currentDay)


            if (spDayStr == currentDayStr) {
                showData(coin, coinVolumeSpData.data)
                return
            }
        }

        Thread(){
            getFromAPi(coin)
        }.start()

//        GlobalScope.launch {
//
//            withContext(Dispatchers.IO) {
//                getFromAPi(coin)
//            }
//        }
    }


    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    private fun getFromAPi(coin: String) {
        runOnUiThread {
            tvTitle.text = "loading"
        }


        var list = syncRequestClient.getCandlestick(
            coin,
            CandlestickInterval.DAILY,
            null,
            null,
            360
        )


        var coinVolume2 = CoinVolume2()
        coinVolume2.coinName = coin
        coinVolume2.data = list
        var datas = list
        var coinVolumeJsonStr = Gson().toJson(coinVolume2)
        SharedPreferenceUtil.saveData(
            AppController.instance.applicationContext,
            coin,
            coinVolumeJsonStr
        )

        runOnUiThread {
            showData(coin, datas)
        }

    }

    private fun showData(coin: String, datas: List<Candlestick>) {
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
            var lastMax = Gson().fromJson(lastMaxStr, Candlestick::class.java)
            var lastMin = Gson().fromJson(lastMinStr, Candlestick::class.java)

            val date = Date(lastMax.openTime.toLong())
            val format = SimpleDateFormat("yyyy.MM.dd")
            var day = format.format(date)

            str.append("Max: ${day} : ${lastMax.volume.toString().getMoneyFormat()} \n")

            val date2 = Date(lastMin.openTime.toLong())
            var day2 = format.format(date2)

            str.append("Min: ${day2} : ${lastMin.volume.toString().getMoneyFormat()} \n")
        }


        var preVolumeStr = ""

        var datas = datas.sortedByDescending { it.openTime }
        for (index in datas.indices) {
            var data = datas.get(index)
            var rateSpan = SpannableStringBuilder("")
            var volumeStr = data.volume

            if (index < datas.size - 1) {
                if (lastMaxStr.isEmpty()) {
                    lastMaxStr = Gson().toJson(data)

                } else {
                    var lastMax = Gson().fromJson(lastMaxStr, Candlestick::class.java)

                    if (BigDecimal(lastMax.volume.toString()) < volumeStr) {
                        lastMaxStr = Gson().toJson(data)
                    }

                }
                if (volumeStr > BigDecimal.ZERO) {
                    if (lastMinStr.isEmpty()) {
                        lastMinStr = Gson().toJson(data)

                    } else {
                        var lastMin = Gson().fromJson(lastMinStr, Candlestick::class.java)

                        if (BigDecimal(lastMin.volume.toString()) > volumeStr) {
                            lastMinStr = Gson().toJson(data)
                        }
                    }
                }
            }

            if (index!=datas.size-1){
                preVolumeStr =  datas.get(index+1).volume.toString()
            }


            if (!preVolumeStr.isEmpty() && preVolumeStr.toBigDecimal() > BigDecimal.ZERO) {

                var preValue = preVolumeStr.toBigDecimal()
                var currentValue = volumeStr

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
            preVolumeStr = volumeStr.toString()

            val date = Date(data.openTime.toLong())
            val format = SimpleDateFormat("yyyy.MM.dd")
            var day = format.format(date)

            str.append("${day} : ${volumeStr.toString().getMoneyFormat()} ")
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


    private fun initAction() {

        request.setOnClickListener {

            var coin = etCoin.text.toString()

            if (coin.isNotEmpty()) {

                getData(coin+"usdt")
            }


        }

    }


}

