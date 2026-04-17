package com.example.quanlythuvien.ui.feeInvoices

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InvoiceSharedViewModel : ViewModel() {

    private val _selectedInvoiceId = MutableStateFlow<Long?>(null)
    val selectedInvoiceId: StateFlow<Long?> = _selectedInvoiceId.asStateFlow()

    private val _shouldRefreshList = MutableStateFlow(false)
    val shouldRefreshList: StateFlow<Boolean> = _shouldRefreshList.asStateFlow()

    fun selectInvoice(id: Long) {
        _selectedInvoiceId.value = id
    }

    fun clearSelectedInvoice() {
        _selectedInvoiceId.value = null
    }

    fun requestRefreshList() {
        _shouldRefreshList.value = true
    }

    fun resetRefreshFlag() {
        _shouldRefreshList.value = false
    }
}