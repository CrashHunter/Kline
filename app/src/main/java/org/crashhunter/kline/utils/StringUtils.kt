package org.crashhunter.kline.utils

import org.crashhunter.kline.isNumeric
import java.text.NumberFormat
import java.util.*


object StringUtils {
     fun String.nosuffix(): String {
        return this.replace("USDT","").replace("BUSD","");
    }

    /**
     *
     * example:
     * 2233,333,444.22
     *
     */
    fun getFormattedVolume(volume: String): String {
        if (!volume.isNumeric()) {
            return "0"
        }

        val format = NumberFormat.getNumberInstance(Locale.CHINA)
        val result = format.format(volume.toBigDecimal())

        return result
    }

    fun getLengthStr(str: String, length: Int): String {
        return String.format("%-${length}s", str)
    }

    fun addZeroForNum(str: String, strLength: Int): String? {
        var str = str
        var strLen = str.length
        if (strLen < strLength) {
            while (strLen < strLength) {
                val sb = StringBuffer()
//                sb.append("0").append(str) // 左补0
                 sb.append(str).append(" ");//右补0
                str = sb.toString()
                strLen = str.length
            }
        }
        return str
    }

}
