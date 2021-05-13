package org.crashhunter.kline.data

import android.icu.math.BigDecimal


/**
 * Created by CrashHunter on 2019/3/13.
 */
class CoinInfo {

    var name = ""
    var rank: Long = 0
    var cap: BigDecimal = BigDecimal.ONE
    var volume: BigDecimal = BigDecimal.ONE
    var oneDayPercent: Double = 0.0
    var sevenDaysPercent: Double = 0.0
    var price : BigDecimal = BigDecimal.ONE
}