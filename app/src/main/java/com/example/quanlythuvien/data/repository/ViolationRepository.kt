package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.UpdateViolationRequest
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.remote.ViolationApiService

class ViolationRepository (private val apiService: ViolationApiService){
    suspend fun getViolationQuantityAlerts() = apiService.getViolationQuantityAlerts()

    suspend fun getViolations(
        search: String? = null,
        status: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 0,
        size: Int = 10
    ) = apiService.getViolations(search, status, startDate, endDate, page, size)

    suspend fun updateViolation(id: Long, request: UpdateViolationRequest) = apiService.updateViolation(id, request)

    suspend fun deleteViolation(id: Long) = apiService.deleteViolation(id)
}