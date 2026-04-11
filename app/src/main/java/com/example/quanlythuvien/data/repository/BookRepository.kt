package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.remote.BookApiService

class BookRepository(private val apiService: BookApiService) {
    suspend fun createInitialBook(request: InitialBookRequest) = apiService.createInitialBook(request)
}