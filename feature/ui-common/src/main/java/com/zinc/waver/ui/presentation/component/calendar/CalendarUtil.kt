package com.zinc.waver.ui.presentation.component.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek
import java.time.LocalDate

internal const val DAY_IN_MILLI = 23 * 60 * 60 * 1000

@RequiresApi(Build.VERSION_CODES.O)
internal fun resolveLocalDate(year: Int, month: Int, day: Int = 1): LocalDate {
    return LocalDate.of(year, month, day)
}

internal fun calculateLengthOfWeek(
    resolvedStartDate: LocalDate,
    resolvedEndDate: LocalDate,
): Int {
    return 7
    // todo (fvalela - #5): allow library caller to configure how many days in a week
    //    val daysBetweenStartAndEndDate = resolvedStartDate.until(resolvedEndDate, ChronoUnit.DAYS)
    //    return if (daysBetweenStartAndEndDate < 7)
    //        daysBetweenStartAndEndDate.toInt()
    //    else
    //        7
}

// todo (fvalela - #3): allow library caller to configure what
//  day of the week is the start (i.e. Sunday, Monday, Saturday, etc...)
@RequiresApi(Build.VERSION_CODES.O)
internal fun differenceBetweenTimeLibEndDayOfWeekAndPassedEndDayOfWeek(endDate: LocalDate): Int {
    val endDateDayOfWeek = endDate.dayOfWeek.value
    return DayOfWeek.SATURDAY.value -
            if (endDateDayOfWeek == DayOfWeek.SUNDAY.value)
                0
            else
                endDateDayOfWeek
}

// todo (fvalela - #3): allow library caller to configure what
//  day of the week is the start (i.e. Sunday, Monday, Saturday, etc...)
@RequiresApi(Build.VERSION_CODES.O)
internal fun differenceBetweenTimeLibStartDayOfWeekAndPassedStartDayOfWeek(startDate: LocalDate): Int {
    val startDateDayOfWeek = startDate.dayOfWeek.value
    return if (startDateDayOfWeek == DayOfWeek.SUNDAY.value)
        0
    else
        startDateDayOfWeek
}