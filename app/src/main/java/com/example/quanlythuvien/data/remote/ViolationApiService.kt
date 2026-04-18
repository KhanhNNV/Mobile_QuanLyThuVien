package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.UpdateViolationRequest
import com.example.quanlythuvien.data.model.response.PageResponse
import com.example.quanlythuvien.data.model.response.ViolationResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ViolationApiService {
    @GET("api/violations/alert")
    suspend fun getViolationQuantityAlerts(): Response<List<String>>


    @GET("api/violations")
    suspend fun getViolations(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("startDate") startDate: String? = null, // format: 2026-04-01T00:00:00
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<ViolationResponse>>


    @PUT("api/violations/{id}")
    suspend fun updateViolation(
        @Path("id") id: Long,
        @Body request: UpdateViolationRequest
    ): Response<ViolationResponse>


    @DELETE("api/violations/{id}")
    suspend fun deleteViolation(
        @Path("id") id: Long
    ): Response<ResponseBody>
}