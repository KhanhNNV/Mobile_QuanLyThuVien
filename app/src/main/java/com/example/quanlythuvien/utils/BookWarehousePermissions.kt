package com.example.quanlythuvien.utils

import android.content.Context

/**
 * Quyền UI module Kho sách (Book / BookCopy), theo User.role ADMIN / STAFF.
 * - STAFF: CRU — thêm đầu sách, sửa metadata, thêm bản sao; không xóa bản sao (không D).
 * - ADMIN: đầy đủ, gồm xóa BookCopy.
 */
object BookWarehousePermissions {

    private fun currentRole(context: Context): String {
        val role = TokenManager(context).getRole().orEmpty()

        // Chuẩn hóa role (backend có thể trả "ROLE_ADMIN", "ADMIN", ...)
        if (role.contains("ADMIN", ignoreCase = true)) return "ADMIN"
        if (role.contains("STAFF", ignoreCase = true)) return "STAFF"

        return ""
    }

    /** Admin — quyền đầy đủ trên kho, gồm xóa BookCopy. */
    fun canManageCatalog(context: Context): Boolean =
        currentRole(context) == "ADMIN"

    /** STAFF hoặc ADMIN — thêm/sửa đầu sách và thêm bản sao (CRU cho staff). */
    fun canCreateOrUpdateCatalog(context: Context): Boolean {
        val role = currentRole(context)
        return role == "ADMIN" || role == "STAFF"
    }

    fun isStaffUser(context: Context): Boolean =
        currentRole(context) == "STAFF"

    /** Chỉ admin xóa được từng BookCopy trên UI. */
    fun canDeleteBookCopiesInWarehouseUi(context: Context): Boolean =
        canManageCatalog(context)
}
