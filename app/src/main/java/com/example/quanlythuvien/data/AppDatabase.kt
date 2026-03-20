package com.example.quanlythuvien.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quanlythuvien.data.converter.Converters
import com.example.quanlythuvien.data.dao.LibraryDao
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category
import com.example.quanlythuvien.data.entity.Reader
import com.example.quanlythuvien.data.dao.BookDao
import com.example.quanlythuvien.data.dao.CategoryDao
import com.example.quanlythuvien.data.dao.ReaderDao

@Database(entities = [Category::class, Book::class, Reader::class],
    version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun bookDao():BookDao
    abstract fun categoryDao():CategoryDao
    abstract fun readerDao():ReaderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "library_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}