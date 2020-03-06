package org.crashhunter.kline

import CoinVolume
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_volume.*
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
        getFromRetrofit("USDT")


    }

    private fun getFromRetrofit(coin: String) {
        tvTitle.text = "loading"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://min-api.cryptocompare.com/data/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(KlineService::class.java)


        val call: Call<CoinVolume?>? = service.queryVolume(coin)


        call!!.enqueue(object : Callback<CoinVolume?> {

            override fun onResponse(call: Call<CoinVolume?>, response: Response<CoinVolume?>) {
                tvTitle.text = ""
                var str = SpannableStringBuilder()
                //                str.append(response.raw().body?.string() + " \n")

                str.append("$coin: \n")

                var preVolumeStr = ""

                for (data in response.body()?.data!!) {
                    var rateSpan = SpannableStringBuilder("")
                    var volumeStr = data.totalVolumeTotal

                    if (!preVolumeStr.isEmpty()) {

                        var preValue = preVolumeStr.toBigDecimal()
                        var currentValue = volumeStr.toBigDecimal()

                        var rate = currentValue / preValue

                        var divide = (rate - BigDecimal.ONE) * BigDecimal(100)

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

                tvTitle.text = str

            }

            override fun onFailure(call: Call<CoinVolume?>, t: Throwable) {

                tvTitle.text = t.localizedMessage

            }
        })
    }

    private fun OKHTTPRequest() {
        //        var client = OkHttpClient();
        //        var url =
        //            "https://min-api.cryptocompare.com/data/symbol/histoday?fsym=USDT&tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d"
        //        var request = Request.Builder()
        //            .url(url)
        //            .build();
        //
        //        object : Thread() {
        //            override fun run() {
        //                super.run()
        //                var response = client.newCall(request).execute()
        //                var json = response.body?.string()
        //                var data = Gson().fromJson(json, CoinVolume::class.java)
        //
        //                runOnUiThread {
        //                    var str = StringBuilder()
        //
        //                    str.append("BTC: \n")
        //
        //                    str.append(json)
        //                    str.append(data.toString())
        ////                    for (data in data.data) {
        ////
        ////                        var volume = data.topTierVolumeTotal.toString()
        ////
        ////                        str.append("  $volume \n")
        ////
        ////
        ////                    }
        //
        //                    tvTitle.text = str
        //
        //                }
        //
        //            }
        //        }.start()
    }

    private fun initAction() {

        request.setOnClickListener {

            var coin = etCoin.text.toString()

            if (coin.isNotEmpty()) {

                getFromRetrofit(coin)
            }


        }

    }


    interface KlineService {
        //https://min-api.cryptocompare.com/data/symbol/histoday?fsym=BTC&tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d
        @GET("symbol/histoday?tsym=USD&limit=30&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d")
        fun queryVolume(@Query("fsym") fsymCoin: String): Call<CoinVolume?>?
    }
}

