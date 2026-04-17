package com.example.quanlythuvien.ui.admin

import com.example.quanlythuvien.data.model.response.UserResponse

sealed class StaffAddState {
    object Idle : StaffAddState()
    object Loading : StaffAddState()
    data class Success(val data: UserResponse) :StaffAddState()
    data class Error(val message: String) : StaffAddState()
}