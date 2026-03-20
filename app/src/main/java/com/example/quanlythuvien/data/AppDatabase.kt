package com.example.quanlythuvien.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quanlythuvien.data.dao.LibraryDao
import com.example.quanlythuvien.data.entity.Book
import com.example.quanlythuvien.data.entity.Category
import com.example.quanlythuvien.data.entity.FeeNotice
import com.example.quanlythuvien.data.entity.Loan
import com.example.quanlythuvien.data.entity.LoanDetail
import com.example.quanlythuvien.data.entity.Notification

@Database(
    entities = [
        Category::class,
        Book::class,
        Loan::class,
        LoanDetail::class,
        FeeNotice::class,
        Notification::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

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