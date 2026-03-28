package com.example.quanlythuvien.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.quanlythuvien.data.AppDatabase
import com.example.quanlythuvien.data.entity.Reader
import com.example.quanlythuvien.data.entity.enums.ReaderType
import com.example.quanlythuvien.data.repository.ReaderRepository
import kotlinx.coroutines.launch

class ReaderListViewModel(application: Application): AndroidViewModel(application) {
    private val repository: ReaderRepository
    val allReaders: LiveData<List<Reader>>

    init {
        val dao = AppDatabase.getDatabase(application).readerDao()
        repository = ReaderRepository(dao)
        allReaders = repository.allReaders
    }
    fun insertMockData() {
        viewModelScope.launch {
            val reader1 = Reader(
                name = "Nguyễn Văn A",
                phoneNumber = "0901234567",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 30) // 30 ngày
            )
            val reader2 = Reader(
                name = "Trần Thị B",
                phoneNumber = "0987654321",
                readerType = ReaderType.GUEST,
                expirationDate = System.currentTimeMillis() + (86400000L * 7) // 7 ngày
            )
            val reader3 = Reader(
                name = "Lê Văn C",
                phoneNumber = "0911222333",
                readerType = ReaderType.STUDENT,
                expirationDate = null
            )
            val reader4 = Reader(
                name = "Phạm Thị D",
                phoneNumber = "0922333444",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 60) // 60 ngày
            )
            val reader5 = Reader(
                name = "Hoàng Văn E",
                phoneNumber = "0933444555",
                readerType = ReaderType.GUEST,
                expirationDate = System.currentTimeMillis() + (86400000L * 14) // 14 ngày
            )
            val reader6 = Reader(
                name = "Đặng Thị F",
                phoneNumber = "0944555666",
                readerType = ReaderType.STUDENT,
                expirationDate = null // Không có thời hạn
            )
            val reader7 = Reader(
                name = "Bùi Văn G",
                phoneNumber = "0955666777",
                readerType = ReaderType.GUEST,
                expirationDate = System.currentTimeMillis() + (86400000L * 3) // 3 ngày
            )
            val reader8 = Reader(
                name = "Vũ Thị H",
                phoneNumber = "0966777888",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 120) // 120 ngày
            )
            val reader9 = Reader(
                name = "Ngô Văn I",
                phoneNumber = "0977888999",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 90) // 90 ngày
            )
            val reader10 = Reader(
                name = "Đỗ Thị K",
                phoneNumber = "0988999000",
                readerType = ReaderType.GUEST,
                expirationDate = null
            )
            val reader11 = Reader(
                name = "Lý Văn L",
                phoneNumber = "0999000111",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 15) // 15 ngày
            )
            val reader12 = Reader(
                name = "Hồ Thị M",
                phoneNumber = "0811222333",
                readerType = ReaderType.GUEST,
                expirationDate = System.currentTimeMillis() + (86400000L * 1) // 1 ngày
            )
            val reader13 = Reader(
                name = "Đinh Văn N",
                phoneNumber = "0822333444",
                readerType = ReaderType.STUDENT,
                expirationDate = System.currentTimeMillis() + (86400000L * 30) // 30 ngày
            )


            repository.insert(reader4)
            repository.insert(reader5)
            repository.insert(reader6)
            repository.insert(reader7)
            repository.insert(reader8)
            repository.insert(reader9)
            repository.insert(reader10)
            repository.insert(reader11)
            repository.insert(reader12)
            repository.insert(reader13)

            repository.insert(reader1)
            repository.insert(reader2)
            repository.insert(reader3)
        }
    }
}