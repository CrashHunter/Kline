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
import com.binance.client.RequestOptions
import com.binance.client.SyncRequestClient
import com.binance.client.examples.constants.PrivateConfig
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.enums.CandlestickInterval
import com.binance.client.model.market.Candlestick
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.tvTitle
import kotlinx.coroutines.*
import org.crashhunter.kline.data.*
import org.crashhunter.kline.oneline.*
import org.crashhunter.kline.test.CoinMarketAPI
import org.crashhunter.kline.utils.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import kotlin.system.measureTimeMillis


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
//        "DAI", "TUSD", "USDC", "BITCNY",
//        "PAX", "GUSD", "USDK", "BUSD"
        ""
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


    val options = RequestOptions()
    var syncRequestClient = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options
    )

    val options_contract = RequestOptions()
    var syncRequestClient_contract = SyncRequestClient.create(
        PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
        options_contract
    )


    private var latestCoinListJsonStr by BaseSharedPreference(
        AppController.instance.applicationContext,
        LATEST_COIN_LIST,
        ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        supportActionBar?.hide()
//        throw NullPointerException()


        setContentView(R.layout.activity_main)

        volumeEnoughNum = 0

        //Log.e("latestCoinListGet", latestCoinListJsonStr)

        if (latestCoinListJsonStr.isNotEmpty()) {

            coinMarketList =
                Gson().fromJson(latestCoinListJsonStr, object : TypeToken<List<Data>>() {}
                    .type) as List<Data>

            Constant.coinMarketList = ArrayList(coinMarketList)
            showAllCap()
        } else {
            getFromAPi()
        }


        getContractList()
        getOwnAccountCoins()

    }

    //获取持有币当前价格
    private fun getOwnCoinsKlineData() {
        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0

                    for (coin in Constant.ownCoinListName) {
                        var n = async {
                            getCoinKlineData(coin + "USDT", Constant.holdCoinItemList)
                        }
                    }
                    amount
                }
            }
        }
    }

    private fun getContractCoinsKlineData() {
        GlobalScope.launch {
            val time = measureTimeMillis {
                val sum = withContext(Dispatchers.IO) {
                    var amount = 0

                    for (coin in Constant.contractCoins) {
                        var n = async {
                            getCoinKlineData(coin + "USDT", Constant.downPerItemList)
                        }
                    }
                    amount
                }
            }
        }
    }

    private fun getCoinKlineData(
        coin: String,
        coinItemList: ArrayList<DownPerItem>
    ): List<Candlestick> {

        try {
            //没有YEAR的维度，最大到月
            var list = syncRequestClient.getSPOTCandlestick(
                coin,
                CandlestickInterval.MONTHLY,
                null,
                null,
                36
            )
            Log.d("sss", "showData:$coin")

            var max = BigDecimal.ZERO
            var min = BigDecimal(9999999999)
            for (index in list.indices) {
                if (index == 0) {
                    continue
                }
                if (list.get(index).high > max) {
                    max = list.get(index).high
                }

                if (list.get(index).low < min
                    && list.get(index).low > BigDecimal.ZERO
                    && list.get(index).low != BigDecimal(0.0001)
                ) {
                    min = list.get(index).low
                }
            }

            var current = list[list.size - 1].close
            var downPer = BigDecimal.ONE.subtract(current.divide(max, 4, BigDecimal.ROUND_HALF_UP))
                .setScale(4, BigDecimal.ROUND_HALF_UP)

            var upPer = current.divide(min, 4, BigDecimal.ROUND_HALF_UP)


            var downPerItem = DownPerItem()
            downPerItem.coin = coin
            downPerItem.current = current
            downPerItem.max = max
            downPerItem.min = min
            downPerItem.downPer = downPer
            downPerItem.upPer = upPer
            coinItemList.add(downPerItem)
            return list
        } catch (e: Exception) {
            Log.e("sss", "Error Coin $coin: : " + Log.getStackTraceString(e))
        }
        return ArrayList<Candlestick>(0)
    }


    private fun getOwnAccountCoins() {
        Log.d("sss", "getOwnAccountCoins")
        options.url = "https://api.binance.com"
        syncRequestClient = SyncRequestClient.create(
            PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options
        )
        object : Thread() {
            override fun run() {
                super.run()

                //获取币安持有现货列表
                var data = syncRequestClient.getAccountSPOT()
                Constant.ownCoinListName.clear()
                Constant.ownCoinList.clear()
                var filter =
                    data.balances.filter { it.free.toBigDecimal() > BigDecimal.ZERO || it.locked.toBigDecimal() > BigDecimal.ZERO }
                Log.d("sss", "own coin size: ${filter.size}")
                for (item in filter) {
//                    Log.d("sss", "own coin : ${item.asset}:${item.free}")
                    Constant.ownCoinListName.add(item.asset)
                    Constant.ownCoinList.add(item)
                }
                //获取持有币当前价格
                getOwnCoinsCurrentPrice()
//                getOwnCoinsKlineData()
            }
        }.start()
    }

    private fun getOwnCoinsCurrentPrice() {
        for (coin in Constant.coinMarketList) {
            if (Constant.ownCoinListName.contains(coin.symbol)) {

                var downPerItem = DownPerItem()
                //不含USDT后缀
                downPerItem.coin = coin.symbol
                downPerItem.current = BigDecimal(coin.quote.USD.price).setScale(2, BigDecimal.ROUND_HALF_UP)
                Constant.holdCoinItemList.add(downPerItem)
            }
        }


    }

    private fun getContractList() {
        Log.d("sss", "getContractList")
        options_contract.url = "https://fapi.binance.com"
        object : Thread() {
            override fun run() {
                super.run()


                var data = syncRequestClient_contract.getExchangeInformation()

                var list = data.symbols
                list = list.filterNot {
                    it.symbol.contains("_")
                }
                list = list.sortedBy { it.symbol }
                Log.d("sss", "contract coin size: ${list.size}")
                for (index in list.indices) {

                    var symbol = list.get(index)
//                    Log.d("sss", "contract coin:" + symbol.symbol)
                    //处理1000shib 特殊情况

                    //提取币名
                    var symbolName = symbol.symbol
                        .replace("1000", "")
                        .replace("USDT", "")
                        .replace("BUSD", "");
                    if (!Constant.contractCoins.contains(symbolName)) {
                        Constant.contractCoins.add(symbolName)
                    } else {

                    }
                }

//                getContractCoinsKlineData()

            }
        }.start()
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
                Constant.coinMarketList = ArrayList(datas)

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

                var intent = Intent(this, CalculateActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.Kline -> {

                var intent = Intent(this, KeyLineActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.ROI -> {

                var intent = Intent(this, ROIActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.DownPer -> {
                //合约跌幅
                var intent = Intent(this, DownPercentActivity::class.java)
                startActivity(intent)
                return true
            }


            R.id.coinmarketapi -> {
                //合约市值
                var intent = Intent(this, CoinMarketAPIActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.TD -> {
                //TD
                var intent = Intent(this, TDActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.refresh -> {
                allCoinList.clear()
                getFromAPi()
                getContractList()
                getOwnAccountCoins()
                return true
            }


            R.id.volumeHistory -> {

                var intent = Intent(this, VolumeRankActivity_binance::class.java)
                startActivity(intent)
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

        if (allCoinList.isEmpty()) {
            return
        }
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
        titleStr.append("After Filter: ${currentCoinList.size}")

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
