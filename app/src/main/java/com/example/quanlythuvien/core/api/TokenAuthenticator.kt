package com.example.quanlythuvien.core.api

import android.content.Context
import android.content.Intent
import com.example.quanlythuvien.MainActivity
import com.example.quanlythuvien.utils.TokenManager
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Tránh vòng lặp vô hạn nếu chính API refresh-token cũng trả về 401
        if (response.request.url.encodedPath.contains("/auth/refresh-token")) {
            return null
        }

        // Dùng synchronized để xử lý lỗi gọi nhiều lần. 
        // Giả sử có 3 API cùng gọi và bị 401 cùng lúc, block này đảm bảo chỉ có 1 request đi lấy token mới.
        synchronized(this) {
            val currentAccessToken = tokenManager.getAccessToken()
            val requestAccessToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // Nếu token đã được làm mới bởi một luồng khác đang chờ, chỉ cần lấy token mới gửi lại
            if (currentAccessToken != null && currentAccessToken != requestAccessToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccessToken")
                    .build()
            }

            val refreshToken = tokenManager.getRefreshToken() ?: return null

            // Gọi đồng bộ (sync) API lấy token mới
            val newAccessToken = fetchNewToken(refreshToken)

            return if (newAccessToken != null) {
                // Thành công: Gắn token mới vào request bị lỗi lúc nãy và retry
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                // Thất bại: Refresh token cũng đã hết hạn hoặc không hợp lệ -> Đăng xuất
                tokenManager.clearTokens()
                navigateToLoginScreen()
                null
            }
        }
    }

    private fun fetchNewToken(refreshToken: String): String? {
        // Dùng OkHttpClient cơ bản để gọi API tránh vòng lặp (Circular Dependency) với RetrofitClient
        val okHttpClient = OkHttpClient()
        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = """{"refreshToken":"$refreshToken"}""".toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("http://10.0.2.2:8080/auth/refresh-token") // Sửa đúng URL Backend của bạn
            .post(requestBody)
            .build()

        return try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                if (bodyString != null) {
                    val jsonObject = JSONObject(bodyString)
                    val newAccessToken = jsonObject.getString("accessToken")
                    val newRefreshToken = jsonObject.getString("refreshToken")

                    // Lưu đè token mới vào SharedPreferences
                    tokenManager.saveTokens(newAccessToken, newRefreshToken)
                    newAccessToken
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun navigateToLoginScreen() {
        // Khởi động lại MainActivity để NavGraph tự động đá về Welcome/Login do không còn Token
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}