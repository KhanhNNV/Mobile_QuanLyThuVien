package com.example.quanlythuvien.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.quanlythuvien.data.entity.enums.NotificationType

/**
 * Thông báo dành cho thủ thư
 * type: OVERDUE | DUE_SOON | FEE_UNPAID | CARD_EXPIRY | LOST_BOOK
 */
@Entity(
    tableName = "notifications",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["loan_id"],
            childColumns = ["loan_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FeeNotice::class,
            parentColumns = ["fee_id"],
            childColumns = ["fee_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Notification(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    val notificationId: Long = 0,

    @ColumnInfo(name = "loan_id", index = true)
    val loanId: Long? = null,           // FK -> Loan (nullable)

    @ColumnInfo(name = "fee_id", index = true)
    val feeId: Long? = null,            // FK -> FeeNotice (nullable)

    /** OVERDUE | DUE_SOON | FEE_UNPAID | CARD_EXPIRY | LOST_BOOK */
    val type: NotificationType,

    val message: String,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "created_date")
    val createdDate: Long               // Timestamp (ms)
)
