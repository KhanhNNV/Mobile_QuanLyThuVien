package com.example.quanlythuvien.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.LoginRequest
import com.example.quanlythuvien.data.model.request.RegisterRequest
import com.example.quanlythuvien.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun registerAndLogin(request: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                // Bước 1: Gọi API Đăng ký
                val regResponse = repository.register(request)

                if (regResponse.isSuccessful) {
                    // Bước 2: Đăng ký thành công -> Gọi tiếp API Đăng nhập
                    val loginRequest = LoginRequest(request.username, request.password)
                    val loginResponse = repository.login(loginRequest)

                    if (loginResponse.isSuccessful && loginResponse.body() != null) {
                        // Bước 3: Lấy 2 Token từ Body của Response
                        val accessToken = loginResponse.body()!!.accessToken
                        val refreshToken = loginResponse.body()!!.refreshToken

                        // Bước 4: Trả về trạng thái Success kèm theo Token
                        _registerState.value = RegisterState.Success(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            message = "Đăng ký và Đăng nhập thành công!"
                        )
                    } else {
                        _registerState.value =
                            RegisterState.Error("Đăng ký thành công nhưng đăng nhập tự động thất bại.")
                    }
                } else {
                    // Xử lý lỗi nếu backend trả về HTTP 400, 500,...
                    val errorMsg =
                        regResponse.errorBody()?.string() ?: "Lỗi mã: ${regResponse.code()}"
                    _registerState.value = RegisterState.Error("Lỗi đăng ký: $errorMsg")
                }
            } catch (e: Exception) {
                // Xử lý lỗi mạng (mất mạng, server tắt, v.v.)
                _registerState.value = RegisterState.Error(e.message ?: "Lỗi kết nối mạng")
            }
        }
    }
}