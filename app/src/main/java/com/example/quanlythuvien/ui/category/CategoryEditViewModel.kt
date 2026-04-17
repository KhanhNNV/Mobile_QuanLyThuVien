package com.example.quanlythuvien.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.CategoryRequest
import com.example.quanlythuvien.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryEditViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _editState = MutableStateFlow<CategoryEditState>(CategoryEditState.Idle)
    val editState: StateFlow<CategoryEditState> = _editState

    fun updateCategory(id: Long, request: CategoryRequest) {
        viewModelScope.launch {
            _editState.value = CategoryEditState.Loading
            try {
                val response = repository.updateCategory(id, request)
                if (response.isSuccessful) _editState.value = CategoryEditState.UpdateSuccess
                else _editState.value = CategoryEditState.Error("Lỗi cập nhật")
            } catch (e: Exception) {
                _editState.value = CategoryEditState.Error(e.message ?: "Mất kết nối")
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            _editState.value = CategoryEditState.Loading
            try {
                val response = repository.deleteCategory(id)
                if (response.isSuccessful) {
                    _editState.value = CategoryEditState.DeleteSuccess
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Không thể xóa thể loại này!"
                    _editState.value = CategoryEditState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _editState.value = CategoryEditState.Error("Mất kết nối: ${e.message}")
            }
        }
    }
}