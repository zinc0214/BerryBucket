package com.zinc.waver.ui.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

fun parseDateWithZero(month: Int): String {
    return if (month > 9) "$month" else "0${month}"
}

fun LocalDate.toEpochMilli(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun LocalDate.parseWithDday(): String {
    val year = this.year
    val month = parseNumberWithZero(this.month.value)
    val date = parseNumberWithZero(this.dayOfMonth)

    return "${year}.${month}.${date}(${this.getDday()})"
}


fun LocalDate.toStringData(): String {
    return format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun String.toLocalData(): LocalDate {
    return LocalDate.parse(this)
}

fun parseNumberWithZero(value: Int): String {
    return if (value > 9) "$value" else "0${value}"
}

private fun LocalDate.getDday(): String {

    // Millisecond 형태의 하루(24 시간)
    // Millisecond 형태의 하루(24 시간)
    val ONE_DAY = 24 * 60 * 60 * 1000

    // D-day 설정
    val ddayCalendar: Calendar = Calendar.getInstance()
    ddayCalendar.set(this.year, this.monthValue - 1, this.dayOfMonth)

    // D-day 를 구하기 위해 millisecond 으로 환산하여 d-day 에서 today 의 차를 구한다.
    val dday: Long = ddayCalendar.timeInMillis / ONE_DAY
    val today: Long = Calendar.getInstance().timeInMillis / ONE_DAY
    var result = dday - today

    // 출력 시 d-day 에 맞게 표시
    val strFormat: String
    if (result > 0) {
        strFormat = "D-%d"
    } else if (result == 0L) {
        strFormat = "D-Day"
    } else {
        result *= -1
        strFormat = "D+%d"
    }
    return String.format(strFormat, result)
}

fun textParseToLocalDate(ddayText: String?): LocalDate {
    if (ddayText.isNullOrEmpty()) return LocalDate.now()
    val split = ddayText.split(".")
    val year = split[0].toInt()
    val month = split[1].toInt()
    val date = split[2].split("(")[0].toInt()

    return LocalDate.of(year, month, date)
}