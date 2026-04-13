package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.LoanPolicyRequest
import com.example.quanlythuvien.data.remote.LoanPolicyApiService

class LoanPolicyRepository(private val apiService: LoanPolicyApiService) {
    suspend fun getPolicies() = apiService.getPolicies()
    suspend fun createPolicy(req: LoanPolicyRequest) = apiService.createPolicy(req)
    suspend fun updatePolicy(id: Long, req: LoanPolicyRequest) = apiService.updatePolicy(id, req)
    suspend fun deletePolicy(id: Long) = apiService.deletePolicy(id)
}