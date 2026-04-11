package com.example.quanlythuvien.data2.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlythuvien.data2.entity.Book

@Dao
interface BookDao {

    //Thêm sách
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: Book): Long

    //Cập nhật
    @Update
    suspend fun update(book: Book)

    //Xóa sách
    @Delete
    suspend fun delete(book: Book)

    //Lấy tất cả sách
    @Query("SELECT * FROM books")
    fun getAllBooks(): LiveData<List<Book>>

    //Lấy sách theo id
    @Query("SELECT * FROM books WHERE book_id = :id")
    suspend fun getBookById(id: Long): Book?

    //Lấy sách theo cateId
    @Query("SELECT * FROM books WHERE category_id = :categoryId")
    fun getBooksByCategory(categoryId: Int): LiveData<List<Book>>

    //Tìm kiếm theo tên
    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%'")
    fun searchBooks(keyword: String): LiveData<List<Book>>

    //Kiểm tra ISBN
    @Query("SELECT * FROM books WHERE isbn_code = :isbn LIMIT 1")
    suspend fun getBookByIsbn(isbn: String): Book?

}