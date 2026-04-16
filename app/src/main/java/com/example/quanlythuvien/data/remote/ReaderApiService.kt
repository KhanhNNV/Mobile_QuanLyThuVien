package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.response.PageResponse
import com.example.quanlythuvien.data.model.response.ReaderResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReaderApiService {
    @GET("api/readers/count")
    suspend fun countReaders(): Response<Long>

    //API Gọi danh sách reader (Phân trang)
    @GET("api/readers")
    suspend fun getReaders(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PageResponse<ReaderResponse>>

    //API gọi search reader
    @GET("api/readers/search")
    suspend fun searchReaders(
        @Query("query") query: String
    ): Response<List<ReaderResponse>>

    @GET("api/readers/{id}")
    suspend fun getReaderById(@Path("id") readerId: Long): Response<ReaderResponse>
}