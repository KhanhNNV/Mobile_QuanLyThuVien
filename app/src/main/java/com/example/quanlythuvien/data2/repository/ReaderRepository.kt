package com.example.quanlythuvien.data2.repository

import androidx.lifecycle.LiveData
import com.example.quanlythuvien.data2.dao.ReaderDao
import com.example.quanlythuvien.data2.entity.Reader
import com.example.quanlythuvien.data2.entity.enums.ReaderType

class ReaderRepository(private val readerDao: ReaderDao) {


    val allReaders: LiveData<List<Reader>> = readerDao.getAllReaders()



    suspend fun insert(reader: Reader): Long {
        return readerDao.insert(reader)
    }

    suspend fun update(reader: Reader) {
        readerDao.update(reader)
    }

    suspend fun delete(reader: Reader) {
        readerDao.delete(reader)
    }

    suspend fun getReaderById(id: Int): Reader? {
        return readerDao.getReaderById(id)
    }

    suspend fun getReaderByPhone(phone: String): Reader? {
        return readerDao.getReaderByPhone(phone)
    }

    suspend fun updateExpiration(readerId: Int, newDate: Long) {
        readerDao.updateExpiration(readerId, newDate)
    }


    fun searchReaders(keyword: String): LiveData<List<Reader>> {
        return readerDao.searchReaders(keyword)
    }

    fun getReadersByType(type: ReaderType): LiveData<List<Reader>> {
        return readerDao.getReadersByType(type)
    }

    fun getExpiredReaders(currentTime: Long): LiveData<List<Reader>> {
        return readerDao.getExpiredReaders(currentTime)
    }
}