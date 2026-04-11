package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.remote.CategoryApiService

class CategoryRepository(private val apiService: CategoryApiService) {
    suspend fun createCategory(request: CategoryRequest) = apiService.createCategory(request)
}