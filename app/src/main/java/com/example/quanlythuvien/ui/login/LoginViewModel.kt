package com.example.quanlythuvien.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.LoginRequest
import com.example.quanlythuvien.data.repository.AuthRepository
import com.example.quanlythuvien.utils.JwtUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    val accessToken = body.accessToken
                    val refreshToken = body.refreshToken

                    // giải mã token để lấy role
                    val role = JwtUtils.getRoleFromToken(accessToken)

                    _loginState.value = LoginState.Success(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        role = role,
                        username = request.username
                    )
                } else {
                    _loginState.value = LoginState.Error("Sai tài khoản hoặc mật khẩu!")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }
}
