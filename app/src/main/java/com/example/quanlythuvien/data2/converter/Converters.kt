// Converters dùng để giúp Room hiểu và lưu enum vào database

package com.example.quanlythuvien.data2.converter

import androidx.room.TypeConverter
import com.example.quanlythuvien.data2.entity.enums.LoanDetailStatus
import com.example.quanlythuvien.data2.entity.enums.LoanStatus
import com.example.quanlythuvien.data2.entity.enums.NotificationType
import com.example.quanlythuvien.data2.entity.enums.ReaderType

class Converters {

    @TypeConverter
    fun fromReaderType(type: ReaderType): String = type.value

    @TypeConverter
    fun toReaderType(value: String): ReaderType = ReaderType.fromValue(value)

    @TypeConverter
    fun fromLoanStatus(status: LoanStatus): String = status.value

    @TypeConverter
    fun toLoanStatus(value: String): LoanStatus = LoanStatus.fromValue(value)

    @TypeConverter
    fun fromLoanDetailStatus(status: LoanDetailStatus): String = status.value

    @TypeConverter
    fun toLoanDetailStatus(value: String): LoanDetailStatus = LoanDetailStatus.fromValue(value)

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.value

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.fromValue(value)
}