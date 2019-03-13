package org.crashhunter.kline.utils

import org.crashhunter.kline.isNumeric
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


object StringUtils {

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
        val result = format.format(volume.toLong())

        return result
    }

}
