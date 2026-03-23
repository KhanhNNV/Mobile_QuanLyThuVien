package com.example.quanlythuvien.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus

/**
 * Chi tiết phiếu mượn (quan hệ nhiều-nhiều giữa Loan và Book)
 * Khóa chính ghép: (loan_id, book_id)
 * status: BORROWING | RETURNED | LOST
 */
@Entity(
    tableName = "loan_details",
    primaryKeys = ["loan_id", "book_id"],
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["loan_id"],
            childColumns = ["loan_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Book::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class LoanDetail(
    @ColumnInfo(name = "loan_id", index = true)
    val loanId: Long,                  // PK + FK -> Loan

    @ColumnInfo(name = "book_id", index = true)
    val bookId: Long,                  // PK + FK -> Book

    @ColumnInfo(name = "return_date")
    val returnDate: Long? = null,      // Timestamp (ms), nullable khi chưa trả

    val status: LoanDetailStatus = LoanDetailStatus.BORROWING
)
