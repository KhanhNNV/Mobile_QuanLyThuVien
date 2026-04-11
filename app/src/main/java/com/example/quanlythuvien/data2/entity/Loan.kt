package com.example.quanlythuvien.data2.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.quanlythuvien.data2.entity.enums.LoanStatus

/**
 * Phiếu mượn sách
 * status: BORROWING | RETURNED
 */
@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = Reader::class,
            parentColumns = ["reader_id"],
            childColumns = ["reader_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "loan_id")
    val loanId: Long = 0,

    @ColumnInfo(name = "reader_id", index = true)
    val readerId: String,             // FK -> Reader

    @ColumnInfo(name = "borrow_date")
    val borrowDate: Long,             // Timestamp (ms)

    @ColumnInfo(name = "due_date")
    val dueDate: Long,                // Timestamp (ms)

    val status: LoanStatus = LoanStatus.BORROWING
)
