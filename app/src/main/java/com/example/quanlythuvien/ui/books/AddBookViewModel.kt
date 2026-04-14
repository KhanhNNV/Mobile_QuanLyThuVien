package com.example.quanlythuvien.ui.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddBookViewModel(private val repository: BookRepository) : ViewModel(){
    private val _addBookState = MutableStateFlow<AddBookState>(AddBookState.Idle)
    val addBookState : StateFlow<AddBookState> = _addBookState
    fun addBook(request: BookRequest){
        viewModelScope.launch{
            _addBookState.value = AddBookState.Loading
            try {
                val response = repository.createBook(request)
                if (response.isSuccessful && response.body() != null){
                    _addBookState.value = AddBookState.Success(response.body()!!)
                }else{
                    _addBookState.value = AddBookState.Error("Lỗi: ${response.code()}")
                }
            }catch (e: Exception) {
                _addBookState.value = AddBookState.Error("Mất kết nối: ${e.message}")
            }
        }
    }
}