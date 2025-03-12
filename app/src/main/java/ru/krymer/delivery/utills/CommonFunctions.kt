package ru.krymer.delivery.utills

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
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

fun isValidCords(cords: String): Boolean {
    val cordRegex =
        "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)\$".toRegex()
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