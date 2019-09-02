package org.crashhunter.kline

import java.util.regex.Pattern


fun String?.isNumeric(): Boolean {
    if (isNullOrEmpty()) {
        return false
    }

    val pattern = Pattern.compile("[0-9.]*")
    val isNum = pattern.matcher(this)
    return isNum.matches()

}


fun String?.parseDouble(): Double {
    if (isNullOrEmpty()) {
        return 0.00
    }

    return this!!.toDouble()

}


fun String?.parseLong(): Long {
    if (isNumeric()) {
        return this!!.toLong()
    } else {
        return 0L
    }


}