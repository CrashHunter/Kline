package org.crashhunter.kline.oneline

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import kotlinx.android.synthetic.main.activity_data_analysis.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.Constant
import org.crashhunter.kline.R
import kotlin.system.measureTimeMillis

class ContractListActivity : AppCompatActivity() {

    var forceRefresh = false

    var stringBuilder = SpannableStringBuilder()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contract_list)

        getData()

    }


    private fun getData() {
        tvTitle.text = "Loading..."

        object : Thread() {
            override fun run() {
                super.run()

                getAllCoins()

            }
        }.start()
    }

    private fun getAllCoins() {

        stringBuilder.clear()

        var data = syncRequestClient.getExchangeInformation()
        data.symbols.sortBy { it.symbol }

        for (index in data.symbols.indices) {
            var symbol = data.symbols.get(index)
            stringBuilder.append("   ${index + 1}. ${symbol.symbol}")
            stringBuilder.append("\n")

            //if(Constant.coinList.contains(symbol.symbol)){
            //    Constant.coinList.add(symbol.symbol)
            //}

        }

        runOnUiThread {

            tvTitle.text = stringBuilder

        }

    }


    val options = RequestOptions()
    val syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )


}