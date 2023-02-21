package org.crashhunter.kline

import com.binance.client.model.custom.HoldPriceItem
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.custom.MarketCapItem
import com.binance.client.model.trade.BalancesItem
import org.crashhunter.kline.data.Data

/**
 * Created by CrashHunter on 2021/1/10.
 */
object Constant {
    //合约的币  KeyLine getContractList
    var contractCoins = ArrayList<String>()

    //持有币的最大值最小值，降幅，当前价
    var holdCoinItemList = ArrayList<DownPerItem>()

    //合约的最大值最小值，降幅，当前价
    var downPerItemList = ArrayList<DownPerItem>()


    //拥有的币
    var ownCoinList = ArrayList<BalancesItem>()

    //拥有的币 名字
    var ownCoinListName = ArrayList<String>()


    //拥有的币 名字
    var coinMarketList = ArrayList<Data>()


    //持有币的 成本统计
    var holdPriceItemList: List<HoldPriceItem> = ArrayList<HoldPriceItem>()


    // 币种市值
    var marketcapItemList: List<MarketCapItem> = ArrayList<MarketCapItem>()


    var badCoinList = arrayListOf(
         "LINA", "ANC", "ALPHA"
    )

    var cleanCoinList = arrayListOf(
        "AKRO", "ALPHA", "BAKE","BCH","BTS",
        "DODO", "DOGE", "EOS","ETC","FLM",
        "LINA", "LTC", "NKN","RAY","REN",
        "SC", "SXP", "TLM","XEM"
    )


    //not in binance
    var notBinance = arrayListOf(
        "ANC","BTCST","AUCTION","SC","SRM",
        "CVC","FTT","BNX"

    )

    var ACoinList = arrayListOf(
        "ADA", "APT", "ATOM","AUDIO","AVAX",
        "AXS", "BAT", "BNB","BTC","CHZ",
        "CRV", "DOT", "ENS","ETH","FIL",
        "FLOW", "GALA", "GMT","GRT","ICP",
        "KAVA", "LINK", "MANA","NEAR",
        "OP", "SAND", "SNX","SOL","STORJ",
        "UNI", "YFI", "ZRX"
    )
}