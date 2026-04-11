package com.example.quanlythuvien.core.api


import com.example.quanlythuvien.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Lấy request ban đầu
        val originalRequest = chain.request()

        // Bỏ qua việc kẹp token cho các API Auth
        if (originalRequest.url.encodedPath.contains("/auth/login") ||
            originalRequest.url.encodedPath.contains("/auth/register")) {
            return chain.proceed(originalRequest)
        }

        // Tạo một request mới dựa trên request cũ, kẹp thêm Header
        val accessToken = tokenManager.getAccessToken()
        val requestBuilder = originalRequest.newBuilder()

        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        // Cho request đi tiếp
        return chain.proceed(requestBuilder.build())
    }
}