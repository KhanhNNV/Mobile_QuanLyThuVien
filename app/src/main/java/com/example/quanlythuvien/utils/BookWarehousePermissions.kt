package com.example.quanlythuvien.utils

import android.content.Context

// Xử lý phân quyền UI cho kho sách
object BookWarehousePermissions {

    // Lấy Role trực tiếp từ TokenManager
    private fun currentRole(context: Context): String {
        val role = TokenManager(context).getRole() ?: ""

        // Chuẩn hóa role
        if (role.contains("ADMIN", true)) return "ADMIN"
        if (role.contains("STAFF", true)) return "STAFF"
        return ""
    }

    // Chỉ ADMIN được quản lý toàn bộ
    fun canManageCatalog(context: Context): Boolean =
        currentRole(context) == "ADMIN"

    // ADMIN + STAFF được thêm/sửa sách
    fun canCreateOrUpdateCatalog(context: Context): Boolean {
        val role = currentRole(context)
        return role == "ADMIN" || role == "STAFF"
    }

    // Kiểm tra có phải STAFF không
    fun isStaffUser(context: Context): Boolean =
        currentRole(context) == "STAFF"

    // Chỉ ADMIN được xóa
    fun canDeleteBookCopiesInWarehouseUi(context: Context): Boolean =
        canManageCatalog(context)
}