package com.example.quanlythuvien.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.enums.TypeFeeConfig
import com.example.quanlythuvien.data.model.request.FeeConfigRequest
import com.example.quanlythuvien.data.repository.FeeConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingViewModel(
    private val repository: FeeConfigRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SettingState>(SettingState.Idle)
    val state: StateFlow<SettingState> = _state

    // Map để lưu trữ configId của từng loại phí dùng cho lúc Update
    private val feeConfigIds = mutableMapOf<TypeFeeConfig, Long>()

    fun fetchFeeConfigs() {
        viewModelScope.launch {
            _state.value = SettingState.Loading
            try {
                val response = repository.getFeeConfigs()
                if (response.isSuccessful) {
                    val configs = response.body() ?: emptyList()

                    // Lưu lại ID vào Map để sử dụng khi update
                    configs.forEach { feeConfigIds[it.feeType] = it.configId }

                    _state.value = SettingState.SuccessGetFees(configs)
                } else {
                    _state.value = SettingState.Error("Lỗi tải cấu hình phí: ${response.code()}")
                }
            } catch (e: Exception) {
                _state.value = SettingState.Error("Mất kết nối mạng: ${e.message}")
            }
        }
    }

    fun updateFeeConfigs(updates: Map<TypeFeeConfig, Double>) {
        viewModelScope.launch {
            _state.value = SettingState.Loading
            try {
                // Lặp qua map dữ liệu truyền từ UI, gọi api update cho từng loại phí
                for ((feeType, amount) in updates) {
                    val configId = feeConfigIds[feeType]
                    if (configId != null) {
                        val request = FeeConfigRequest(feeType, amount)
                        repository.updateFeeConfig(request, configId)
                    }
                }

                _state.value = SettingState.SuccessUpdate("Cập nhật thành công!")
                fetchFeeConfigs() // Lấy lại dữ liệu mới nhất sau khi update

            } catch (e: Exception) {
                _state.value = SettingState.Error("Lỗi khi lưu: ${e.message}")
            }
        }
    }
}