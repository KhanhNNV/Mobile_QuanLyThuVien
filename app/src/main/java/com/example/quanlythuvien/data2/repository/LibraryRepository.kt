package com.example.quanlythuvien.data2.repository

import androidx.lifecycle.LiveData
import com.example.quanlythuvien.data2.dao.LibraryDao
import com.example.quanlythuvien.data2.entity.Book
import com.example.quanlythuvien.data2.entity.Category

class LibraryRepository(private val libraryDao: LibraryDao) {
    suspend fun insertCategory(category: Category): Long {
        return libraryDao.insertCategory(category)
    }

    suspend fun insertBook(book: Book): Long {
        return libraryDao.insertBook(book)
    }

    val allBooks: LiveData<List<Book>> = libraryDao.getAllBooks()
}