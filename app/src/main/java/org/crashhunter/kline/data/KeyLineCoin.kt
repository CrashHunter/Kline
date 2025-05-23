package org.crashhunter.kline.data

import com.binance.client.model.enums.CandlestickInterval
import java.math.BigDecimal
class KeyLineCoin {

    var name = ""
    var close = BigDecimal(0)
    var rateInc = BigDecimal(0)
    var rangeInc = BigDecimal(0)
    var volumeRatio = BigDecimal(0)//量比
    var quoteAssetVolume = BigDecimal(0)//成交额
    var takerBuyBaseAssetVolume = BigDecimal(0)
    var takerBuyQuoteAssetVolume = BigDecimal(0)
    var openTime = 0L
    var closeTime = 0L
    var candlestickInterval = CandlestickInterval.DAILY
}