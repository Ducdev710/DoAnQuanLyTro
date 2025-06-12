package com.app.motel.common.service

import android.os.Build
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateConverter {
    private const val PATTERN_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    private const val LOCAL_DATE_FORMAT = "dd/MM/yyyy"
    private const val LOCAL_DATE_FORMAT2 = "HH:mm - dd/MM/yyyy"

    fun getCurrentDateTime(): Date {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDateTime = LocalDateTime.now()
            Date.from(currentDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant())
        } else {
            val calendar = Calendar.getInstance()
            calendar.time
        }
    }

    //Trả về thời gian hiện tại dưới dạng chuỗi theo định dạng PATTERN_DATE_FORMAT
    fun getCurrentStringDateTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDateTime = LocalDateTime.now()
            currentDateTime.format(DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT))
        } else {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(PATTERN_DATE_FORMAT, Locale.getDefault())
            dateFormat.format(calendar.time)
        }
    }

    //Trả về thời gian hiện tại dưới dạng chuỗi theo định dạng LOCAL_DATE_FORMAT
    fun getCurrentLocalDateTime(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val currentDateTime = LocalDateTime.now()
            currentDateTime.format(DateTimeFormatter.ofPattern(LOCAL_DATE_FORMAT))
        } else {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(LOCAL_DATE_FORMAT, Locale.getDefault())
            dateFormat.format(calendar.time)
        }
    }

    //Tính số ngày chênh lệch giữa hai chuỗi ngày tháng
    fun getDaysDifference(date1: String?, date2: String?): Long {
        if(date1.isNullOrEmpty() || date2.isNullOrEmpty()) return 0;
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT)
            val firstDate = LocalDate.parse(date1 ?: getCurrentStringDateTime(), formatter)
            val secondDate = LocalDate.parse(date2 ?: getCurrentStringDateTime(), formatter)
            kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(firstDate, secondDate))
        } else {
            val dateFormat = SimpleDateFormat(PATTERN_DATE_FORMAT, Locale.getDefault())
            val firstDate = dateFormat.parse(date1 ?: getCurrentStringDateTime())!!
            val secondDate = dateFormat.parse(date2 ?: getCurrentStringDateTime())!!
            val diff = kotlin.math.abs(firstDate.time - secondDate.time)
            TimeUnit.MILLISECONDS.toDays(diff)
        }
    }

    //Kiểm tra liệu danh sách các ngày có gần nhau (trong vòng 60 phút)
    fun areDatesClose(dates: List<String>): Boolean {
        if (dates.size < 2) return true

        val dateTimes: List<Long> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern(PATTERN_DATE_FORMAT)
            dates.map { LocalDateTime.parse(it, formatter).atZone(java.time.ZoneId.systemDefault()).toEpochSecond() }
        } else {
            val formatter = SimpleDateFormat(PATTERN_DATE_FORMAT, Locale.getDefault())
            dates.map { formatter.parse(it)!!.time / 1000 }
        }.sorted()

        for (i in 0 until dateTimes.size - 1) {
            val durationMinutes = TimeUnit.SECONDS.toMinutes(dateTimes[i + 1] - dateTimes[i])
            if (durationMinutes > 60) return false
        }
        return true
    }

    //Chuyển chuỗi định dạng PATTERN_DATE_FORMAT thành đối tượng Date
    fun stringToDate(dateString: String?): Date?{
        val format = SimpleDateFormat(PATTERN_DATE_FORMAT, Locale.getDefault())
        return try{
            format.parse(dateString!!)
        }catch (e: Exception){
            null
        }
    }

    //Chuyển chuỗi định dạng LOCAL_DATE_FORMAT thành đối tượng Date
    fun localStringToDate(dateString: String?): Date?{
        val format = SimpleDateFormat(LOCAL_DATE_FORMAT, Locale.getDefault())
        return try{
            format.parse(dateString!!)
        }catch (e: Exception){
            null
        }
    }

    //Chuyển đối tượng Date thành chuỗi định dạng LOCAL_DATE_FORMAT
    fun dateToLocalString(date: Date): String {
        val format = SimpleDateFormat(LOCAL_DATE_FORMAT, Locale.getDefault())
        return format.format(date)
    }

    //Chuyển đối tượng Date thành chuỗi định dạng LOCAL_DATE_FORMAT2 (có giờ phút)
    fun dateToLocalString2(date: Date): String {
        val format = SimpleDateFormat(LOCAL_DATE_FORMAT2, Locale.getDefault())
        return format.format(date)
    }

    //Tính số tháng chênh lệch giữa hai đối tượng Date
    fun monthsBetweenDates(date1: Date, date2: Date): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            ChronoUnit.MONTHS.between(localDate1, localDate2).toInt()
        } else {
            val cal1 = Calendar.getInstance().apply { time = date1 }
            val cal2 = Calendar.getInstance().apply { time = date2 }

            val yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)
            val monthDiff = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH)

            yearDiff * 12 + monthDiff
        }
    }

    //Thêm một số tháng nhất định vào một đối tượng Date
    fun addMonthsToDate(date: Date, monthsToAdd: Int): Date {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val updatedDate = localDate.plusMonths(monthsToAdd.toLong())
            Date.from(updatedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } else {
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.add(Calendar.MONTH, monthsToAdd)
            calendar.time
        }
    }

    //Tính toán và trả về đối tượng Calendar sau khi thêm một số tháng vào ngày cụ thể
    fun calculateMonth(date: Date, monthCountToCalculate: Int): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, monthCountToCalculate)
        return calendar
    }

    fun Date.toCalendar(): Calendar{
        val date = this
        return Calendar.getInstance().apply {time = date}
    }
}
