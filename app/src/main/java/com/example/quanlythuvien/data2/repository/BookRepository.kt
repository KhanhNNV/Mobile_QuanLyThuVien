package com.example.quanlythuvien.data2.repository

import androidx.lifecycle.LiveData
import com.example.quanlythuvien.data2.dao.BookDao
import com.example.quanlythuvien.data2.entity.Book

class BookRepository(private val bookDao: BookDao) {


    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()



    suspend fun insert(book: Book): Long {
        return bookDao.insert(book)
    }

    suspend fun update(book: Book) {
        bookDao.update(book)
    }

    suspend fun delete(book: Book) {
        bookDao.delete(book)
    }

    suspend fun getBookById(id: Long): Book? {
        return bookDao.getBookById(id)
    }

    suspend fun getBookByIsbn(isbn: String): Book? {
        return bookDao.getBookByIsbn(isbn)
    }



    fun getBooksByCategory(categoryId: Int): LiveData<List<Book>> {
        return bookDao.getBooksByCategory(categoryId)
    }

    fun searchBooks(keyword: String): LiveData<List<Book>> {
        return bookDao.searchBooks(keyword)
    }
}