package com.example.quanlythuvien.ui.setting

import com.example.quanlythuvien.data.model.response.FeeConfigResponse

sealed class SettingState {
    object Idle : SettingState()
    object Loading : SettingState()
    data class SuccessGetFees(val fees: List<FeeConfigResponse>) : SettingState()
    data class SuccessUpdate(val message: String) : SettingState()
    data class Error(val message: String) : SettingState()
}