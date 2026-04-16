package com.example.quanlythuvien.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.UpdateUserRequest
import com.example.quanlythuvien.data.model.response.UserResponse
import com.example.quanlythuvien.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StaffViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<StaffState>(StaffState.Idle)
    val state: StateFlow<StaffState> = _state.asStateFlow()

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users.asStateFlow()

    private val _filteredUsers = MutableStateFlow<List<UserResponse>>(emptyList())
    val filteredUsers: StateFlow<List<UserResponse>> = _filteredUsers.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StaffFilter.ALL)
    val selectedFilter: StateFlow<StaffFilter> = _selectedFilter.asStateFlow()

    fun fetchUsers() {
        viewModelScope.launch {
            _state.value = StaffState.Loading
            try {
                val response = repository.getAllUsers()
                if (response.isSuccessful) {
                    val userList = response.body() ?: emptyList()
                    _users.value = userList
                    applyFilters()
                    _state.value = StaffState.SuccessList(userList)
                } else {
                    val errorMsg = when (response.code()) {
                        403 -> "Bạn không có quyền truy cập"
                        else -> "Lỗi tải dữ liệu: ${response.code()}"
                    }
                    _state.value = StaffState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = StaffState.Error("Mất kết nối mạng: ${e.message}")
            }
        }
    }

    fun updateUser(id: Long, fullname: String?, role: String, isActive: Boolean?) {
        viewModelScope.launch {
            _state.value = StaffState.Loading
            try {
                val request = UpdateUserRequest(
                    fullname = fullname?.takeIf { it.isNotBlank() },
                    role = role,
                    isActive = isActive
                )
                val response = repository.updateUser(id, request)
                if (response.isSuccessful) {
                    _state.value = StaffState.SuccessAction("Cập nhật thành công")
                    fetchUsers()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = extractErrorMessage(errorBody, "Lỗi cập nhật: ${response.code()}")
                    _state.value = StaffState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = StaffState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun deleteUser(id: Long) {
        viewModelScope.launch {
            _state.value = StaffState.Loading
            try {
                val response = repository.deleteUser(id)
                if (response.isSuccessful) {
                    _state.value = StaffState.SuccessAction(response.body() ?: "Xóa thành công")
                    fetchUsers()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMsg = extractErrorMessage(errorBody, "Lỗi xóa: ${response.code()}")
                    _state.value = StaffState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _state.value = StaffState.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    private fun extractErrorMessage(errorBody: String?, defaultMsg: String): String {
        return try {
            errorBody?.let {
                // Parse JSON error message nếu có
                if (it.contains("message")) {
                    val start = it.indexOf("\"message\":\"") + 11
                    val end = it.indexOf("\"", start)
                    if (start > 10 && end > start) {
                        it.substring(start, end)
                    } else {
                        defaultMsg
                    }
                } else {
                    defaultMsg
                }
            } ?: defaultMsg
        } catch (e: Exception) {
            defaultMsg
        }
    }

    fun searchUsers(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun setFilter(filter: StaffFilter) {
        _selectedFilter.value = filter
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = _users.value.filter { user ->
            val matchesFilter = when (_selectedFilter.value) {
                StaffFilter.ALL -> true
                StaffFilter.ACTIVE -> user.isActive
                StaffFilter.INACTIVE -> !user.isActive
            }

            val matchesSearch = if (_searchQuery.value.isEmpty()) {
                true
            } else {
                user.fullname.contains(_searchQuery.value, ignoreCase = true) ||
                        user.username.contains(_searchQuery.value, ignoreCase = true) ||
                        user.userId.toString().contains(_searchQuery.value)
            }

            matchesFilter && matchesSearch
        }
        _filteredUsers.value = filtered
    }

    fun getRoleDisplay(role: String): String {
        return when (role) {
            "ADMIN" -> "Quản trị viên"
            "STAFF" -> "Nhân viên"
            else -> role
        }
    }
}