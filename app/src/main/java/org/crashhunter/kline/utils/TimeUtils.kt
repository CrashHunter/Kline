package org.crashhunter.kline.utils

import java.text.SimpleDateFormat
import java.util.*


object TimeUtils {

    fun dateFormat(time: Long): String {
        val date = Date(time)
        var format = SimpleDateFormat("MM.dd HH:mm")
        var timeStr = format.format(date)
        return timeStr
    }

    fun stringToLong(strTime: String, formatType: String): Long {
        val date: Date = stringToDate(strTime, formatType) // String类型转成date类型
        return date?.let { dateToLong(it) } ?: 0
    }

    fun stringToDate(strTime: String, formatType: String): Date {
        val formatter = SimpleDateFormat(formatType)
        var date: Date? = null
        date = formatter.parse(strTime)
        return date
    }

    fun dateToLong(date: Date): Long {
        return date.time
    }
}
