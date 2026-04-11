package com.example.quanlythuvien.data2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.quanlythuvien.data2.converter.Converters
import com.example.quanlythuvien.data2.dao.BookDao
import com.example.quanlythuvien.data2.dao.CategoryDao
import com.example.quanlythuvien.data2.dao.LibraryDao
import com.example.quanlythuvien.data2.dao.ReaderDao
import com.example.quanlythuvien.data2.entity.Book
import com.example.quanlythuvien.data2.entity.Category
import com.example.quanlythuvien.data2.entity.FeeNotice
import com.example.quanlythuvien.data2.entity.Loan
import com.example.quanlythuvien.data2.entity.LoanDetail
import com.example.quanlythuvien.data2.entity.Notification
import com.example.quanlythuvien.data2.entity.Reader

@Database(
    entities = [
        Category::class,
        Book::class,
        Reader::class,
        Loan::class,
        LoanDetail::class,
        FeeNotice::class,
        Notification::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun readerDao(): ReaderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "library_database"
                )
                    // TODO: Thay bằng Migration thực tế khi lên production
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}