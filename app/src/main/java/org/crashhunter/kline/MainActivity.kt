package org.crashhunter.kline

import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import org.crashhunter.kline.data.BaseSharedPreference
import org.crashhunter.kline.data.CoinInfo
import org.crashhunter.kline.data.LATEST_COIN_LIST
import org.crashhunter.kline.utils.StringUtils
import org.jsoup.Jsoup
import java.io.IOException


class MainActivity : AppCompatActivity() {


    var handler: Handler = Handler()


    val volumMin = 10 * 1000000

    private var latestCoinListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATEST_COIN_LIST,
        ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Log.e("latestCoinListGet", latestCoinListJsonStr)

        object : Thread() {
            override fun run() {
                super.run()
                try {

                    var currentCoinList = ArrayList<CoinInfo>()

                    var titleStr = StringBuffer()
                    var contextStr = SpannableStringBuilder()

                    var urlStr = "https://coinmarketcap.com/all/views/all/"
//                    titleStr.append(urlStr)

                    val doc = Jsoup.connect(urlStr).get()
//                    contextStr.append(doc.toString())

                    var icons = doc.select("#currencies-all > tbody > tr")
                    Log.e("icons size", icons.size.toString())

                    for (i in 0 until icons.size) {

                        if (icons[i].select("td").size < 10) {
                            continue
                        }

                        var rank = icons[i].select("td")[0]
                        Log.e("icon rank", rank.text())
                        var name = icons[i].select("td")[2]
                        Log.e("icon name", name.text())

                        var cap = icons[i].select("td")[3]
                        Log.e("icon cap", cap.text())
                        var capStr = cap.text().replace("$", "").replace(",", "")

                        var volume = icons[i].select("td")[6].select("a")
                        Log.e("icon volume", volume.text())
                        var volumeStr = volume.text().replace("$", "").replace(",", "")


                        var oneDayPercent = icons[i].select("td")[8]
                        Log.e("icon oneDayPercent", oneDayPercent.text())

                        var sevenDaysPercent = icons[i].select("td")[9]
                        Log.e("icon sevenDaysPercent", sevenDaysPercent.text())

                        if (volumeStr.toLong() > volumMin) {

                            if (filterStable(name.text()) || filterBlack(name.text()) || filterTop(rank.text())) {
                                continue
                            }

                            var iconInfo = CoinInfo()
                            iconInfo.name = name.text()
                            iconInfo.rank = rank.text()
                            iconInfo.volume = volumeStr.toLong()
                            iconInfo.cap = capStr.toLong()
                            iconInfo.oneDayPercent = oneDayPercent.text().replace("%", "").toDouble()
                            iconInfo.sevenDaysPercent = sevenDaysPercent.text().replace("%", "").toDouble()
                            currentCoinList.add(iconInfo)


                        }

                    }

                    titleStr.append("Size: ${currentCoinList.size}")
                    currentCoinList.sortBy { it.sevenDaysPercent }



                    diaplayCoinList(currentCoinList, contextStr)


                    displayDiffs(currentCoinList, contextStr)


                    var jsonList = Gson().toJson(currentCoinList)
                    Log.e("jsonListSave", jsonList)
                    latestCoinListJsonStr = jsonList


                    handler.post {

                        url.text = titleStr

                        context.text = contextStr

                    }

                } catch (e: IOException) {
//                    e.printStackTrace()
                }

            }
        }.start()


    }

    private fun displayDiffs(
        currentCoinList: ArrayList<CoinInfo>,
        contextStr: SpannableStringBuilder
    ) {
        val latestCoinList = Gson().fromJson(latestCoinListJsonStr, object : TypeToken<List<CoinInfo>>() {}
            .type) as ArrayList<CoinInfo>

        var sum: List<CoinInfo> = latestCoinList + currentCoinList
        sum = sum.groupBy { it.name }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        contextStr.append("-----------Diffs----------- \n")

        for (i in 0 until sum.size) {
            var item = sum[i]
            var diffName = item.name + "\n"
            var diffNameStr = SpannableStringBuilder(diffName)

            if (currentCoinList.contains(item)) {
                diffNameStr.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    diffName.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                diffNameStr.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                    0,
                    diffName.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

            contextStr.append(diffNameStr)
        }
    }

    private fun diaplayCoinList(
        coinInfos: ArrayList<CoinInfo>,
        contextStr: SpannableStringBuilder
    ) {
        for (i in 0 until coinInfos.size) {
            var item = coinInfos[i]
            contextStr.append("${i + 1}: ")

            if (filterHold(item.name)) {

                var nameStr = item.name + " "
                var nameSpan = SpannableStringBuilder(nameStr)
                nameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
                    0,
                    nameStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                contextStr.append(nameSpan)

            } else {
                contextStr.append(item.name + " ")
            }


            contextStr.append("No.${item.rank} ")
            // contextStr.append(StringUtils.getFormattedVolume(item.cap.toString()) + " ")
            contextStr.append(StringUtils.getFormattedVolume(item.volume.toString()) + " ")


            var oneDayPercentStr = item.oneDayPercent.toString() + " "
            var oneDayPercentSpan = SpannableStringBuilder(oneDayPercentStr)
            if (item.oneDayPercent > 0) {
                oneDayPercentSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                    0,
                    oneDayPercentStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                oneDayPercentSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    oneDayPercentStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            contextStr.append(oneDayPercentSpan)


            var sevenDaysPercentStr = item.sevenDaysPercent.toString() + "\n"
            var sevenDaysPercentSpan = SpannableStringBuilder(sevenDaysPercentStr)
            if (item.sevenDaysPercent > 0) {
                sevenDaysPercentSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                    0,
                    sevenDaysPercentSpan.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            } else {
                sevenDaysPercentSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    sevenDaysPercentSpan.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            contextStr.append(sevenDaysPercentSpan)
        }
    }


    private fun filterHold(name: String): Boolean {

        return when (name) {

            "TRUE", "EOS", "AE", "POLY", "ZRX", "NANO", "MIOTA", "DCR",
            "OKB", "ABT", "SNT", "CMT", "IOST", "ELF" -> true

            else -> false
        }
    }

    private fun filterStable(name: String): Boolean {

        return when (name) {

            "USDT", "DAI", "TUSD", "USDC", "BITCNY", "PAX" -> true

            else -> false
        }
    }

    private fun filterBlack(name: String): Boolean {

        return when (name) {

            "ABBC", "XMR", "DMT", "BTG", "BSV", "BCD", "ZEC", "BTS",
            "XEM", "OMG", "IGNIS", "EMC2", "COSM", "RLC", "GRS" -> true

            else -> false
        }
    }


    private fun filterTop(rank: String): Boolean {

        return rank.toInt() <= 10

    }
}
