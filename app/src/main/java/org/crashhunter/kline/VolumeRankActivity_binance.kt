package org.crashhunter.kline

import CoinVolume2
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_volume.tvTitle
import kotlinx.android.synthetic.main.activity_volume_rank.*
import kotlinx.coroutines.*
import org.crashhunter.kline.data.SharedPreferenceUtil
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis


class VolumeRankActivity_binance : AppCompatActivity() {

    var coinList = Constant.contractCoins
    var coinsVolume = ArrayList<CoinVolume2>()
    var allStr = SpannableStringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume_rank)


        initAction()
//        OKHTTPRequest()

        tvTitle.text = "loading"


        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0
//                    var n  = ArrayList<Deferred<Int>>(10)
//                    n.add(Deferred)
                    for (index in coinList.indices) {
                        var n = async {
                            getData2(coinList[index])
                        }
                    }
//                    for(x in n){
//                        amount+=x.await()
//                    }
//                    amount+=x.await()
                    amount
                }
                Log.d("sss", sum.toString())
            }
            Log.d("sss", time.toString())
            runOnUiThread {

                coinsVolume.sortByDescending {
                    if (it.data.size > 2) {
                        it.data[it.data.size - 2].divide
                    } else {
                        it.data[it.data.size - 1].divide
                    }
                }

                for (coin in coinsVolume) {
                    Log.d("sss", "${coin.coinName} ${coin.data.size}")
                    showData(coin.coinName, coin)
                }
                Log.d("sss", " tvTitle.text = allStr")
                tvTitle.text = allStr
            }

        }


    }

    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    private suspend fun getData2(coinName: String): Int {
        try {

            runOnUiThread {

                tvTitle.text = "loading $coinName"

            }

            var list = syncRequestClient.getCandlestick(
                coinName,
                CandlestickInterval.DAILY,
                null,
                null,
                6
            )

            var coin = CoinVolume2()
            coin!!.coinName = coinName
            coin.data = list
            coinsVolume.add(coin)
            calculateDivide(coinName, coin)
        } catch (e: Exception) {
            Log.e("sss", e.printStackTrace().toString())
        }

        return 1
    }


    private fun calculateDivide(coin: String, coinVolume: CoinVolume2) {


        var preVolumeStr = ""
        var datas = coinVolume.data

        for (index in datas.indices) {
            if (index == datas.size - 1) {
                break
            }

            var data = datas.get(index)
            var volumeStr = data.volume


            if (!preVolumeStr.isEmpty() && preVolumeStr.toBigDecimal() > BigDecimal.ZERO) {

                var preValue = preVolumeStr.toBigDecimal()
                var currentValue = volumeStr

                var diff = currentValue.minus(preValue)


                var divide = diff.divide(preValue, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)

                data.divide = divide


            }
            preVolumeStr = volumeStr.toString()

        }


    }

    private fun showData(coin: String, coinVolume: CoinVolume2) {
//        tvTitle.text = ""
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


        var preVolumeStr = ""
        var datas = coinVolume.data

        for (index in datas.indices) {
            if (index == datas.size - 1) {
                break
            }

            var data = datas.get(index)
            var rateSpan = SpannableStringBuilder("")
            var volumeStr = data.volume

            if (index < datas.size - 1) {
                if (lastMaxStr.isEmpty()) {
                    lastMaxStr = Gson().toJson(data)

                } else {
                    var lastMax = Gson().fromJson(lastMaxStr, Candlestick::class.java)

                    if (lastMax.volume < volumeStr) {
                        lastMaxStr = Gson().toJson(data)
                    }

                }
                if (volumeStr > BigDecimal.ZERO) {
                    if (lastMinStr.isEmpty()) {
                        lastMinStr = Gson().toJson(data)

                    } else {
                        var lastMin = Gson().fromJson(lastMinStr, Candlestick::class.java)

                        if (lastMin.volume > volumeStr) {
                            lastMinStr = Gson().toJson(data)
                        }
                    }
                }
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

        allStr.append(str)

    }


    private fun initAction() {
        Single?.setOnClickListener {

            startActivity(Intent(this, VolumeActivity_binance::class.java))

        }

    }

}

