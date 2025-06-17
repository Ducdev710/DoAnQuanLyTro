package com.app.motel.common.service

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class StringListRoomConverter {

    // Chuyển từ List<String> thành chuỗi JSON để lưu trong CSDL
    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

    // Chuyển từ chuỗi JSON thành List<String> khi đọc từ CSDL
    @TypeConverter
    fun toList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }
}

class DateRoomConverters {

    // Chuyển từ timestamp (Long) thành đối tượng Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    // Chuyển từ đối tượng Date thành timestamp (Long) để lưu trong CSDL
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

