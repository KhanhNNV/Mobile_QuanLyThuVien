package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CategoryApiService {
    @POST("api/categories")
    suspend fun createCategory(@Body request: CategoryRequest): Response<CategoryResponse>
}