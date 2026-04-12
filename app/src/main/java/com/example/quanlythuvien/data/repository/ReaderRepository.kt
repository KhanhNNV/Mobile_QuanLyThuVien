package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.InitialBookRequest
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.ReaderApiService

class ReaderRepository (private val apiService: ReaderApiService){
    suspend fun countReaders(libraryId: Long) = apiService.countReaders(libraryId)
}