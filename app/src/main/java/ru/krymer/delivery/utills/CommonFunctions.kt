package ru.krymer.delivery.utills

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
    return emailRegex.matches(email)
}

fun isEmptyInput(str: String): Boolean {
    return str.isNotEmpty()
}

fun startsWithDigit(input: String): Boolean {
    if (input.isEmpty()) return false
    return input.first().isDigit()
}

fun isSameDay(date1: Long, date2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply { timeInMillis = date1 }
    val calendar2 = Calendar.getInstance().apply { timeInMillis = date2 }

    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) &&
            calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH)
}

fun getStartOfNextDay(): Long {
    return LocalDateTime.now()
        .plusDays(1)
        .truncatedTo(ChronoUnit.DAYS)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

fun isValidCords(cords: String): Boolean {
    val cordRegex =
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)\\s*,\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$".toRegex()
    return cordRegex.matches(cords)
}

fun isValidPhone(phone: String): Boolean {
    val phoneRegex = "^[+]?[0-9]{11,}$".toRegex()
    return phoneRegex.matches(phone)
}

fun convertToTextDate(timeStamp: Long, pattern: String = Constants.PatternDate.DEFAULT): String {
    val date = Date(timeStamp)
    val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return dateFormat.format(date)
}

fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", text)
    clipboardManager.setPrimaryClip(clipData)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> findChangedFields(original: T, updated: T): String {
    val changedFields = mutableMapOf<String, Pair<Any?, Any?>>()

    original::class.memberProperties.forEach { property ->
        val originalValue = (property as KProperty1<T, *>).get(original)
        val updatedValue = property.get(updated)

        if (originalValue != updatedValue) {
            changedFields[property.name] = Pair(originalValue, updatedValue)
        }
    }

    return if (changedFields.isEmpty()) {
        "Никакие поля не были изменены."
    } else {
        val result = StringBuilder("Измененные поля:\n")
        changedFields.forEach { (fieldName, values) ->
            result.append("$fieldName: ${values.first} -> ${values.second}\n")
        }
        result.toString()
    }
}

fun getCurrentDayRangeTimestamps(calendar: Calendar = Calendar.getInstance()): Pair<Long, Long> {

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfDay = calendar.timeInMillis

    return Pair(startOfDay, endOfDay)
}

fun colorChangerDay(day: String): List<Color> {
    return when (day) {
        "Пн" -> listOf(Color.Red, Color.Red)
        "Вт" -> listOf(Color.Yellow, Color.Yellow)
        "Ср" -> listOf(Color.Green, Color.Green)
        "Чт" -> listOf(Color.Cyan, Color.Cyan)
        "Пт" -> listOf(Color.Blue, Color.Blue)
        "Сб" -> listOf(Color.Magenta, Color.Magenta)
        "Вс" -> listOf(Color.Red, Color.Red)
        else -> {
            listOf(Color.Red, Color.Red)
        }
    }
}

fun colorChangerMonth(month: String): List<Color> {
    return when (month) {
        "Янв" -> listOf(Color.Red, Color.Red)
        "Фев" -> listOf(Color.Yellow, Color.Yellow)
        "Мар" -> listOf(Color.Green, Color.Green)
        "Апр" -> listOf(Color.Cyan, Color.Cyan)
        "Май" -> listOf(Color.Blue, Color.Blue)
        "Июн" -> listOf(Color.Magenta, Color.Magenta)
        "Июл" -> listOf(Color.Black, Color.Black)
        "Авг" -> listOf(Color.Gray, Color.Gray)
        "Сен" -> listOf(Color.DarkGray, Color.DarkGray)
        "Окт" -> listOf(Color.LightGray, Color.LightGray)
        "Ноя" -> listOf(Color.Black, Color.Red)
        "Дек" -> listOf(Color.Red, Color.Yellow)
        else -> {
            listOf(Color.Red, Color.Blue)
        }
    }
}

sealed class MyResult<out T> {
    data class Success<T>(val data: T): MyResult<T>()
    data class Error(val message: String?): MyResult<Nothing>()
}

