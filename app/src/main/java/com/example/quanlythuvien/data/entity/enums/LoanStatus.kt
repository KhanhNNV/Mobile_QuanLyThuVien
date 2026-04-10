package com.example.quanlythuvien.data.entity.enums

enum class LoanStatus(val value: String) {
    BORROWING("BORROWING"),
    OVERDUE("OVERDUE"),
    RETURNED("RETURNED");

    companion object {
        fun fromValue(value: String): LoanStatus {
            return entries.find { it.value == value } ?: BORROWING
        }
    }
}
