package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface CategoryApiService {
    @POST("api/categories/welcome")
    suspend fun createFirstCategory(@Body request: CategoryRequest): Response<CategoryResponse>

    @GET("api/categories")
    suspend fun getCategoriesByLibrary(): Response<List<CategoryResponse>>
}