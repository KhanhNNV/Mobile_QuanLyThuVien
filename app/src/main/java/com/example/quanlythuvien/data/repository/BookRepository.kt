package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.response.BookResponse
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.remote.BookApiService

class BookRepository(private val apiService: BookApiService) {
    suspend fun createInitialBook(request: InitialBookRequest) = apiService.createInitialBook(request)

    suspend fun getCurrentLibraryId(): Result<Long> {
        return try {
            val response = apiService.getCurrentLibraryId()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string().orEmpty().ifBlank { "Không thể tải ID thư viện hiện tại." }))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

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

    suspend fun getBooksByLibrary(): Result<List<BookResponse>> {
        return try {
            val response = apiService.getBooksByLibrary()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception(extractUserFriendlyError(response.errorBody()?.string())))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun extractUserFriendlyError(rawError: String?): String {
        if (rawError.isNullOrBlank()) {
            return "Không thể tải danh sách sách. Vui lòng thử lại."
        }
        return when {
            rawError.contains("404", ignoreCase = true) || rawError.contains("Not Found", ignoreCase = true) ->
                "Không tìm thấy API danh sách sách. Vui lòng kiểm tra backend."
            rawError.contains("Method Not Allowed", ignoreCase = true) ->
                "API danh sách sách chưa được cấu hình đúng phương thức GET."
            rawError.contains("Bad Request", ignoreCase = true) ->
                "Yêu cầu chưa hợp lệ. Vui lòng thử lại."
            rawError.contains("<html", ignoreCase = true) || rawError.contains("<!doctype", ignoreCase = true) ->
                "Backend đang trả về trang lỗi thay vì JSON hợp lệ."
            else -> "Không thể tải danh sách sách. Vui lòng thử lại."
        }
    }
}