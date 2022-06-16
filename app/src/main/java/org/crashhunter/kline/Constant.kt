package org.crashhunter.kline

import com.binance.client.model.custom.HoldPriceItem
import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.trade.BalancesItem
import org.crashhunter.kline.data.Data

/**
 * Created by CrashHunter on 2021/1/10.
 */
object Constant {
    //合约的币  KeyLine getContractList
    var contractCoins = ArrayList<String>()

    //持有币的最大值最小值，降幅，当前价
    var downPerItemList = ArrayList<DownPerItem>()


    //拥有的币
    var ownCoinList = ArrayList<BalancesItem>()

    //拥有的币 名字
    var ownCoinListName = ArrayList<String>()


    //拥有的币 名字
    var coinMarketList = ArrayList<Data>()


    //ROI LIST
    var holdPriceItemList: List<HoldPriceItem> = ArrayList<HoldPriceItem>()
}