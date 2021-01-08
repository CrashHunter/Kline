package org.crashhunter.kline

import CoinVolume
import Data
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_volume.*
import kotlinx.coroutines.*
import org.crashhunter.kline.data.SharedPreferenceUtil
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.measureTimeMillis


class VolumeRankActivity : AppCompatActivity() {

    var coinList = ArrayList<String>()
    var coinsVolume = ArrayList<CoinVolume>()
    var allStr = SpannableStringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)


        initAction()
//        OKHTTPRequest()

        tvTitle.text = "loading"

        setCoinList()

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
                    }else{
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

    private suspend fun getData2(coinName: String): Int {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://min-api.cryptocompare.com/data/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(KlineService::class.java)
            val call: Call<CoinVolume?>? = service.queryVolume2(coinName)
            var response = call!!.execute()
            var datas = response.body()?.data!!
            Log.d("sss", "showData:$coinName")
            runOnUiThread {

                tvTitle.text = "Got $coinName"

            }
            var coin = response.body()
            coin!!.coinName = coinName
            coinsVolume.add(coin)
            calculateDivide(coinName, coin)
        } catch (e: Exception) {
            Log.e("sss", coinName + ":" + e.localizedMessage)
        }

        return 1
    }


    private fun setCoinList() {
        coinList.add("AAVE")
        coinList.add("ADA")
        coinList.add("ALGO")
        coinList.add("ATOM")
        coinList.add("AVAX")

        coinList.add("BAL")
        coinList.add("BAND")
        coinList.add("BAT")
        coinList.add("BCH")
        coinList.add("BNB")
        coinList.add("BTC")
        coinList.add("BZRX")

        coinList.add("COMP")
        coinList.add("CRV")

        coinList.add("DASH")
        coinList.add("DEFI")
        coinList.add("DOGE")
        coinList.add("DOT")

        coinList.add("EGLD")
        coinList.add("EOS")
        coinList.add("ETC")
        coinList.add("ETH")

        coinList.add("FIL")
        coinList.add("FLM")
        coinList.add("FTM")

        coinList.add("HNT")

        coinList.add("ICX")
        coinList.add("IOST")
        coinList.add("IOTA")

        coinList.add("KAVA")
        coinList.add("KNC")

        coinList.add("LINK")
        coinList.add("LRC")
        coinList.add("LTC")

        coinList.add("MKR")

        coinList.add("NEO")

        coinList.add("OMG")
        coinList.add("ONT")

        coinList.add("QTUM")

        coinList.add("REN")
        coinList.add("RLC")
        coinList.add("RSR")
        coinList.add("RUNE")

        coinList.add("SNX")
        coinList.add("SOL")
        coinList.add("SRM")
        coinList.add("STORJ")
        coinList.add("SUSHI")
        coinList.add("SXP")

        coinList.add("THETA")
        coinList.add("TOMO")
        coinList.add("TRB")
        coinList.add("TRX")

        coinList.add("UNI")

        coinList.add("VET")

        coinList.add("WAVES")

        coinList.add("XLM")
        coinList.add("XMR")
        coinList.add("XRP")
        coinList.add("XTZ")

        coinList.add("YFI")
        coinList.add("YFII")

        coinList.add("ZEC")
        coinList.add("ZIL")
        coinList.add("ZRX")
    }

    private fun calculateDivide(coin: String, coinVolume: CoinVolume) {


        var preVolumeStr = ""
        var datas = coinVolume.data

        for (index in datas.indices) {
            if (index == datas.size - 1) {
                break
            }

            var data = datas.get(index)
            var volumeStr = data.totalVolumeTotal


            if (!preVolumeStr.isEmpty() && preVolumeStr.toBigDecimal() > BigDecimal.ZERO) {

                var preValue = preVolumeStr.toBigDecimal()
                var currentValue = volumeStr.toBigDecimal()

                var diff = currentValue.minus(preValue)


                var divide = diff.divide(preValue, 4, BigDecimal.ROUND_HALF_UP) * BigDecimal(100)

                data.divide = divide


            }
            preVolumeStr = volumeStr

        }


    }

    private fun showData(coin: String, coinVolume: CoinVolume) {
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

//        if (lastMaxStr.isNotEmpty() && lastMinStr.isNotEmpty()) {
//            var lastMax = Gson().fromJson(lastMaxStr, Data::class.java)
//            var lastMin = Gson().fromJson(lastMinStr, Data::class.java)
//
//            val date = Date(lastMax.time.toLong() * 1000)
//            val format = SimpleDateFormat("yyyy.MM.dd")
//            var day = format.format(date)
//
//            str.append("Max: ${day} : ${lastMax.totalVolumeTotal.getMoneyFormat()} \n")
//
//            val date2 = Date(lastMin.time.toLong() * 1000)
//            var day2 = format.format(date2)
//
//            str.append("Min: ${day2} : ${lastMin.totalVolumeTotal.getMoneyFormat()} \n")
//        }


        var preVolumeStr = ""
        var datas = coinVolume.data

        for (index in datas.indices) {
            if (index == datas.size - 1) {
                break
            }

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
                if (BigDecimal(volumeStr) > BigDecimal.ZERO) {
                    if (lastMinStr.isEmpty()) {
                        lastMinStr = Gson().toJson(data)

                    } else {
                        var lastMin = Gson().fromJson(lastMinStr, Data::class.java)

                        if (BigDecimal(lastMin.totalVolumeTotal) > BigDecimal(volumeStr)) {
                            lastMinStr = Gson().toJson(data)
                        }
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

        allStr.append(str)

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


    }


    interface KlineService {
        //https://min-api.cryptocompare.com/data/symbol/histoday?fsym=BTC&tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d
        @GET("symbol/histoday?tsym=USD&limit=90&api_key=e607037237ae0f8fce5397d2dd69253d0321f07a05aa88c0206bd36b5e3fbec7")
        fun queryVolume(@Query("fsym") fsymCoin: String): Call<CoinVolume?>?

        @GET("symbol/histoday?tsym=USD&limit=3&api_key=e607037237ae0f8fce5397d2dd69253d0321f07a05aa88c0206bd36b5e3fbec7")
        fun queryVolume2(@Query("fsym") fsymCoin: String): Call<CoinVolume?>?
    }
}

