package com.example.quanlythuvien.data.remote

import com.example.quanlythuvien.data.model.request.UpdateInvoiceRequest
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse
import com.example.quanlythuvien.data.model.response.PageResponse
import retrofit2.Response
import retrofit2.http.*

interface FeeInvoiceApiService {
  
  
@GET("api/fee-invoices/loan-detail/{loanDetailId}")
    suspend fun getInvoiceByLoanDetailId(
        @Path("loanDetailId") loanDetailId: Long
    ): Response<FeeInvoiceResponse>
  
  
    @GET("api/fee-invoices")
    suspend fun getInvoicesByLibrary(): Response<List<FeeInvoiceResponse>>

    @GET("api/fee-invoices/{id}")
    suspend fun getInvoiceById(@Path("id") id: Long): Response<FeeInvoiceResponse>

    @PUT("api/fee-invoices/{id}")
    suspend fun updateInvoice(
        @Path("id") id: Long,
        @Body request: UpdateInvoiceRequest
    ): Response<FeeInvoiceResponse>


    @GET("api/fee-invoices/search")
    suspend fun searchInvoices(
        @Query("keyword") keyword: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<PageResponse<FeeInvoiceResponse>>
}