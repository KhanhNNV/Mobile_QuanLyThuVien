package com.example.quanlythuvien.ui.books

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.quanlythuvien.data.AppDatabase
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.repository.LibraryRepository

class BookListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LibraryRepository
    val allBooks: LiveData<List<Book>>

    init {
        val dao = AppDatabase.getDatabase(application).libraryDao()
        repository = LibraryRepository(dao)
        allBooks = repository.allBooks
    }
}