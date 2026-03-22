package com.example.quanlythuvien.ui.welcome

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.AppDatabase
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category
import com.example.quanlythuvien.data.repository.LibraryRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LibraryRepository

    // Lưu tạm categoryId sau khi tạo để truyền cho sách
    var currentCategoryId: Int = -1

    init {
        val dao = AppDatabase.getDatabase(application).libraryDao()
        repository = LibraryRepository(dao)
    }

    fun saveCategory(name: String, desc: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val category = Category(name = name, description = desc)
            // Insert và lấy lại ID
            val insertedId = repository.insertCategory(category)
            currentCategoryId = insertedId.toInt()
            onSaved() // Báo cho UI biết đã lưu xong để chuyển trang
        }
    }

    fun saveBook(title: String, author: String, quantity: Int,isbnCode: String, onSaved: () -> Unit) {
        viewModelScope.launch {
            val book = Book(
                categoryId = currentCategoryId, // Lấy ID đã lưu từ bước trước
                title = title,
                author = author,
                totalQuantity = quantity,
                availableQuantity = quantity,
                isbnCode = isbnCode
            )
            repository.insertBook(book)
            onSaved() // Báo cho UI biết để nhảy vào Dashboard
        }
    }
}