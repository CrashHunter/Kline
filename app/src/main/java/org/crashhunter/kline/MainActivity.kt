package org.crashhunter.kline

import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.crashhunter.kline.data.IconInfo
import org.crashhunter.kline.utils.StringUtils
import org.jsoup.Jsoup
import java.io.IOException


class MainActivity : AppCompatActivity() {


    var handler: Handler = Handler()


    val volumMin = 9 * 1000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : Thread() {
            override fun run() {
                super.run()
                try {

                    var iconInfos = ArrayList<IconInfo>()

                    var titleStr = StringBuffer()
                    var contextStr = SpannableStringBuilder()

                    var urlStr = "https://coinmarketcap.com/all/views/all/"
//                    titleStr.append(urlStr)

                    val doc = Jsoup.connect(urlStr).get()
//                    contextStr.append(doc.toString())

                    var icons = doc.select("#currencies-all > tbody > tr")
                    Log.e("icons size", icons.size.toString())

                    for (i in 0 until icons.size) {

                        var rank = icons[i].select("td")[0]
                        Log.e("icon rank", rank.text())
                        var name = icons[i].select("td")[2]
                        Log.e("icon name", name.text())

                        var cap = icons[i].select("td")[3]
                        Log.e("icon cap", cap.text())
                        var capStr = cap.text().replace("$", "").replace(",", "")

                        if (icons[i].select("td").size < 7) {
                            Log.e("icon name", "----------------" + name.text())
                        } else {
                            var volume = icons[i].select("td")[6].select("a")
                            Log.e("icon volume", volume.text())
                            var volumeStr = volume.text().replace("$", "").replace(",", "")


                            if (icons[i].select("td").size > 10) {

                                var oneDayPercent = icons[i].select("td")[8]
                                Log.e("icon oneDayPercent", oneDayPercent.text())

                                var sevenDaysPercent = icons[i].select("td")[9]
                                Log.e("icon sevenDaysPercent", sevenDaysPercent.text())

                                if (volumeStr.toLong() > volumMin) {

                                    if (filterStable(name.text()) || filterTop(rank.text())) {
                                        continue
                                    }


                                    var iconInfo = IconInfo()
                                    iconInfo.name = name.text()
                                    iconInfo.rank = rank.text()
                                    iconInfo.volume = volumeStr.toLong()
                                    iconInfo.cap = capStr.toLong()
                                    iconInfo.oneDayPercent = oneDayPercent.text().replace("%", "").toDouble()
                                    iconInfo.sevenDaysPercent = sevenDaysPercent.text().replace("%", "").toDouble()
                                    iconInfos.add(iconInfo)

//                                    contextStr.append(index.text() + "-")
//                                    contextStr.append(name.text() + "-")
//                                    contextStr.append(sevenDaysPercent.text() + "-")
//                                    contextStr.append(volumeStr + "\n")
                                }

                            }


                        }


                    }

                    titleStr.append("Size: ${iconInfos.size}")
                    iconInfos.sortBy { it.sevenDaysPercent }

                    for (i in 0 until iconInfos.size) {
                        var item = iconInfos[i]
                        contextStr.append("${i + 1}: ")
                        contextStr.append(item.name + " ")
                        contextStr.append("No.${item.rank} ")
//                        contextStr.append(StringUtils.getFormattedVolume(item.cap.toString()) + " ")
                        contextStr.append(StringUtils.getFormattedVolume(item.volume.toString()) + " ")


                        var oneDayPercent = item.oneDayPercent.toString() + " "
                        var oneDayPercentStr = SpannableStringBuilder(oneDayPercent)
                        if (item.oneDayPercent > 0) {
                            oneDayPercentStr.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_green_dark)), 0, oneDayPercent.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        } else {
                            oneDayPercentStr.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_red_light)), 0, oneDayPercent.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        }
                        contextStr.append(oneDayPercentStr)


                        var sevenDaysPercent = item.sevenDaysPercent.toString() + "\n"
                        var sevenDaysPercentStr = SpannableStringBuilder(sevenDaysPercent)
                        if (item.sevenDaysPercent > 0) {
                            sevenDaysPercentStr.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_green_dark)), 0, sevenDaysPercentStr.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        } else {
                            sevenDaysPercentStr.setSpan(ForegroundColorSpan(getColor(android.R.color.holo_red_light)), 0, sevenDaysPercentStr.length - 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        }
                        contextStr.append(sevenDaysPercentStr)
                    }


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

    private fun filterStable(name: String): Boolean {

        return when (name) {

            "USDT", "DAI", "TUSD", "USDC", "BITCNY" -> true

            else -> false
        }
    }


    private fun filterTop(rank: String): Boolean {

        return rank.toInt() <= 10

    }
}
