package com.erpnext.pos.localSource.converters

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String?>?): String? {
        return ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String?>? {
        return listOf()
    }

    @TypeConverter
    fun fromIntList(value: List<Int?>?): String {
        return ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int?>? {
        return listOf()
    }
}