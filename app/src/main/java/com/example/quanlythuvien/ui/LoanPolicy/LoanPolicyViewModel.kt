package com.example.quanlythuvien.ui.LoanPolicy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.LoanPolicyRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.data.repository.LoanPolicyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoanPolicyViewModel(
    private val repository: LoanPolicyRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _state = MutableStateFlow<PolicyState>(PolicyState.Idle)
    val state: StateFlow<PolicyState> = _state

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

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
}