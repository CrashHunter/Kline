package org.crashhunter.kline.data

import com.binance.client.model.enums.CandlestickInterval
import java.math.BigDecimal
class KeyLineCoin {

    var name = ""
    var divide = BigDecimal(0)
    var closeTime = 0L
    var candlestickInterval = CandlestickInterval.DAILY
}