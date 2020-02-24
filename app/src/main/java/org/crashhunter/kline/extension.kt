package org.crashhunter.kline

import org.crashhunter.kline.utils.StringUtils

/**
 * Created by CrashHunter on 2020/2/24.
 */


fun String.getMoneyFormat(): String {
    var str = StringUtils.getFormattedVolume(this)
    return str

}
