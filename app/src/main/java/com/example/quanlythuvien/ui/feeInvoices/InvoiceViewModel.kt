package com.example.quanlythuvien.ui.feeInvoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateInvoiceRequest
import com.example.quanlythuvien.data.model.response.FeeInvoiceResponse
import com.example.quanlythuvien.data.repository.FeeInvoiceRepository
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

    private val _selectedInvoice = MutableStateFlow<FeeInvoiceResponse?>(null)
    val selectedInvoice: StateFlow<FeeInvoiceResponse?> = _selectedInvoice.asStateFlow()

    private val _selectedStatus = MutableStateFlow<InvoiceStatusFilter>(InvoiceStatusFilter.ALL)
    val selectedStatus: StateFlow<InvoiceStatusFilter> = _selectedStatus.asStateFlow()

    // --- Biến quản lý phân trang và tìm kiếm ---
    var isLoading = false
    var isLastPage = false
    var currentPage = 0
    private var currentKeyword: String? = null
    private var currentStatusString: String? = null // Lưu trạng thái dưới dạng String để gửi API
    val pageSize = 10

    /**
     * Gọi API lấy danh sách hóa đơn.
     * @param isRefresh: true nếu muốn tải lại từ trang 0 (khi search, đổi status, hoặc pull to refresh)
     * @param keyword: Từ khóa tìm kiếm
     * @param status: Trạng thái muốn lọc (ALL, PAID, UNPAID)
     */
    fun fetchInvoices(
        isRefresh: Boolean = false,
        keyword: String? = currentKeyword,
        status: InvoiceStatusFilter = _selectedStatus.value
    ) {
        if (isLoading) return

        if (isRefresh) {
            currentPage = 0
            isLastPage = false
            currentKeyword = keyword
            _selectedStatus.value = status // Cập nhật state cho UI (Spinner)

            // Chuyển Enum thành String ("PAID", "UNPAID", hoặc null) để gửi lên Server
            currentStatusString = when (status) {
                InvoiceStatusFilter.ALL -> null
                InvoiceStatusFilter.PAID -> "PAID"
                InvoiceStatusFilter.UNPAID -> "UNPAID"
            }

            _invoices.value = emptyList() // Clear dữ liệu cũ
        } else {
            if (isLastPage) return // Không tải thêm nếu đã hết dữ liệu
        }

        viewModelScope.launch {
            isLoading = true
            _state.value = InvoiceState.Loading

            try {
                // Gọi Repository truyền cả keyword, status, page, size
                val response = invoiceRepository.searchInvoices(
                    keyword = currentKeyword,
                    status = currentStatusString,
                    page = currentPage,
                    size = pageSize
                )

                if (response.isSuccessful) {
                    val pageResponse = response.body()
                    val newInvoices = pageResponse?.content ?: emptyList()

                    // Nối danh sách
                    val currentList = if (isRefresh) emptyList() else _invoices.value
                    val updatedList = currentList + newInvoices

                    _invoices.value = updatedList

                    // Cập nhật biến phân trang
                    isLastPage = pageResponse?.last ?: true
                    if (!isLastPage) {
                        currentPage++
                    }

                    _state.value = InvoiceState.SuccessList(updatedList)
                } else {
                    _state.value = InvoiceState.Error("Lỗi tải dữ liệu: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Lỗi kết nối mạng: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchInvoiceByLoanDetailId(loanDetailId: Long) {
        viewModelScope.launch {
            _state.value = InvoiceState.Loading // Bật trạng thái loading
            try {
                val response = invoiceRepository.getInvoiceByLoanDetailId(loanDetailId)

                if (response.isSuccessful && response.body() != null) {
                    // Nếu gọi thành công -> gán data vào selectedInvoice để UI tự động vẽ ra
                    _selectedInvoice.value = response.body()
                    _state.value = InvoiceState.SuccessDetail(response.body()!!)
                } else {
                    // Xử lý lỗi (ví dụ Backend trả về 404 Not Found)
                    if (response.code() == 404) {
                        _state.value = InvoiceState.Error("Lỗi 404: Không tìm thấy hóa đơn cho chi tiết mượn này.")
                    } else {
                        _state.value = InvoiceState.Error("Lỗi tải dữ liệu: Mã ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Lỗi kết nối mạng: ${e.localizedMessage}")
            }
        }
    }

    fun fetchInvoiceDetail(id: Long) {
        viewModelScope.launch {
            _state.value = InvoiceState.Loading
            try {
                val response = invoiceRepository.getInvoiceById(id)
                val invoice = response.body()
                if (response.isSuccessful && invoice != null) {
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
                    fetchInvoices(isRefresh = true) // Cập nhật xong thì tải lại từ trang 0
                } else {
                    _state.value = InvoiceState.Error("Lỗi cập nhật: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = InvoiceState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    // Khi người dùng đổi trạng thái trên Spinner, gọi API làm mới danh sách
    fun setStatusFilter(status: InvoiceStatusFilter) {
        if (_selectedStatus.value != status) {
            fetchInvoices(isRefresh = true, status = status)
        }
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