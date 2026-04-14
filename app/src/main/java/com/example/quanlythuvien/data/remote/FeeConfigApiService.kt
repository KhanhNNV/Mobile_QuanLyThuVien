package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.FeeConfigRequest
import com.example.quanlythuvien.data.model.response.FeeConfigResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FeeConfigApiService {
    @GET("/api/fee-configs")
    suspend fun getFeeConfigs(): Response<List<FeeConfigResponse>>

    @PUT("api/fee-configs/{configId}")
    suspend fun updateFeeConfig(
        @Path("configId") configId: Long,
        @Body request: FeeConfigRequest
    ): Response<FeeConfigResponse>
}