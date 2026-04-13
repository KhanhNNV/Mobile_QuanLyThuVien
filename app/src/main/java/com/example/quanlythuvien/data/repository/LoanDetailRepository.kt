package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.remote.LoanDetailApiService

class LoanDetailRepository(private val apiService: LoanDetailApiService) {
    suspend fun getDueTodayAlerts() = apiService.getDueTodayAlerts()
}