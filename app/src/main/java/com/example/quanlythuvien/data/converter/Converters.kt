
//Converters dùng để giúp Room hiểu và lưu enum ReaderType vào database

package com.example.quanlythuvien.data.converter

import androidx.room.TypeConverter
import com.example.quanlythuvien.data.entity.enums.ReaderType

class Converters {

    @TypeConverter
    fun fromReaderType(type: ReaderType): String {
        return type.value
    }

    @TypeConverter
    fun toReaderType(value: String): ReaderType {
        return ReaderType.fromValue(value)
    }
}