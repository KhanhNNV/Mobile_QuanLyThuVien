package com.example.quanlythuvien.ui.feeInvoices

import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse

sealed class InvoiceState {
    object Idle : InvoiceState()
    object Loading : InvoiceState()
    data class SuccessList(val invoices: List<FeeInvoiceResponse>) : InvoiceState()
    data class SuccessDetail(val invoice: FeeInvoiceResponse) : InvoiceState()
    data class SuccessAction(val message: String) : InvoiceState()
    data class Error(val message: String) : InvoiceState()
}