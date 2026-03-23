package com.example.quanlythuvien.data.entity.enums

enum class LoanDetailStatus(val value: String) {
    BORROWING("BORROWING"),
    RETURNED("RETURNED"),
    LOST("LOST");

    companion object {
        fun fromValue(value: String): LoanDetailStatus {
            return entries.find { it.value == value } ?: BORROWING
        }
    }
}
