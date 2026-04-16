package com.example.quanlythuvien.ui.admin

import com.example.quanlythuvien.data.model.response.UserResponse

sealed class StaffState {
    object Idle : StaffState()
    object Loading : StaffState()
    data class SuccessList(val users: List<UserResponse>) : StaffState()
    data class SuccessAction(val message: String) : StaffState()
    data class Error(val message: String) : StaffState()
}