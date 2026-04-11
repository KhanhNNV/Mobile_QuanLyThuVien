package com.example.quanlythuvien.utils

import android.content.Context

/**
 * Quyền UI module Kho sách (Book / BookCopy), theo User.role ADMIN / STAFF.
 * - STAFF: CRU — thêm đầu sách, sửa metadata, thêm bản sao; không xóa bản sao (không D).
 * - ADMIN: đầy đủ, gồm xóa BookCopy.
 */
object BookWarehousePermissions {

    private const val PREFS_NAME = "LibraryAppPrefs"
    private const val KEY_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ROLE = "userRole"

    private fun currentRole(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_LOGGED_IN, false)) return ""
        return prefs.getString(KEY_USER_ROLE, "").orEmpty()
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
