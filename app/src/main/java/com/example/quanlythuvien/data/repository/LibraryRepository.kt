package com.example.quanlythuvien.data.repository

import com.example.quanlythuvien.data.dao.LibraryDao
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category

class LibraryRepository(private val libraryDao: LibraryDao) {
    suspend fun insertCategory(category: Category): Long {
        return libraryDao.insertCategory(category)
    }

    suspend fun insertBook(book: Book): Long {
        return libraryDao.insertBook(book)
    }
}