package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.model.request.UpdateInvoiceRequest
import com.example.quanlythuvien.data.remote.FeeInvoiceApiService

class FeeInvoiceRepository(private val apiService: FeeInvoiceApiService) {

    suspend fun getInvoicesByLibrary() = apiService.getInvoicesByLibrary()

    suspend fun getInvoiceById(id: Long) = apiService.getInvoiceById(id)

    suspend fun updateInvoice(id: Long, request: UpdateInvoiceRequest) =
        apiService.updateInvoice(id, request)

    suspend fun searchInvoices(keyword: String? = null,status: String?=null, page: Int = 0, size: Int = 10) =
        apiService.searchInvoices(keyword = keyword,status=status, page = page, size = size)
}