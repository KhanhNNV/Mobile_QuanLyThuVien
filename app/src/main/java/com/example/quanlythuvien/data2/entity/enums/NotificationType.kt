package com.example.quanlythuvien.data2.entity.enums

enum class NotificationType(val value: String) {
    OVERDUE("OVERDUE"),
    DUE_SOON("DUE_SOON"),
    FEE_UNPAID("FEE_UNPAID"),
    CARD_EXPIRY("CARD_EXPIRY"),
    LOST_BOOK("LOST_BOOK");

    companion object {
        fun fromValue(value: String): NotificationType {
            return entries.find { it.value == value } ?: OVERDUE
        }
    }
}
