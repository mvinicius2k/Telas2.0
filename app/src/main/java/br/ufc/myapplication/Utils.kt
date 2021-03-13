package br.ufc.myapplication

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object{
        @SuppressLint("NewApi")
        fun strDateToLocalDateTime(string: String): LocalDateTime{
            val dateTime = LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:sss"));
            return dateTime
        }
    }
}