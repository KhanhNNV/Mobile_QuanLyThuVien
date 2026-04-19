package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.LibraryRequest
import com.example.quanlythuvien.data.remote.LibraryApiService

class LibraryRepository(private val apiService: LibraryApiService) {

    suspend fun updateLibrary(request: LibraryRequest) = apiService.updateLibrary(request)
    suspend fun getLibraryById() = apiService.getLibraryById()

    suspend fun updateLibraryLoansQuota(quota: Int) = apiService.updateLibraryLoansQuota(quota)

    suspend fun updateLibraryBooksQuota(quota: Int) = apiService.updateLibraryBooksQuota(quota)
}