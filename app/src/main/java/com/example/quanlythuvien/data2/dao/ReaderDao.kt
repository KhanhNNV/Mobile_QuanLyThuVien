package com.example.quanlythuvien.data2.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.quanlythuvien.data2.entity.Reader
import com.example.quanlythuvien.data2.entity.enums.ReaderType

@Dao
interface ReaderDao {

    //Thêm reader
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reader: Reader): Long

    //Cập nhật
    @Update
    suspend fun update(reader: Reader)

    //Xóa
    @Delete
    suspend fun delete(reader: Reader)

    //Lấy tất cả reader
    @Query("SELECT * FROM readers")
    fun getAllReaders(): LiveData<List<Reader>>

    //Lấy theo ID
    @Query("SELECT * FROM readers WHERE reader_id = :id")
    suspend fun getReaderById(id: Int): Reader?

    //Tìm theo tên
    @Query("SELECT * FROM readers WHERE name LIKE '%' || :keyword || '%'")
    fun searchReaders(keyword: String): LiveData<List<Reader>>

    //Tìm theo số điện thoại
    @Query("SELECT * FROM readers WHERE phone_number = :phone LIMIT 1")
    suspend fun getReaderByPhone(phone: String): Reader?

    //Lọc theo loại độc giả (enum)
    @Query("SELECT * FROM readers WHERE reader_type = :type")
    fun getReadersByType(type: ReaderType): LiveData<List<Reader>>

    //Lấy danh sách đã hết hạn
    @Query("""
        SELECT * FROM readers 
        WHERE expiration_date IS NOT NULL 
        AND expiration_date < :currentTime
    """)
    fun getExpiredReaders(currentTime: Long): LiveData<List<Reader>>

    //Gia hạn thẻ
    @Query("""
        UPDATE readers 
        SET expiration_date = :newDate 
        WHERE reader_id = :readerId
    """)
    suspend fun updateExpiration(readerId: Int, newDate: Long)
}