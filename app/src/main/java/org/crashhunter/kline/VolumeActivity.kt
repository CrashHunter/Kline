package org.crashhunter.kline

import CoinVolume
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_volume.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


class VolumeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_volume)


        initAction()
//        OKHTTPRequest()


        val retrofit = Retrofit.Builder()
            .baseUrl("https://min-api.cryptocompare.com/data/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GitHubService::class.java)


        val call: Call<CoinVolume?>? = service.listRepos("BTC")


        call!!.enqueue(object : Callback<CoinVolume?> {

            override fun onResponse(call: Call<CoinVolume?>, response: Response<CoinVolume?>) {

                var str = StringBuilder()
                str.append("BTC: \n")
//                str.append(response.raw().body?.string() + " \n")
                for (data in response.body()?.data!!) {

                    var volume = data.topTierVolumeTotal.toString()

                    str.append("  $volume \n")


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

    }


    interface GitHubService {
        //https://min-api.cryptocompare.com/data/symbol/histoday?fsym=BTC&tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d
        @GET("symbol/histoday?tsym=USD&limit=10&api_key=4789529a8c5e2a2e26d4c665fa74c50d497c8971a5f1a6785d2a556da615d57d")
        fun listRepos(@Query("fsym") fsymCoin: String): Call<CoinVolume?>?
    }
}
