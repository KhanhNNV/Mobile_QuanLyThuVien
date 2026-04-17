// File: app/src/main/java/com/example/quanlythuvien/ui/feeInvoices/InvoiceViewModel.kt
package com.example.quanlythuvien.ui.feeInvoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateInvoiceRequest
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse
import com.example.quanlythuvien.data.repository.FeeInvoiceRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val invoiceRepository: FeeInvoiceRepository
) : ViewModel() {

    private val _state = MutableStateFlow<InvoiceState>(InvoiceState.Idle)
    val state: StateFlow<InvoiceState> = _state.asStateFlow()

    private val _invoices = MutableStateFlow<List<FeeInvoiceResponse>>(emptyList())
    val invoices: StateFlow<List<FeeInvoiceResponse>> = _invoices.asStateFlow()

    private val _filteredInvoices = MutableStateFlow<List<FeeInvoiceResponse>>(emptyList())
    val filteredInvoices: StateFlow<List<FeeInvoiceResponse>> = _filteredInvoices.asStateFlow()

    private val _selectedInvoice = MutableStateFlow<FeeInvoiceResponse?>(null)
    val selectedInvoice: StateFlow<FeeInvoiceResponse?> = _selectedInvoice.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatus = MutableStateFlow<InvoiceStatusFilter>(InvoiceStatusFilter.ALL)
    val selectedStatus: StateFlow<InvoiceStatusFilter> = _selectedStatus.asStateFlow()

    fun fetchInvoices() {
        viewModelScope.launch {
            _state.value = InvoiceState.Loading
            try {
                val response = invoiceRepository.getInvoicesByLibrary()
                if (response.isSuccessful) {
                    val invoiceList = response.body() ?: emptyList()
                    _invoices.value = invoiceList
                    applyFilters()
                    _state.value = InvoiceState.SuccessList(invoiceList)
                } else {
                    _state.value = InvoiceState.Error("Lỗi tải dữ liệu: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Mất kết nối mạng: ${e.message}")
            }
        }
    }


    fun fetchInvoiceDetail(id: Long) {
        viewModelScope.launch {
            _state.value = InvoiceState.Loading
            try {
                // Lấy thông tin invoice
                val invoiceResponse = invoiceRepository.getInvoiceById(id)
                val invoice = invoiceResponse.body()

                if (invoice != null) {
                    _selectedInvoice.value = invoice
                    _state.value = InvoiceState.SuccessDetail(invoice)
                } else {
                    _state.value = InvoiceState.Error("Không tìm thấy hóa đơn")
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun updateInvoice(id: Long, status: String, paymentMethod: String) {
        viewModelScope.launch {
            _state.value = InvoiceState.Loading
            try {
                val request = UpdateInvoiceRequest(status, paymentMethod)
                val response = invoiceRepository.updateInvoice(id, request)
                if (response.isSuccessful) {
                    _state.value = InvoiceState.SuccessAction("Cập nhật hóa đơn thành công")
                    fetchInvoices() // Refresh list
                } else {
                    _state.value = InvoiceState.Error("Lỗi cập nhật: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun searchInvoices(keyword: String) {
        viewModelScope.launch {
            _searchQuery.value = keyword
            if (keyword.isEmpty()) {
                applyFilters()
                return@launch
            }

            try {
                val response = invoiceRepository.searchInvoices(
                    keyword = keyword,
                    status = if (_selectedStatus.value != InvoiceStatusFilter.ALL)
                        _selectedStatus.value.name else null
                )
                if (response.isSuccessful) {
                    val searchResults = response.body() ?: emptyList()
                    _filteredInvoices.value = searchResults
                }
            } catch (e: Exception) {
                // Fallback to local filter
                applyFilters()
            }
        }
    }

    fun setStatusFilter(status: InvoiceStatusFilter) {
        _selectedStatus.value = status
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = _invoices.value.filter { invoice ->
            val matchesStatus = when (_selectedStatus.value) {
                InvoiceStatusFilter.ALL -> true
                InvoiceStatusFilter.PAID -> invoice.status == "PAID"
                InvoiceStatusFilter.UNPAID -> invoice.status == "UNPAID"
            }

            val matchesSearch = if (_searchQuery.value.isEmpty()) {
                true
            } else {
                (invoice.invoiceId.toString()?.contains(_searchQuery.value, ignoreCase = true) ?: false) ||
                        (invoice.readerName?.contains(_searchQuery.value, ignoreCase = true) ?: false)
            }

            matchesStatus && matchesSearch
        }
        _filteredInvoices.value = filtered
    }

    fun formatCurrency(amount: Double): String {
        return "%,.0f VND".format(amount)
    }

    fun getStatusDisplayName(status: String): String {
        return when (status) {
            "PAID" -> "Đã thanh toán"
            "UNPAID" -> "Chưa thanh toán"
            else -> status
        }
    }
}