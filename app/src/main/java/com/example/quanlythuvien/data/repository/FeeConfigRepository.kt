package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.model.request.FeeConfigRequest
import com.example.quanlythuvien.data.model.response.FeeConfigResponse
import com.example.quanlythuvien.data.remote.FeeConfigApiService
import com.example.quanlythuvien.data.remote.LibraryApiService
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

class FeeConfigRepository (private val apiService: FeeConfigApiService) {

    suspend fun createOrUpdateFeeConfig(request: FeeConfigRequest) = apiService.createOrUpdateFeeConfig(request)

    suspend fun getFeeConfigs()= apiService.getFeeConfigs()

}