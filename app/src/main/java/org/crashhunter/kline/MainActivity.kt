package org.crashhunter.kline

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import java.io.IOException


class MainActivity : AppCompatActivity() {


    var handler: Handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        object : Thread() {
            override fun run() {
                super.run()
                try {
                    var contextStr = StringBuffer()

                    var urlStr = "https://coinmarketcap.com/all/views/all/"
                    val doc = Jsoup.connect(urlStr).get()

//                    contextStr.append(doc.toString())

                    var icons = doc.select("#currencies-all > tbody > tr")
                    Log.e("icons size", icons.size.toString())


//                    Log.e("icon ", icons[0].select("td").toString())


                    for (i in 0 until icons.size) {

                        var index = icons[i].select("td")[0]
                        Log.e("icon index", index.text())
//                        contextStr.append(index.text() + "-")
//                        Log.e("icon ", icons[i].select("td").toString())
                        var name = icons[i].select("td")[1].select("a")
//                        contextStr.append(name.text() + "-")
                        Log.e("icon name", name.text())

                        if (icons[i].select("td").size < 7) {
                            Log.e("icon name", "----------------" + name.text())
                        } else {
                            var volume = icons[i].select("td")[6].select("a")
                            Log.e("icon volume", volume.text())
//                            contextStr.append(volume.text() + "\n")

                            var volumeStr = volume.text().replace("$", "").replace(",", "")

                            if (volumeStr.toLong() > 10000000) {
                                contextStr.append(index.text() + "-")
                                contextStr.append(name.text() + "-")
                                contextStr.append(volumeStr + "\n")
                            }


                        }


                    }


                    handler.post {

                        url.text = urlStr

                        context.text = contextStr

                    }

                } catch (e: IOException) {
//                    e.printStackTrace()
                }

            }
        }.start()


    }
}
