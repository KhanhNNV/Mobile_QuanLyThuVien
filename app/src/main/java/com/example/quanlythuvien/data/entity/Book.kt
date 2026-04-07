package com.example.quanlythuvien.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["isbn_code"], unique = true)]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "book_id")
    val bookId: Long = 0,

    @ColumnInfo(name = "category_id", index = true)
    val categoryId: Int, // Khóa ngoại liên kết với Category

    @ColumnInfo(name = "isbn_code",)

    val isbnCode: String,

    val title: String,
    val author: String,

    @ColumnInfo(name = "total_quantity")
    val totalQuantity: Int,

    @ColumnInfo(name = "available_quantity")
    val availableQuantity: Int,

    @ColumnInfo(name = "lost_quantity")
    val lostQuantity: Int = 0,

    @ColumnInfo(name = "base_price")
    val basePrice: Double = 0.0
)