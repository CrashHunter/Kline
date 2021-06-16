package org.crashhunter.kline

import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_main.tvTitle
import org.crashhunter.kline.data.*
import org.crashhunter.kline.oneline.KeyLineActivity
import org.crashhunter.kline.test.CoinMarketAPI
import org.crashhunter.kline.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {

    val maximum = 999999999 * 1000000L
    var volumeMax = maximum
    val minimum = 2000 * 10000L
    var volumMin = minimum

    val capDivider = 100 * 1000000

    val topNum = -1

    var sortByType = "volume"

    //1 billion
    var SLevel = ArrayList<String>()

    //0.1 billion
    var ALevel = ArrayList<String>()

    // 10 million top 10
    var BLevel = ArrayList<String>()

    var leverList = ArrayList<String>()

    var stableList = arrayListOf(
        "DAI", "TUSD", "USDC", "BITCNY",
        "PAX", "GUSD", "USDK", "BUSD"
    )


    //not in binance
    var notBinance = arrayListOf(

//        "ZB", "HT", "OKB", "SNT", "SEELE",
//        "MOF", "XMX", "GRIN", "XEM", "HYC",
//        "YOU", "INB", "BSV", "LOOM",
//        "AE", "BTM", "VALOR", "WICC", "TRUE",
//        "BNT", "ELF", "BCV", "EKT", "OCEAN",
//        "ABBC", "LAMB", "BTG", "CRO", "KCS",
//        "NEXO", "REP", "BCD", "XZC", "MONA"
        ""
    )

//    var foreignList = arrayListOf(
//        "BSV", "BCD", "ZEC", "BTS", "XEM",
//        "OMG", "IGNIS", "EMC2", "COSM",
//        "PLC", "BEAM", "INB", "NET", "MEDX",
//        "MXM", "EDR", "XTZ", "MHC", "LA",
//        "KCS", "VTC", "DGB", "ANKR", "DEX",
//        "MONA", "PHX", "BZ", "GRIN", "BIX",
//        "SOLVE", "ORBS", "AERGO", "CLAM",
//        "AOA"
//
//    )

    //    var badCoinList = arrayListOf("")
    //bad code / low avg. volume
    var badCoinList = arrayListOf(
//        "ABBC", "DMT", "BTG", "RLC",
//        "GRS", "XZC", "CVC", "META", "ETP",
//        "AGI", "SPND", "PPT", "CRO", "NEXO",
//        "CNX", "VIA", "DENT", "XVG", "KMD",
//        "MDA", "NAS", "CMT", "ARN", "WTC",
//        "FUEL", "WAN", "WABI", "MTL",
//        "SMART", "DTA", "RFR", "MOC", "IQ",
//        "STORM", "NULS", "ETN", "MITH", "OCN",
//        "GTC", "POWR", "GTO", "EVX", "REN",
//        "QLC", "XAS", "ADX", "MTH",
//        "OAX", "SNGLS", "VIB", "ETHOS", "DLT",
//        "TNB", "AMB", "TTC", "LAMB", "TRIO",
//        "SWFTC", "HPB", "ITC", "LBA", "RNT",
//        "BHP", "KCASH", "NEW", "RCN", "FCT",
//        "XPX", "CPT", "ELA", "NPXS", "CTXC",
//        "SKY", "WAX", "REQ", "POE", "LEND",
//        "TNT", "EDO", "POA", "STRAT"
        ""
    )


    var handler: Handler = Handler()


    var allCoinList: ArrayList<CoinInfo> = ArrayList<CoinInfo>()

    var coinMarketList: List<Data> = ArrayList<Data>()

    lateinit var currentCoinList: List<CoinInfo>
    lateinit var latestCoinList: List<CoinInfo>

//    lateinit var diffs: List<CoinInfo>

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
//        throw NullPointerException()


        setLever()

        setContentView(R.layout.activity_main)

        volumeEnoughNum = 0

        //Log.e("latestCoinListGet", latestCoinListJsonStr)

        if (latestCoinListJsonStr.isNotEmpty()) {
//            allCoinList =
//                Gson().fromJson(latestCoinListJsonStr, object : TypeToken<ArrayList<CoinInfo>>() {}
//                    .type) as ArrayList<CoinInfo>

            coinMarketList =
                Gson().fromJson(latestCoinListJsonStr, object : TypeToken<List<Data>>() {}
                    .type) as List<Data>
            showAllCap()
        } else {
            getFromAPi()
        }
//        getFromAPi()


    }

    private fun setLever() {


    }


    private fun getFromAPi() {
        runOnUiThread {

            tvTitle.text = "loading getFromAPi"
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pro-api.coinmarketcap.com/")
            .client(CoinMarketAPI.genericClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val service = retrofit.create(CoinMarketAPI.ListService::class.java)

        val call: Call<CoinMarketList?>? = service.queryList()


        call!!.enqueue(object : Callback<CoinMarketList?> {

            override fun onResponse(
                call: Call<CoinMarketList?>,
                response: Response<CoinMarketList?>
            ) {
                var datas = response.body()?.data!!

                coinMarketList = datas

                var coinVolumeJsonStr = Gson().toJson(datas)
//                var jsonList = Gson().toJson(allCoinList)
                latestCoinListJsonStr = coinVolumeJsonStr



                showAllCap()

            }

            override fun onFailure(call: Call<CoinMarketList?>, t: Throwable) {
                Log.d("sss", Log.getStackTraceString(t));
                throw t
            }
        })
    }


//    private fun getData() {
//
//        tvTitle.text = "Loading..."
//
//        initData()
//
//        object : Thread() {
//            override fun run() {
//                super.run()
//                try {
//
//
//                    var urlStr = "https://coinmarketcap.com/all/views/all/"
//
//                    val doc = Jsoup.connect(urlStr).get()
//                    contextStr.append(doc.toString())
//                    //Log.e("doc.toString()", doc.toString())
////                    var icons = doc.select("#currencies-all > tbody > tr")
//                    var icons = doc.select("tr")
//                    Log.e("iconssize", icons.size.toString())
//
//                    for (i in 0 until icons.size) {
//
//                        if (icons[i].select("td").size < 10) {
//                            Log.d("invaild item", icons[i].toString())
//                            //                            titleStr.append("\n invaild item: ${icons[i]} \n")
//                            continue
//                        }
//
//
//                        var rank = icons[i].select("td")[0]
//                        //                        Log.e("icon rank", rank.text())
//                        var name =
//                            icons[i].select("td")[2].text() + " " + icons[i].select("td")[1].text()
//                        //                        Log.e("icon name", name.text())
//
//                        var cap = icons[i].select("td")[3]
//                        //                        Log.e("icon cap", cap.text())
//                        var capStr = cap.text().replace("$", "").replace(",", "")
//
//
//                        var price = icons[i].select("td")[4]
//                        var priceStr = price.text()
//
//
//                        var volume = icons[i].select("td")[6].select("a")
//                        //                        Log.e("icon volume", volume.text())
//                        var volumeStr = volume.text().replace("$", "").replace(",", "")
//
//
//                        var oneDayPercent = icons[i].select("td")[8]
//                        //                        Log.e("icon oneDayPercent", oneDayPercent.text())
//
//                        var sevenDaysPercent = icons[i].select("td")[9]
//                        //                        Log.e("icon sevenDaysPercent", sevenDaysPercent.text())
//
//
//                        var iconInfo = CoinInfo()
//                        iconInfo.name = name
//                        iconInfo.rank = rank.text().parseLong()
//                        iconInfo.volume = volumeStr.parseLong()
//                        iconInfo.cap = capStr.parseLong()
//                        iconInfo.oneDayPercent =
//                            oneDayPercent.text().replace("%", "").replace("?", "").parseDouble()
//                        iconInfo.sevenDaysPercent =
//                            sevenDaysPercent.text()
//                                .replace("%", "")
//                                .replace("?", "")
//                                .replace(">", "")
//                                .parseDouble()
//
//                        iconInfo.price = priceStr
//                        allCoinList.add(iconInfo)
//
//
//                    }
//
//                    showAllCap()
//
//
//                } catch (e: IOException) {
//                    //                    e.printStackTrace()
//                }
//
//            }
//        }.start()
//    }

    private fun initData() {

        volumeMax = maximum
        volumMin = minimum

        coinMarketList = ArrayList<Data>()
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

//    private fun displayDiffs(
//        currentCoinList: List<CoinInfo>
//    ) {
//
//
//        contextStr.append("-----------Diffs----------- \n")
//
//        for (i in 0 until diffs.size) {
//            var item = diffs[i]
//
//
//            if (currentCoinList.contains(item)) {
//                var index = currentCoinList.indexOf(item)
//                var diffNameStr = "+ ${index + 1}.${item.name} No.${item.rank} \n"
//                var diffNameSpan = SpannableStringBuilder(diffNameStr)
//
//                if (filterALevel(item.name)) {
//                    diffNameSpan.setSpan(
//                        ForegroundColorSpan(getColor(android.R.color.holo_blue_light)),
//                        0,
//                        diffNameStr.length - 1,
//                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                    )
//                } else {
//                    diffNameSpan.setSpan(
//                        ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
//                        0,
//                        diffNameStr.length - 1,
//                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                    )
//                }
//
//                contextStr.append(diffNameSpan)
//            } else {
//                var diffNameStr = "- ${item.name}\n"
//                var diffNameSpan = SpannableStringBuilder(diffNameStr)
//                diffNameSpan.setSpan(
//                    ForegroundColorSpan(getColor(android.R.color.holo_green_dark)),
//                    0,
//                    diffNameStr.length - 1,
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                )
//                contextStr.append(diffNameSpan)
//            }
//
//
//        }
//    }

//    private fun getdiffs(
//        currentCoinList: List<CoinInfo>,
//        latestCoinList: List<CoinInfo>
//    ) {
//        diffs = latestCoinList + currentCoinList
//        diffs = diffs.groupBy { it.name }
//            .filter { it.value.size == 1 }
//            .flatMap { it.value }
//    }

    private fun displayCoinList(
        coinInfos: List<CoinInfo>
    ) {
        for (i in 0 until coinInfos.size) {
            var item = coinInfos[i]

            setLevel(item)

            var indexStr = "${i}: "
//            if (diffs.contains(item)) {
//                var indexSpan = SpannableStringBuilder(indexStr)
//                indexSpan.setSpan(
//                    ForegroundColorSpan(getColor(android.R.color.holo_red_light)),
//                    0,
//                    indexStr.length - 1,
//                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
//                )
//                contextStr.append(indexSpan)
//            } else {
//                contextStr.append(indexStr)
//            }
            contextStr.append(indexStr)

            if (filterBLevel(item.name)) {

                var nameStr = item.name + " "
                var nameSpan = SpannableStringBuilder(nameStr)
                nameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_blue_dark)),
                    0,
                    nameStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                contextStr.append(nameSpan)

            } else if (filterALevel(item.name)) {
                var nameStr = item.name + " "
                var nameSpan = SpannableStringBuilder(nameStr)
                nameSpan.setSpan(
                    ForegroundColorSpan(getColor(android.R.color.holo_orange_dark)),
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


            contextStr.append(
                BigDecimal(item.price).setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " \n   "
            )


            // contextStr.append(StringUtils.getFormattedVolume(item.cap.toString()) + " ")
            var volumeStr = StringUtils.getFormattedVolume(item.volume.toString()) + " "
            if (BigDecimal(item.volume) > BigDecimal(volumMin)) {
                var volumeSpan = SpannableStringBuilder(volumeStr)
                volumeSpan.setSpan(
                    ForegroundColorSpan(getColor(R.color.brown)),
                    0,
                    volumeStr.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                contextStr.append(volumeSpan)
            } else {
                contextStr.append(volumeStr)
            }


            var oneDayPercentStr = BigDecimal(item.oneDayPercent)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString() + " "
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


            var sevenDaysPercentStr = BigDecimal(item.sevenDaysPercent)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "\n"
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

    private fun setLevel(item: CoinInfo) {
        if (item.volume.toBigDecimal() >= BigDecimal(10 * 10000 * 10000)) {
            SLevel.add(item.name)
        } else if (item.volume.toBigDecimal() >= BigDecimal(1 * 10000 * 10000)) {
            ALevel.add(item.name)
        } else if (item.volume.toBigDecimal() >= BigDecimal(2 * 1000 * 10000)) {
            if (BLevel.size < 20) {
                BLevel.add(item.name)
            }
        }
    }

    private fun filterSpecial(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return SLevel.any { it == symbol || it == name }
    }

    private fun filterALevel(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return ALevel.any { it == symbol || it == name }
    }

    private fun filterBLevel(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return BLevel.any { it == symbol || it == name }
    }

    private fun filterLeverList(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return leverList.any { it == symbol || it == name }
    }

    private fun filterStable(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return stableList.any { it == symbol || it == name }
    }


    private fun filterBlack(name: String): Boolean {
        var symbol = name.split(" ")[0]
        return notBinance.any { it == symbol || it.startsWith(name) }
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
        when (item.itemId) {
            R.id.count -> {

                var intent = Intent(this, CountActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Kline -> {

                var intent = Intent(this, KeyLineActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.volumeHistory -> {

                var intent = Intent(this, VolumeRankActivity_binance::class.java)
                startActivity(intent)
                return true
            }
            R.id.coinmarketapi -> {

                var intent = Intent(this, CoinMarketAPIActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.refresh -> {
                allCoinList.clear()
                getFromAPi()
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

            R.id.leverList -> {
                showLeverList()
                return true
            }
            R.id.candidate -> {
                showCandidate()
                return true
            }
            //SORT BY
            R.id.name -> {
                sortByType = "name"
                sortListRefreshUI()
                return true
            }
            R.id.volume -> {
                sortByType = "volume"
                sortListRefreshUI()
                return true
            }
            R.id.oneDay -> {
                sortByType = "oneDay"
                sortListRefreshUI()
                return true
            }
            R.id.sevenDay -> {
                sortByType = "sevenDay"
                sortListRefreshUI()
                return true
            }
            R.id.rank -> {
                sortByType = "rank"
                sortListRefreshUI()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showCandidate() {
        contextStr = SpannableStringBuilder()

        var list =
            allCoinList.filter { filterALevel(it.name) || filterBLevel(it.name) || filterSpecial(it.name) }

        list = sortList(list)

        displayCoinList(list)

        refreshUI()

    }


    private fun sortList(list: List<CoinInfo>): List<CoinInfo> {
        var list1 = list
        if (sortByType == "name") {
            list1 = list1.sortedBy { it.name }
        } else if (sortByType == "volume") {
            list1 = list1.sortedByDescending { BigDecimal(it.volume) }
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

        var smallVolumeCoinList =
            allCoinList.filter {
                BigDecimal(it.volume) > BigDecimal(volumMin + 1) && BigDecimal(it.volume) < BigDecimal(
                    volumeMax - 1
                )
            }

        smallVolumeCoinList =
            smallVolumeCoinList.filterNot {
                filterStable(it.name) || filterBlack(it.name) || filterTop(
                    it.rank
                )
            }
                .sortedBy { it.sevenDaysPercent }

        displayCoinList(smallVolumeCoinList)

        refreshUI()

    }

    private fun showAllCap() {

        for (i in 0 until coinMarketList.size) {
            val item = coinMarketList.get(i)


            var rank = i
            var name = item.symbol

            var capStr = item.quote.USD.market_cap

            var price = item.quote.USD.price.toString()

            var volume = item.quote.USD.volume_24h

            var oneDayPercent = item.quote.USD.percent_change_24h

            var sevenDaysPercent = item.quote.USD.percent_change_7d


            var iconInfo = CoinInfo()
            iconInfo.name = name
            iconInfo.rank = rank.toLong() + 1
            iconInfo.volume = volume
            iconInfo.cap = capStr
            iconInfo.oneDayPercent = oneDayPercent
            iconInfo.sevenDaysPercent = sevenDaysPercent

            iconInfo.price = price
            allCoinList.add(iconInfo)

        }



        volumeMax = maximum
        volumMin = minimum

        titleStr = StringBuffer()

        titleStr.append("All: ${allCoinList.size} ")

        var item = allCoinList[0]
        Log.e("sss", item.name.toString())

        currentCoinList = allCoinList.filter {
            BigDecimal(it.volume) > BigDecimal(volumMin + 1) && BigDecimal(it.volume) < BigDecimal(
                volumeMax - 1
            )
        }
        titleStr.append("VolEnough: ${currentCoinList.size} ")

        currentCoinList =
            currentCoinList.filterNot {
                filterStable(it.name) || filterBlack(it.name) || filterTop(
                    it.rank
                )
            }
                .sortedBy { it.sevenDaysPercent }
        titleStr.append("Filter: ${currentCoinList.size}")

//        getdiffs(currentCoinList, latestCoinList)
        sortListRefreshUI()

//        displayDiffs(currentCoinList)
    }

    private fun showLeverList() {

        currentCoinList = allCoinList.filter { filterLeverList(it.name) }

        sortListRefreshUI()

    }

    private fun sortListRefreshUI() {
        contextStr = SpannableStringBuilder()
        var list = sortList(currentCoinList)

        displayCoinList(list)

        refreshUI()
    }

    private fun showSmallCap() {

        var smallCoinList =
            currentCoinList.filter { BigDecimal(it.cap) < BigDecimal(capDivider) }
        var smallLatestCoinList =
            latestCoinList.filter { BigDecimal(it.cap) < BigDecimal(capDivider) }
        contextStr = SpannableStringBuilder()

//        getdiffs(smallCoinList, smallLatestCoinList)
        displayCoinList(smallCoinList)
//        displayDiffs(smallCoinList)

        refreshUI()

    }

    private fun showBigCap() {

        var bigCoinList =
            currentCoinList.filter { BigDecimal(it.cap) >= BigDecimal(capDivider) }
        var bigLatestCoinList =
            latestCoinList.filter { BigDecimal(it.cap) >= BigDecimal(capDivider) }
        contextStr = SpannableStringBuilder()

//        getdiffs(bigCoinList, bigLatestCoinList)
        displayCoinList(bigCoinList)
//        displayDiffs(bigCoinList)

        refreshUI()

    }
}
