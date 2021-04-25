package org.crashhunter.kline.data

import android.icu.math.BigDecimal

/**
 * Created by CrashHunter on 2021/4/25.
 */
data class CoinMarketList(
    val `data`: List<Data> = listOf(),
    val status: Status = Status()
)

data class Data(
    val circulating_supply: Double = 0.0,
    val cmc_rank: Int = 0,
    val date_added: String = "",
    val id: Int = 0,
    val last_updated: String = "",
    val max_supply: Double = 0.0,
    val name: String = "",
    val num_market_pairs: Int = 0,
    val platform: Any = Any(),
    val quote: Quote = Quote(),
    val slug: String = "",
    val symbol: String = "",
    val tags: List<String> = listOf(),
    val total_supply: Double = 0.0
)

data class Status(
    val credit_count: Int = 0,
    val elapsed: Int = 0,
    val error_code: Int = 0,
    val error_message: String = "",
    val timestamp: String = ""
)

data class Quote(
    val BTC: BTC = BTC(),
    val USD: USD = USD()
)

data class BTC(
    val last_updated: String = "",
    val market_cap: Double = 0.0,
    val percent_change_1h: Double = 0.0,
    val percent_change_24h: Double = 0.0,
    val percent_change_7d: Double = 0.0,
    val price: Double = 0.0,
    val volume_24h: Double = 0.0
)

data class USD(
    val last_updated: String = "",
    val market_cap: String = "",
    val percent_change_1h: Double = 0.0,
    val percent_change_24h: Double = 0.0,
    val percent_change_7d: Double = 0.0,
    val price: Double = 0.0,
    val volume_24h: Double = 0.0
)