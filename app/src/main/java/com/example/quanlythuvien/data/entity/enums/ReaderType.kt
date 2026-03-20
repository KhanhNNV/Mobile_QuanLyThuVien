package com.example.quanlythuvien.data.entity.enums

enum class ReaderType(val value: String) {
    STUDENT("STUDENT"),
    GUEST("GUEST");

    companion object {
        fun fromValue(value: String): ReaderType {
            return entries.find { it.value == value } ?: STUDENT
        }
    }
}