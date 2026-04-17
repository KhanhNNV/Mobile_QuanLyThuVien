package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.remote.ViolationApiService

class ViolationRepository (private val apiService: ViolationApiService){
    suspend fun getViolationQuantityAlerts() = apiService.getViolationQuantityAlerts()
}