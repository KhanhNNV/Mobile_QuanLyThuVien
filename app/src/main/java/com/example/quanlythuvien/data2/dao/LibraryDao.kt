package com.example.quanlythuvien.data2.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quanlythuvien.data2.entity.Book
import com.example.quanlythuvien.data2.entity.Category

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

    //    Dùng LiveData ở đây thì Room sẽ tự động chạy trên luồng nền và tự động cập nhật UI mỗi khi cơ sở dữ liệu có sự thay đổi
    @Query("SELECT * FROM books")
    fun getAllBooks(): LiveData<List<Book>>
}