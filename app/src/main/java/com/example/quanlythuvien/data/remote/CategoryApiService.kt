package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryApiService {
    @POST("api/categories/welcome")
    suspend fun createFirstCategory(@Body request: CategoryRequest): Response<CategoryResponse>

    @GET("api/categories")
    suspend fun getCategoriesByLibrary(): Response<List<CategoryResponse>>

    @POST("api/categories")
    suspend fun createCategory(@Body request: CategoryRequest): Response<CategoryResponse>

    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryResponse>>
    @PUT("api/categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") categoryId: Long,
        @Body request: CategoryRequest
    ): Response<CategoryResponse>

    @DELETE("api/categories/{categoryId}")
    suspend fun deleteCategory(@Path("categoryId") categoryId: Long): Response<Unit>
}