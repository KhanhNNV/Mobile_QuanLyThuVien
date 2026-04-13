package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.LoanPolicyRequest
import com.example.quanlythuvien.data.model.response.LoanPolicyResponse
import retrofit2.Response
import retrofit2.http.*

interface LoanPolicyApiService {
    @GET("api/loan-policies")
    suspend fun getPolicies(): Response<List<LoanPolicyResponse>>

    @POST("api/loan-policies")
    suspend fun createPolicy(@Body request: LoanPolicyRequest): Response<LoanPolicyResponse>

    @PUT("api/loan-policies/{id}")
    suspend fun updatePolicy(@Path("id") id: Long, @Body request: LoanPolicyRequest): Response<LoanPolicyResponse>

    @DELETE("api/loan-policies/{id}")
    suspend fun deletePolicy(@Path("id") id: Long): Response<Unit>
}