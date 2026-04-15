package com.example.quanlythuvien.ui.books.addBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.model.request.BookRequest
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.ui.books.addBook.AddBookState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddBookViewModel(private val repository: BookRepository ,
                       private val categoryRepository: CategoryRepository
) : ViewModel(){
    private val _addBookState = MutableStateFlow<AddBookState>(AddBookState.Idle)
    val addBookState : StateFlow<AddBookState> = _addBookState

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = categoryRepository.getCategoriesByLibrary()
                if (response.isSuccessful && response.body() != null) {
                    _categories.value = response.body()!!
                }
            } catch (e: Exception) {
                // Có thể log lỗi ở đây nếu không lấy được thể loại
            }
        }
    }

    fun addBook(request: BookRequest){
        viewModelScope.launch {
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