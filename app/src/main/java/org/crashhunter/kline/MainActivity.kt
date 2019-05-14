package org.crashhunter.kline

import android.os.Bundle
import android.os.Handler
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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


    var sortByType = "oneDay"


    var holdList = arrayListOf(
        "ATOM", "BNB", "BTT", "CELR", "DCR",
        "ENJ", "LSK", "MFT", "MIOTA", "NANO",
        "NEO", "ONT", "REP", "TRUE", "TRX",
        "WAVES", "ZRX", "ADA", "IOST"

    )
    var stableList = arrayListOf(
        "USDT", "DAI", "TUSD", "USDC", "BITCNY",
        "PAX", "GUSD"
    )

    var specialList = arrayListOf(
        "PAI Project Pai", "BTT", "FET", "MATIC"
    )

    var candidateist = arrayListOf(
        "LINK", "XLM", "LSK", "NANO", "ADA",
        "HT", "THETA", "BAT", "ARK", "PAI Project Pai",
        "ONT", "GXC", "GVT", "ATOM Cosmos",
        "DASH", "BCH", "BNB", "XTZ", "MKR",
        "ELF", "QKC", "BTM", "OKB", "PIVX",
        "RDN", "NEO", "QTUM", "STEEM", "VET",
        "ICX", "OST", "LRC", "AION", "POLY",
        "KNC", "GNT", "MIOTA", "MFT", "XMR",
        "ZRX", "BNT", "DATA Streamr DATAcoin",
        "IOST", "TRX", "AE", "BNB", "STORJ",
        "LOOM", "ZIL", "MANA", "INS", "ABT",
        "XRP", "HOT", "MCO", "RVN", "SNT"

    )

    //foreign
    var foreignList = arrayListOf(
        "BSV", "BCD", "ZEC", "BTS", "XEM",
        "OMG", "IGNIS", "EMC2", "COSM", "ETC",
        "PLC", "BEAM", "INB", "NET", "MEDX",
        "MXM", "EDR", "XTZ", "MHC", "LA",
        "KCS", "VTC", "DGB", "ANKR", "DEX",
        "MONA", "PHX", "BZ", "GRIN", "BIX",
        "SOLVE", "ORBS", "AERGO"

    )

    //bad code / low avg. volume
    var badCoinList = arrayListOf(
        "ABBC", "DMT", "BTG", "RLC",
        "GRS", "XZC", "CVC", "META", "ETP",
        "AGI", "SPND", "PPT", "CRO", "NEXO",
        "CNX", "VIA", "DENT", "XVG", "KMD",
        "MDA", "NAS", "CMT", "ARN", "WTC",
        "FUEL", "WAN", "WABI", "MTL",
        "SMART", "DTA", "RFR", "MOC", "IQ",
        "STORM", "NULS", "ETN", "MITH", "OCN",
        "GTC", "POWR", "GTO", "EVX", "REN",
        "QLC", "XAS", "ADX", "MTH",
        "OAX", "SNGLS", "VIB", "ETHOS", "DLT",
        "TNB", "AMB", "TTC", "LAMB", "TRIO",
        "SWFTC", "HPB", "ITC", "LBA", "RNT",
        "BHP", "KCASH", "NEW", "RCN"

    )


    var handler: Handler = Handler()


    var volumeMax = 9999 * 1000000

    var volumMin = 8 * 1000000

    val capDivider = 100 * 1000000

    val topNum = 0



    var allCoinList = ArrayList<CoinInfo>()

    lateinit var currentCoinList: List<CoinInfo>
    lateinit var latestCoinList: List<CoinInfo>

    lateinit var diffs: List<CoinInfo>

    var titleStr = StringBuffer()
    var contextStr = SpannableStringBuilder()

    var volumeEnoughNum = 0

    private var latestCoinListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATEST_COIN_LIST,
        ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        supportActionBar?.hide()

        setContentView(R.layout.activity_main)

        volumeEnoughNum = 0

        Log.e("latestCoinListGet", latestCoinListJsonStr)

        getData()

    }

    private fun getData() {

        tvTitle.text = "Loading..."

        initData()

        object : Thread() {
            override fun run() {
                super.run()
                try {


                    var urlStr = "https://coinmarketcap.com/all/views/all/"
//                    var urlStr = "https://www.baidu.com"
                    //                    titleStr.append(urlStr)


//                    val httpUtils = HttpUtils.instance
//                    httpUtils.timeout = 30000
//                    httpUtils.waitForBackgroundJavaScript = 30000
//                    val doc = httpUtils.getHtmlPageResponseAsDocument(urlStr)

//                    val webClient = WebClient(BrowserVersion.CHROME)
//                    val htmlpage: HtmlPage = webClient.getPage(urlStr)
//
//                    val pageAsXml = htmlpage.asXml()
//                    val doc = Jsoup.parse(pageAsXml, urlStr)

//                    val driver = FirefoxDriver()
//                    driver.get(urlStr)
//                    var list = driver.findElements("//*[@id=\"id-waves\"]")

                    val doc = Jsoup.connect(urlStr).get()
                    contextStr.append(doc.toString())

                    var icons = doc.select("#currencies-all > tbody > tr")
                    Log.e("icons size", icons.size.toString())

                    for (i in 0 until icons.size) {

                        if (icons[i].select("td").size < 10) {
                            Log.e("invaild item", icons[i].toString())
                            //                            titleStr.append("\n invaild item: ${icons[i]} \n")
                            continue
                        }


                        var rank = icons[i].select("td")[0]
                        //                        Log.e("icon rank", rank.text())
                        var name = icons[i].select("td")[1]
                        //                        Log.e("icon name", name.text())

                        var cap = icons[i].select("td")[3]
                        //                        Log.e("icon cap", cap.text())
                        var capStr = cap.text().replace("$", "").replace(",", "")

                        var volume = icons[i].select("td")[6].select("a")
                        //                        Log.e("icon volume", volume.text())
                        var volumeStr = volume.text().replace("$", "").replace(",", "")


                        var oneDayPercent = icons[i].select("td")[8]
                        //                        Log.e("icon oneDayPercent", oneDayPercent.text())

                        var sevenDaysPercent = icons[i].select("td")[9]
                        //                        Log.e("icon sevenDaysPercent", sevenDaysPercent.text())


                        var iconInfo = CoinInfo()
                        iconInfo.name = name.text()
                        iconInfo.rank = rank.text().toLong()
                        iconInfo.volume = volumeStr.toLong()
                        iconInfo.cap = capStr.toLong()
                        iconInfo.oneDayPercent =
                            oneDayPercent.text().replace("%", "").replace("?", "").parseDouble()
                        iconInfo.sevenDaysPercent =
                            sevenDaysPercent.text()
                                .replace("%", "")
                                .replace("?", "")
                                .replace(">", "")
                                .parseDouble()
                        allCoinList.add(iconInfo)


                    }

                    showAllCap()


                } catch (e: IOException) {
                    //                    e.printStackTrace()
                }

            }
        }.start()
    }

    private fun initData() {

        volumeMax = 9999 * 1000000
        volumMin = 8 * 1000000

        allCoinList = ArrayList<CoinInfo>()
        currentCoinList = ArrayList<CoinInfo>()
        latestCoinList = ArrayList<CoinInfo>()

        titleStr = StringBuffer()
        contextStr = SpannableStringBuilder()

        volumeEnoughNum = 0

    }

    private fun refreshUI() {
        handler.post {

            tvTitle.text = titleStr

            tvContext.text = contextStr

        }
    }

    private fun displayDiffs(
        currentCoinList: List<CoinInfo>
    ) {


        contextStr.append("-----------Diffs----------- \n")

        for (i in 0 until diffs.size) {
            var item = diffs[i]


            if (currentCoinList.contains(item)) {
                var index = currentCoinList.indexOf(item)
                var diffNameStr = "+ ${index + 1}.${item.name} No.${item.rank} \n"
                var diffNameSpan = SpannableStringBuilder(diffNameStr)

                if (filterCandidate(item.name)) {
                    diffNameSpan.setSpan(
                        ForegroundColorSpan(getColor(android.R.color.holo_blue_light)),
                        0,
                        diffNameStr.length - 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                } else {
                    diffNameSpan.setSpan(
                        ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                        0,
                        diffNameStr.length - 1,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }

                contextStr.append(diffNameSpan)
            } else {
                var diffNameStr = "- ${item.name}\n"
                var diffNameSpan = SpannableStringBuilder(diffNameStr)
                diffNameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
                    0,
                    diffNameStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                contextStr.append(diffNameSpan)
            }


        }
    }

    private fun getdiffs(
        currentCoinList: List<CoinInfo>,
        latestCoinList: List<CoinInfo>
    ) {
        diffs = latestCoinList + currentCoinList
        diffs = diffs.groupBy { it.name }
            .filter { it.value.size == 1 }
            .flatMap { it.value }
    }

    private fun displayCoinList(
        coinInfos: List<CoinInfo>
    ) {
        for (i in 0 until coinInfos.size) {
            var item = coinInfos[i]

            var indexStr = "${i + 1}: "
            if (diffs.contains(item)) {
                var indexSpan = SpannableStringBuilder(indexStr)
                indexSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
                    0,
                    indexStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                contextStr.append(indexSpan)
            } else {
                contextStr.append(indexStr)
            }


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

            } else if (filterCandidate(item.name)) {
                var nameStr = item.name + " "
                var nameSpan = SpannableStringBuilder(nameStr)
                nameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_blue_light)),
                    0,
                    nameStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                contextStr.append(nameSpan)
            } else if (filterSpecial(item.name)) {
                var nameStr = item.name + " "
                var nameSpan = SpannableStringBuilder(nameStr)
                nameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_purple)),
                    0,
                    nameStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                contextStr.append(nameSpan)
            } else {
                contextStr.append(item.name + " ")
            }


            contextStr.append("No.${item.rank} | ")
            // contextStr.append(StringUtils.getFormattedVolume(item.cap.toString()) + " ")
            var volumeStr = StringUtils.getFormattedVolume(item.volume.toString()) + " "
            if (item.volume > volumMin) {
                var volumeSpan = SpannableStringBuilder(volumeStr)
                volumeSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_orange_light)),
                    0,
                    volumeStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                contextStr.append(volumeSpan)
            } else {
                contextStr.append(volumeStr)
            }


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

    private fun filterSpecial(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return specialList.any { it == symbol || it == name }
    }

    private fun filterCandidate(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return candidateist.any { it == symbol || it == name }
    }

    private fun filterHold(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return holdList.any { it == symbol || it == name }
    }

    private fun filterStable(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return stableList.any { it == symbol || it == name }
    }


    private fun filterBlack(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return foreignList.any { it == symbol || it.startsWith(name) }
                || badCoinList.any { it == symbol || it.startsWith(name) }
    }


    private fun filterTop(rank: Long): Boolean {

        return rank <= topNum

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        inflater.inflate(R.menu.sort_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.refresh -> {
                getData()
                return true
            }
            R.id.allcap -> {
                showAllCap()
                return true
            }
            R.id.bigcap -> {
                showBigCap()
                return true
            }
            R.id.smallcap -> {
                showSmallCap()
                return true
            }

            R.id.smallVolume -> {
                showSmallVolume()
                return true
            }

            R.id.myHolding -> {
                showMyHolding()
                return true
            }
            R.id.candidate -> {
                showCandidate()
                return true
            }
            //SORT BY
            R.id.name -> {
                sortByType = "name"
                return true
            }
            R.id.volume -> {
                sortByType = "volume"
                return true
            }
            R.id.oneDay -> {
                sortByType = "oneDay"
                return true
            }
            R.id.sevenDay -> {
                sortByType = "sevenDay"
                return true
            }
            R.id.rank -> {
                sortByType = "rank"
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showCandidate() {
        contextStr = SpannableStringBuilder()

        var list = allCoinList.filter { filterCandidate(it.name) || filterHold(it.name) || filterSpecial(it.name) }

        list = sortList(list)

        displayCoinList(list)

        refreshUI()

    }


    private fun showMyHolding() {
        contextStr = SpannableStringBuilder()

        var list = allCoinList.filter { filterHold(it.name) }

        list = sortList(list)

        displayCoinList(list)

        refreshUI()

    }

    private fun sortList(list: List<CoinInfo>): List<CoinInfo> {
        var list1 = list
        if (sortByType == "name") {
            list1 = list1.sortedBy { it.name }
        } else if (sortByType == "volume") {
            list1 = list1.sortedBy { it.volume }
        } else if (sortByType == "oneDay") {
            list1 = list1.sortedBy { it.oneDayPercent }
        } else if (sortByType == "sevenDay") {
            list1 = list1.sortedBy { it.sevenDaysPercent }
        } else if (sortByType == "rank") {
            list1 = list1.sortedBy { it.rank }
        }
        return list1
    }


    private fun showSmallVolume() {

        volumeMax = 8 * 1000000
        volumMin = 5 * 1000000

        contextStr = SpannableStringBuilder()

        var smallVolumeCoinList = allCoinList.filter { it.volume in (volumMin + 1)..(volumeMax - 1) }

        smallVolumeCoinList =
            smallVolumeCoinList.filterNot { filterStable(it.name) || filterBlack(it.name) || filterTop(it.rank) }
                .sortedBy { it.sevenDaysPercent }

        displayCoinList(smallVolumeCoinList)

        refreshUI()

    }

    private fun showAllCap() {

        volumeMax = 9999 * 1000000
        volumMin = 8 * 1000000

        titleStr = StringBuffer()

        titleStr.append("All: ${allCoinList.size} ")

        currentCoinList = allCoinList.filter { it.volume in (volumMin + 1)..(volumeMax - 1) }
        titleStr.append("VolEnough: ${currentCoinList.size} ")

        currentCoinList =
            currentCoinList.filterNot { filterStable(it.name) || filterBlack(it.name) || filterTop(it.rank) }
                .sortedBy { it.sevenDaysPercent }
        titleStr.append("Filter: ${currentCoinList.size}")

        //get latestCoinList
        if (latestCoinListJsonStr.isNotEmpty()) {
            latestCoinList = Gson().fromJson(latestCoinListJsonStr, object : TypeToken<List<CoinInfo>>() {}
                .type) as List<CoinInfo>
        }


        //save latestCoinList
        var jsonList = Gson().toJson(currentCoinList)
        Log.e("jsonListSave", jsonList)
        latestCoinListJsonStr = jsonList


        contextStr = SpannableStringBuilder()


        getdiffs(currentCoinList, latestCoinList)

        currentCoinList = sortList(currentCoinList)

        displayCoinList(currentCoinList)
        displayDiffs(currentCoinList)

        refreshUI()

    }

    private fun showSmallCap() {

        var smallCoinList = currentCoinList.filter { it.cap < capDivider }
        var smallLatestCoinList = latestCoinList.filter { it.cap < capDivider }
        contextStr = SpannableStringBuilder()

        getdiffs(smallCoinList, smallLatestCoinList)
        displayCoinList(smallCoinList)
        displayDiffs(smallCoinList)

        refreshUI()

    }

    private fun showBigCap() {

        var bigCoinList = currentCoinList.filter { it.cap >= capDivider }
        var bigLatestCoinList = latestCoinList.filter { it.cap >= capDivider }
        contextStr = SpannableStringBuilder()

        getdiffs(bigCoinList, bigLatestCoinList)
        displayCoinList(bigCoinList)
        displayDiffs(bigCoinList)

        refreshUI()

    }
}
