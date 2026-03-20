package com.example.quanlythuvien.data.repository

import androidx.lifecycle.LiveData
import com.example.quanlythuvien.data.dao.CategoryDao
import com.example.quanlythuvien.data.entity.Category

class CategoryRepository(private val categoryDao: CategoryDao) {


    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()



    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }



    fun searchCategories(keyword: String): LiveData<List<Category>> {
        return categoryDao.searchCategories(keyword)
    }

    fun getCategoriesSortedByName(): LiveData<List<Category>> {
        return categoryDao.getCategoriesSortedByName()
    }
}