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



