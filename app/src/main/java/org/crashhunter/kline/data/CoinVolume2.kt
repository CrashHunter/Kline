import android.annotation.SuppressLint
import android.os.Parcelable
import com.binance.client.model.market.Candlestick
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@SuppressLint("ParcelCreator")
data class CoinVolume2(
    var coinName: String = "",
    var `data`: List<Candlestick> = listOf<Candlestick>()
)
