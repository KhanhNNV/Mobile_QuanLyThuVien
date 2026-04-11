package com.example.quanlythuvien.data2.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Thông báo phí (phạt trễ hạn, mất sách, …)
 */
@Entity(
    tableName = "fee_notices",
    foreignKeys = [
        ForeignKey(
            entity = Reader::class,
            parentColumns = ["reader_id"],
            childColumns = ["reader_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["loan_id"],
            childColumns = ["loan_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FeeNotice(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "fee_id")
    val feeId: Long = 0,

    @ColumnInfo(name = "reader_id", index = true)
    val readerId: String,              // FK -> Reader

    @ColumnInfo(name = "loan_id", index = true)
    val loanId: Long? = null,          // FK -> Loan (nullable)

    @ColumnInfo(name = "book_id", index = true)
    val bookId: Long? = null,          // FK -> Book (nullable)

    val amount: Double,                // Số tiền phạt

    val reason: String,                // Lý do phát sinh phí

    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,       // Đã thanh toán chưa

    @ColumnInfo(name = "created_date")
    val createdDate: Long              // Timestamp (ms)
)
