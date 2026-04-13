package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LibraryApiService

class LibraryRepository(private val apiService: LibraryApiService) {
    suspend fun getLibraryConfig() = apiService.getLibraryConfig()
}