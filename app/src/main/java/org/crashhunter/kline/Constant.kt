package org.crashhunter.kline

import com.binance.client.model.custom.DownPerItem
import com.binance.client.model.trade.BalancesItem

/**
 * Created by CrashHunter on 2021/1/10.
 */
object Constant {

    var coinList = ArrayList<String>()
    var downPerItemList = ArrayList<DownPerItem>()

    var ownCoinListName = ArrayList<String>()
    var ownCoinList = ArrayList<BalancesItem>()
}