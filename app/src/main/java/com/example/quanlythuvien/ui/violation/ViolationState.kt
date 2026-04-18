package com.example.quanlythuvien.ui.violation

import com.example.quanlythuvien.data.model.response.ViolationResponse

sealed class ViolationState {
    object Idle : ViolationState()
    object Loading : ViolationState()
    data class SuccessList(val violations: List<ViolationResponse>) : ViolationState()
    data class SuccessAction(val message: String) : ViolationState()
    data class Error(val message: String) : ViolationState()
}