package com.example.quanlythuvien.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category

@Dao
interface LibraryDao {
    // Trả về Long chính là ID của Category vừa được tạo
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Query("SELECT * FROM categories LIMIT 1")
    suspend fun getFirstCategory(): Category?

    @Query("SELECT * FROM books LIMIT 1")
    suspend fun getFirstBook(): Book?

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>
}