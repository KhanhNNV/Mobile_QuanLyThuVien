package com.example.quanlythuvien.data2.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlythuvien.data2.entity.Category

@Dao
interface CategoryDao {

    // Thêm category
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: Category): Long

    //Cập nhật
    @Update
    suspend fun update(category: Category)

    //Xóa
    @Delete
    suspend fun delete(category: Category)

    //Lấy tất cả category
    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<Category>>

    //Lấy theo ID
    @Query("SELECT * FROM categories WHERE category_id = :id")
    suspend fun getCategoryById(id: Int): Category?

    //Tìm theo tên
    @Query("SELECT * FROM categories WHERE name LIKE '%' || :keyword || '%'")
    fun searchCategories(keyword: String): LiveData<List<Category>>

    //Sắp xếp theo tên
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getCategoriesSortedByName(): LiveData<List<Category>>
}