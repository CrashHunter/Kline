import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@SuppressLint("ParcelCreator")
@Parcelize
data class CoinVolume(
    var coinName: String = "",

    @SerializedName("ConversionType")
    var conversionType: String = "", // direct
    @SerializedName("Data")
    var `data`: List<Data> = listOf(),
    @SerializedName("FirstValueInArray")
    var firstValueInArray: Boolean = false, // true
    @SerializedName("HasWarning")
    var hasWarning: Boolean = false, // false
    @SerializedName("Message")
    var message: String = "", // Got the data
    @SerializedName("RateLimit")
    var rateLimit: RateLimit = RateLimit(),
    @SerializedName("TimeFrom")
    var timeFrom: Int = 0, // 1581638400
    @SerializedName("TimeTo")
    var timeTo: Int = 0, // 1582502400
    @SerializedName("Type")
    var type: Int = 0 // 100
) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
data class Data(
    var divide: BigDecimal = BigDecimal.ZERO,
    @SerializedName("cccagg_volume_base")
    var cccaggVolumeBase: String = "", // 952775.38
    @SerializedName("cccagg_volume_quote")
    var cccaggVolumeQuote: String = "", // 333444696.9
    @SerializedName("cccagg_volume_total")
    var cccaggVolumeTotal: String = "", // 334397472.27
    var time: Int = 0, // 1582502400
    @SerializedName("top_tier_volume_base")
    var topTierVolumeBase: String = "", // 515825.24
    @SerializedName("top_tier_volume_quote")
    var topTierVolumeQuote: String = "", // 163947943.65
    @SerializedName("top_tier_volume_total")
    var topTierVolumeTotal: String = "", // 164463768.9
    @SerializedName("total_volume_base")
    var totalVolumeBase: String = "", // 2203708.87
    @SerializedName("total_volume_quote")
    var totalVolumeQuote: String = "", // 1463411008.11
    @SerializedName("total_volume_total")
    var totalVolumeTotal: String = "" // 1465614716.98
) : Parcelable

@SuppressLint("ParcelCreator")
@Parcelize
class RateLimit(
) : Parcelable