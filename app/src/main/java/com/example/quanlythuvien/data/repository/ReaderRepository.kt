package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.ReaderApiService

class ReaderRepository (private val apiService: ReaderApiService){
    suspend fun countReaders() = apiService.countReaders()

    suspend fun getReadersPaginated(page: Int, size: Int) = apiService.getReaders(page, size)

    suspend fun searchReaders(query: String) = apiService.searchReaders(query)
}