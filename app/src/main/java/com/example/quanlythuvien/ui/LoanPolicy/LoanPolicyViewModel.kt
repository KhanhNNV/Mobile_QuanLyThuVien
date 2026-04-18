package com.example.quanlythuvien.ui.LoanPolicy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.LoanPolicyRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.data.repository.LibraryRepository
import com.example.quanlythuvien.data.repository.LoanPolicyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoanPolicyViewModel(
    private val repository: LoanPolicyRepository,
    private val categoryRepository: CategoryRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PolicyState>(PolicyState.Idle)
    val state: StateFlow<PolicyState> = _state

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

    // Hạn ngạch phiếu mượn
    private val _libraryQuota = MutableStateFlow<Int>(0)
    val libraryQuota: StateFlow<Int> = _libraryQuota

    // Hạn ngạch sách
    private val _libraryBooksQuota = MutableStateFlow<Int>(0)
    val libraryBooksQuota: StateFlow<Int> = _libraryBooksQuota

    fun fetchPolicies() {
        viewModelScope.launch {
            _state.value = PolicyState.Loading
            try {
                val response = repository.getPolicies()
                if (response.isSuccessful) {
                    _state.value = PolicyState.SuccessList(response.body() ?: emptyList())
                } else {
                    _state.value = PolicyState.Error("Lỗi tải dữ liệu: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PolicyState.Error(e.message ?: "Mất kết nối mạng")
            }
        }
    }

    fun savePolicy(policyId: Long?, categoryId: Long?, maxDays: Int) {
        viewModelScope.launch {
            _state.value = PolicyState.Loading
            try {
                val req = LoanPolicyRequest(categoryId, maxDays)
                val response = if (policyId == null) {
                    repository.createPolicy(req)
                } else {
                    repository.updatePolicy(policyId, req)
                }

                if (response.isSuccessful) {
                    _state.value = PolicyState.SuccessAction(if (policyId == null) "Thêm thành công" else "Cập nhật thành công")
                    fetchPolicies() // Refresh lại list
                } else {
                    _state.value = PolicyState.Error("Lỗi lưu dữ liệu: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PolicyState.Error("Mất kết nối mạng")
            }
        }
    }

    fun deletePolicy(id: Long) {
        viewModelScope.launch {
            _state.value = PolicyState.Loading
            try {
                val response = repository.deletePolicy(id)
                if (response.isSuccessful) {
                    _state.value = PolicyState.SuccessAction("Xóa thành công")
                    fetchPolicies()
                } else {
                    _state.value = PolicyState.Error("Lỗi xóa: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PolicyState.Error("Mất kết nối mạng")
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = categoryRepository.getCategoriesByLibrary()
                if (response.isSuccessful) {
                    _categories.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }

    fun fetchLibraryQuota() {
        viewModelScope.launch {
            try {
                val response = libraryRepository.getLibraryById()
                if (response.isSuccessful) {
                    _libraryQuota.value = response.body()?.maxLoansQuota ?: 0
                    _libraryBooksQuota.value = response.body()?.maxBooksQuota ?: 0
                }
            } catch (e: Exception) {
            }
        }
    }

    // Hàm gọi API cập nhật Quota
    fun updateLibraryQuota(newQuota: Int) {
        viewModelScope.launch {
            _state.value = PolicyState.Loading
            try {
                val response = libraryRepository.updateLibraryLoansQuota(newQuota)
                if (response.isSuccessful) {
                    _state.value = PolicyState.SuccessAction("Cập nhật hạn ngạch thành công!")
                    _libraryQuota.value = newQuota
                } else {
                    _state.value = PolicyState.Error("Lỗi cập nhật hạn ngạch: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PolicyState.Error("Mất kết nối mạng")
            }
        }
    }

    fun updateLibraryBooksQuota(newQuota: Int) {
        viewModelScope.launch {
            _state.value = PolicyState.Loading
            try {
                // Đảm bảo bạn đã khai báo hàm này trong LibraryRepository
                val response = libraryRepository.updateLibraryBooksQuota(newQuota)
                if (response.isSuccessful) {
                    _state.value = PolicyState.SuccessAction("Cập nhật hạn ngạch sách thành công!")
                    _libraryBooksQuota.value = newQuota
                } else {
                    _state.value = PolicyState.Error("Lỗi cập nhật hạn ngạch sách: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = PolicyState.Error("Mất kết nối mạng")
            }
        }
    }
}