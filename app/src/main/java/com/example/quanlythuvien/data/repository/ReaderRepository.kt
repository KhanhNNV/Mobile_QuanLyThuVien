package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.response.ReaderResponse
import com.example.quanlythuvien.data.remote.ReaderApiService

class ReaderRepository (private val apiService: ReaderApiService){
    suspend fun countReaders() = apiService.countReaders()

    suspend fun getReadersPaginated(page: Int, size: Int) = apiService.getReaders(page, size)

    suspend fun searchReaders(query: String) = apiService.searchReaders(query)

    suspend fun deletedReader(readerId: Long) = apiService.deleteReader(readerId)

    suspend fun editReader(readerId:Long, request: ReaderResponse) = apiService.updateReader(readerId, request)
}