package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.remote.CategoryApiService

class CategoryRepository(private val apiService: CategoryApiService) {
    suspend fun createFirstCategory(request: CategoryRequest) = apiService.createFirstCategory(request)

    suspend fun getCategoriesByLibrary()= apiService.getCategoriesByLibrary()
    suspend fun createCategory(request: CategoryRequest) = apiService.createCategory(request)

    suspend fun getCategories() = apiService.getCategories()

    suspend fun updateCategory(id: Long, request: CategoryRequest) = apiService.updateCategory(id, request)

    suspend fun deleteCategory(id: Long) = apiService.deleteCategory(id)
}
