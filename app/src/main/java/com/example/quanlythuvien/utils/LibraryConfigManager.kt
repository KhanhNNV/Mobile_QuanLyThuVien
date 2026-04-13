package com.example.quanlythuvien.utils

import android.content.Context
import android.content.SharedPreferences

class LibraryConfigManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("LibraryConfigPrefs", Context.MODE_PRIVATE)

    fun saveHasStudentDiscount(value: Boolean) {
        prefs.edit().putBoolean("HAS_STUDENT_DISCOUNT", value).apply()
    }

    // Trả về Boolean? (null nếu chưa từng lưu)
    fun getHasStudentDiscount(): Boolean? {
        return if (prefs.contains("HAS_STUDENT_DISCOUNT")) {
            prefs.getBoolean("HAS_STUDENT_DISCOUNT", false)
        } else {
            null
        }
    }

    fun clearConfig() {
        prefs.edit().remove("HAS_STUDENT_DISCOUNT").apply()
    }
}