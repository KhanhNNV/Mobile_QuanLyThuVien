package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.response.BookResponse
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.remote.BookApiService

class BookRepository(private val apiService: BookApiService) {
    suspend fun createInitialBook(request: InitialBookRequest) = apiService.createInitialBook(request)

    suspend fun countBooksByLibrary() = apiService.countBooksByLibrary()

    suspend fun getLowCopyAlerts() = apiService.getLowCopyAlerts()

    suspend fun createBook(request: BookRequest) = apiService.createBook(request)

    suspend fun getBookById(bookId: Long): Result<BookResponse> {
        return try {
            val response = apiService.getBookById(bookId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể tải chi tiết sách." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun updateBook(bookId: Long, request: BookRequest): Result<BookResponse> {
        return try {
            val response = apiService.updateBook(bookId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể cập nhật sách." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getBooksByLibrary(libraryId: Long): Result<List<BookResponse>> {
        return try {
            val response = apiService.getBooksByLibrary(libraryId)
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Khong the tai danh sach sach." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}