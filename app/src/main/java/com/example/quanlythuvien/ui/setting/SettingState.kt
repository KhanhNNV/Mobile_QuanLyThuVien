package com.example.quanlythuvien.ui.setting

import com.example.quanlythuvien.data.model.response.FeeConfigResponse
import com.example.quanlythuvien.data.model.response.LibraryResponse

sealed class SettingState {
    object Idle : SettingState()
    object Loading : SettingState()
    data class SuccessGetFees(val fees: List<FeeConfigResponse>) : SettingState()
    data class SuccessUpdate(val message: String) : SettingState()

    data class SuccessGetLibrary(val library: LibraryResponse) : SettingState()
    data class SuccessUpdateLibrary(val library: LibraryResponse) : SettingState()
    data class Error(val message: String) : SettingState()
}