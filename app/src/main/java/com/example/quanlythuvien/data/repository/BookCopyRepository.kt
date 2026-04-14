package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.BookCopyRequest
import com.example.quanlythuvien.data.model.response.BookCopyResponse
import com.example.quanlythuvien.data.remote.BookCopyApiService

class BookCopyRepository(private val apiService: BookCopyApiService) {
    suspend fun getBookCopiesByBook(bookId: Long): Result<List<BookCopyResponse>> {
        return try {
            val response = apiService.getBookCopiesByBook(bookId)
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể tải bản sao sách." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun createBookCopy(request: BookCopyRequest): Result<BookCopyResponse> {
        return try {
            val response = apiService.createBookCopy(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể thêm bản sao sách." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun deleteBookCopy(copyId: Long): Result<Unit> {
        return try {
            val response = apiService.deleteBookCopy(copyId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể xóa bản sao sách." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
