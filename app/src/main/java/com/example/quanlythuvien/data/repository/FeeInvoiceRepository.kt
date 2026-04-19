package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.UpdateInvoiceRequest
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse
import com.example.quanlythuvien.data.remote.FeeInvoiceApiService
import retrofit2.Response

class FeeInvoiceRepository(private val apiService: FeeInvoiceApiService) {

    suspend fun getInvoicesByLibrary() = apiService.getInvoicesByLibrary()

    suspend fun getInvoiceById(id: Long) = apiService.getInvoiceById(id)

    suspend fun updateInvoice(id: Long, request: UpdateInvoiceRequest) =
        apiService.updateInvoice(id, request)

    suspend fun searchInvoices(keyword: String, status: String? = null) =
        apiService.searchInvoices(keyword, status)

    suspend fun getInvoiceByLoanDetailId(loanDetailId: Long): Response<FeeInvoiceResponse> {
        return apiService.getInvoiceByLoanDetailId(loanDetailId)
    }
}