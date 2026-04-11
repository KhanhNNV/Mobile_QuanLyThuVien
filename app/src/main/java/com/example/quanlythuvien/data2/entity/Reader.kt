package com.example.quanlythuvien.data2.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quanlythuvien.data2.entity.enums.ReaderType

@Entity(tableName="readers")

data class Reader (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reader_id")
    val readerId: Int = 0,

    val name:String,

    @ColumnInfo(name = "reader_type")
    val readerType: ReaderType,

    @ColumnInfo(name="phone_number")
    val phoneNumber:String,

    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis(), /*tự động lấy thời gian hiện tại khi tạo object
    mà không cần truyền tay
    */

    @ColumnInfo(name = "expiration_date")
    val expirationDate: Long? // có thể null vì người dùng có thể chưa mượn
    )


