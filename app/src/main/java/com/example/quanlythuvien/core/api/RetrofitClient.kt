package com.example.quanlythuvien.core.api

import android.content.Context
import com.example.quanlythuvien.utils.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 là IP trỏ về localhost của máy tính khi dùng Android Emulator
    private const val BASE_URL = "http://10.0.2.2:8080/"
    @Volatile
    private var instance: Retrofit? = null

    fun getInstance(context: Context): Retrofit {
        return instance ?: synchronized(this) {
            instance ?: buildRetrofit(context.applicationContext).also { instance = it }
        }
    }

    private fun buildRetrofit(context: Context): Retrofit {
        val tokenManager = TokenManager(context)
        val authInterceptor = AuthInterceptor(tokenManager)

        // Log API ra Logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor) // Ghi log request/response
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gắn OkHttp vào Retrofit
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}