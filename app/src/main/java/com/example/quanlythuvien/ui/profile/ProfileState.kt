package com.example.quanlythuvien.ui.profile

import com.example.quanlythuvien.data.model.response.UserResponse

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    data class LoadSuccess(val user: UserResponse) : ProfileState()
    data class UpdateSuccess(val user: UserResponse) : ProfileState()
    data class Error(val message: String) : ProfileState()
}