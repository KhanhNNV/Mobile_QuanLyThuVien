package com.example.quanlythuvien.ui.violation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateViolationRequest
import com.example.quanlythuvien.data.model.response.ViolationResponse
import com.example.quanlythuvien.data.repository.ViolationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViolationViewModel(
    private val repository: ViolationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ViolationState>(ViolationState.Idle)
    val state: StateFlow<ViolationState> = _state

    // --- CÁC BIẾN QUẢN LÝ PHÂN TRANG ---
    private val currentViolationList = mutableListOf<ViolationResponse>()
    var isLoading = false
    var isLastPage = false
    private var currentPage = 0
    private val pageSize = 5

    // Lưu lại các điều kiện lọc để dùng cho việc tải trang tiếp theo
    private var currentSearch: String? = null
    private var currentStatus: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null

    // Lấy danh sách vi phạm
    // isRefresh = true nghĩa là load lại từ đầu (trang 0)
    // isRefresh = false nghĩa là load trang tiếp theo
    fun fetchViolations(
        isRefresh: Boolean = true,
        search: String? = currentSearch,
        status: String? = currentStatus,
        startDate: String? = currentStartDate,
        endDate: String? = currentEndDate
    ) {
        if (isLoading) return // Đang tải thì không gọi API tiếp
        if (!isRefresh && isLastPage) return // Đã hết dữ liệu và không phải làm mới thì dừng

        viewModelScope.launch {
            if (isRefresh) {
                currentPage = 0
                isLastPage = false
                currentViolationList.clear()

                // Cập nhật lại các biến filter
                currentSearch = search
                currentStatus = status
                currentStartDate = startDate
                currentEndDate = endDate
            } else {
                currentPage++ // Tăng trang lên nếu là load thêm
            }

            isLoading = true
            // Chỉ hiển thị Loading UI toàn màn hình nếu là Refresh
            if (isRefresh) _state.value = ViolationState.Loading

            try {
                val response = repository.getViolations(
                    currentSearch, currentStatus, currentStartDate, currentEndDate, currentPage, pageSize
                )

                if (response.isSuccessful) {
                    val pageResponse = response.body()
                    val newItems = pageResponse?.content ?: emptyList()

                    // Nếu số lượng item trả về ít hơn pageSize, nghĩa là đã đến trang cuối
                    if (newItems.size < pageSize) {
                        isLastPage = true
                    }

                    currentViolationList.addAll(newItems)

                    // Gửi danh sách đã cộng dồn qua UI
                    _state.value = ViolationState.SuccessList(currentViolationList.toList())
                } else {
                    _state.value = ViolationState.Error("Lỗi tải dữ liệu. Mã lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = ViolationState.Error(e.message ?: "Đã có lỗi xảy ra khi tải dữ liệu")
            } finally {
                isLoading = false // Đặt lại cờ sau khi gọi xong
            }
        }
    }

    // Cập nhật vi phạm
    fun updateViolation(violationId: Long, reason: String, status: String) {
        viewModelScope.launch {
            _state.value = ViolationState.Loading
            try {
                // Tạo Request Body bằng Data Class
                val requestBody = UpdateViolationRequest(reason = reason, status = status)

                // Gọi API
                val response = repository.updateViolation(violationId, requestBody)

                if (response.isSuccessful) {
                    _state.value = ViolationState.SuccessAction("Cập nhật vi phạm thành công!")
                } else {
                    _state.value = ViolationState.Error("Lỗi khi cập nhật. Mã lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = ViolationState.Error(e.message ?: "Lỗi ngoại lệ khi cập nhật vi phạm")
            }
        }
    }

    // Xóa vi phạm
    fun deleteViolation(violationId: Long) {
        viewModelScope.launch {
            _state.value = ViolationState.Loading
            try {
                val response = repository.deleteViolation(violationId)

                if (response.isSuccessful) {
                    _state.value = ViolationState.SuccessAction("Xóa vi phạm thành công!")
                } else {
                    _state.value = ViolationState.Error("Lỗi khi xóa. Mã lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = ViolationState.Error(e.message ?: "Lỗi ngoại lệ khi xóa vi phạm")
            }
        }
    }
}