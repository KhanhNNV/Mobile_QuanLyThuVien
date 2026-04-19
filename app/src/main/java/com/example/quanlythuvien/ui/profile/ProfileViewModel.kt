package com.example.quanlythuvien.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateProfileRequest
import com.example.quanlythuvien.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadMyProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val response = userRepository.getMyProfile()
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileState.LoadSuccess(response.body()!!)
                } else {
                    _profileState.value = ProfileState.Error("Không thể tải thông tin: Mã ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun updateMyProfile(fullname: String, newPassword: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val request = UpdateProfileRequest(
                    fullname = fullname,
                    password = if (newPassword.isNullOrBlank()) null else newPassword
                )

                val response = userRepository.updateMyProfile(request)
                if (response.isSuccessful && response.body() != null) {
                    _profileState.value = ProfileState.UpdateSuccess(response.body()!!)
                } else {
                    _profileState.value = ProfileState.Error("Cập nhật thất bại: Mã ${response.code()}")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}