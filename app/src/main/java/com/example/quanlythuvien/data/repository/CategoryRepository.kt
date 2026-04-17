package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.core.network.ApiErrorParser
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.remote.CategoryApiService

class CategoryRepository(private val apiService: CategoryApiService) {
    suspend fun createFirstCategory(request: CategoryRequest) = apiService.createFirstCategory(request)

    suspend fun getCategoriesByLibrary() = apiService.getCategoriesByLibrary()
    suspend fun createCategory(request: CategoryRequest) = apiService.createCategory(request)

    suspend fun getCategories() = apiService.getCategories()

    suspend fun updateCategory(id: Long, request: CategoryRequest) = apiService.updateCategory(id, request)

    suspend fun deleteCategory(id: Long) = apiService.deleteCategory(id)

    suspend fun createCategoryResult(request: CategoryRequest): Result<Unit> {
        return safeResult("Không thể thêm danh mục. Vui lòng thử lại.") {
            apiService.createCategory(request)
        }
    }

    suspend fun updateCategoryResult(id: Long, request: CategoryRequest): Result<Unit> {
        return safeResult("Không thể cập nhật danh mục. Vui lòng thử lại.") {
            apiService.updateCategory(id, request)
        }
    }

    suspend fun deleteCategoryResult(id: Long): Result<Unit> {
        return safeResult("Không thể xóa danh mục. Vui lòng thử lại.") {
            apiService.deleteCategory(id)
        }
    }

    private suspend fun safeResult(
        fallback: String,
        call: suspend () -> retrofit2.Response<*>
    ): Result<Unit> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(ApiErrorParser.parseErrorMessage(response, fallback)))
            }
        } catch (exception: Exception) {
            Result.failure(Exception(exception.message ?: fallback, exception))
        }
    }
}
