package com.example.quanlythuvien.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    // Hàm dùng chung để lấy Payload của Token
    private fun getPayload(token: String): JSONObject? {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                // Giải mã phần thứ 2 (Payload) bằng Base64 URL_SAFE
                val payloadString = String(Base64.decode(parts[1], Base64.URL_SAFE))
                return JSONObject(payloadString)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // Lấy Role từ Token
    fun getRoleFromToken(token: String): String {
        val payload = getPayload(token)
        return payload?.optString("role", "STAFF") ?: "STAFF" // Mặc định là STAFF nếu lỗi
    }

    // Lấy LibraryId từ Token
    fun getLibraryIdFromToken(token: String): Long? {
        val payload = getPayload(token)
        return if (payload != null && payload.has("libraryId") && !payload.isNull("libraryId")) {
            payload.getLong("libraryId")
        } else {
            null
        }
    }
}