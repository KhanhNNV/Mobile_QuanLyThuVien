package com.example.quanlythuvien.core.network

import org.json.JSONObject
import retrofit2.Response

object ApiErrorParser {
    fun parseErrorMessage(response: Response<*>, fallback: String): String {
        return parseRawError(response.errorBody()?.string(), fallback)
    }

    fun parseRawError(rawError: String?, fallback: String): String {
        if (rawError.isNullOrBlank()) return fallback

        val trimmed = rawError.trim()
        runCatching {
            val json = JSONObject(trimmed)
            val details = json.opt("details")
            when (details) {
                is String -> if (details.isNotBlank()) return details
                is JSONObject -> {
                    details.optString("message").takeIf { it.isNotBlank() }?.let { return it }
                    val nested = details.optJSONArray("violations")
                    if (nested != null && nested.length() > 0) {
                        val messages = mutableListOf<String>()
                        for (i in 0 until nested.length()) {
                            val item = nested.optJSONObject(i) ?: continue
                            val reason = item.optString("reason").takeIf { it.isNotBlank() } ?: continue
                            val field = item.optString("field").takeIf { it.isNotBlank() }
                            messages += if (field != null) "$field: $reason" else reason
                        }
                        if (messages.isNotEmpty()) return messages.joinToString("\n")
                    }
                }
            }

            json.optString("message").takeIf { it.isNotBlank() }?.let { return it }
            json.optString("error").takeIf { it.isNotBlank() }?.let { return it }
        }

        return when {
            trimmed.contains("400") || trimmed.contains("Bad Request", ignoreCase = true) -> "Dữ liệu gửi lên không hợp lệ."
            trimmed.contains("401") || trimmed.contains("Unauthorized", ignoreCase = true) -> "Phiên đăng nhập đã hết hạn."
            trimmed.contains("403") || trimmed.contains("Forbidden", ignoreCase = true) -> "Bạn không có quyền thực hiện thao tác này."
            trimmed.contains("404") || trimmed.contains("Not Found", ignoreCase = true) -> "Không tìm thấy dữ liệu yêu cầu."
            trimmed.contains("500") || trimmed.contains("Internal Server Error", ignoreCase = true) -> "Máy chủ đang gặp lỗi, vui lòng thử lại sau."
            else -> trimmed.ifBlank { fallback }
        }
    }
}