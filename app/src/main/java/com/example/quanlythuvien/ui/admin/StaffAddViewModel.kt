package com.example.quanlythuvien.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UserRequest
import com.example.quanlythuvien.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffAddViewModel(private val repository: UserRepository) : ViewModel() {
    private val _addState = MutableStateFlow<StaffAddState>(StaffAddState.Idle)
    val addState: StateFlow<StaffAddState> = _addState

    fun addStaff(request: UserRequest) {
        viewModelScope.launch {
            _addState.value = StaffAddState.Loading
            try {
                val response = repository.createUser(request)
                if (response.isSuccessful && response.body() != null) {
                    _addState.value = StaffAddState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi thêm nhân viên"
                    _addState.value = StaffAddState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _addState.value = StaffAddState.Error(e.message ?: "Mất kết nối")
            }
        }
    }
}