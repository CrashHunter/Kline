package org.crashhunter.kline.test

import android.util.Log
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_volume.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.crashhunter.kline.data.CoinMarketList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException


/**
 * Created by CrashHunter on 2021/4/25.
 */
internal object CoinMarketAPI {
    private const val apiKey = "716cf148-7b23-46ff-89b8-fb8b3d84e157"

    @JvmStatic
    fun main(args: Array<String>) {

        val retrofit = Retrofit.Builder()
                .baseUrl("https://pro-api.coinmarketcap.com/")
                .client(genericClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()


        val service = retrofit.create(ListService::class.java)

        val call: Call<CoinMarketList?>? = service.queryList()


        call!!.enqueue(object : Callback<CoinMarketList?> {

            override fun onResponse(call: Call<CoinMarketList?>, response: Response<CoinMarketList?>) {
                var datas = response.body()?.data!!
                var coinVolumeJsonStr = Gson().toJson(response.body())

                Log.d("sss", coinVolumeJsonStr);

            }

            override fun onFailure(call: Call<CoinMarketList?>, t: Throwable) {
                Log.d("sss", Log.getStackTraceString(t));

            }
        })
    }

    private fun genericClient(): OkHttpClient? {
        return OkHttpClient().newBuilder()
                .addInterceptor(object : Interceptor {
                    @Throws(IOException::class)
                    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                        val request: Request = chain.request()
                                .newBuilder()
                                .addHeader("X-CMC_PRO_API_KEY", apiKey)
                                .addHeader("Accept", "application/json")
                                .build()
                        return chain.proceed(request)
                    }
                })
                .build()
    }


    interface ListService {

        @GET("v1/cryptocurrency/listings/latest?start=1&limit=50&convert=USD")
        fun queryList(): Call<CoinMarketList?>?
    }
}