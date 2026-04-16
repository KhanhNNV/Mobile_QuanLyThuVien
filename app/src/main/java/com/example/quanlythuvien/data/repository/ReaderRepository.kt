package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.ReaderRequest
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.ReaderApiService

class ReaderRepository (private val apiService: ReaderApiService){
    suspend fun countReaders() = apiService.countReaders()
    suspend fun createReader(request: ReaderRequest) = apiService.createReader(request)

    suspend fun getReadersPaginated(page: Int, size: Int) = apiService.getReaders(page, size)

    suspend fun searchReaders(query: String) = apiService.searchReaders(query)

    suspend fun getReaderById(readerId: Long) = apiService.getReaderById(readerId)

}