package com.example.quanlythuvien.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnResetTest = view.findViewById<Button>(R.id.btnResetTest)
        val tvCategoryInfo = view.findViewById<TextView>(R.id.tvCategoryInfo)
        val tvBookInfo = view.findViewById<TextView>(R.id.tvBookInfo)

        // 1. Gọi Database để lấy danh sách (Chạy ngầm bằng Coroutine)
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            // Chú ý: Gọi hàm get ALL nhé
            val categories = db.libraryDao().getAllCategories()
            val books = db.libraryDao().getAllBooks()

            // 2. Chuyển về luồng chính (Main Thread) để hiển thị lên UI
            withContext(Dispatchers.Main) {
                // Xử lý hiển thị danh sách Thể loại
                if (categories.isNotEmpty()) {
                    // Nối các thể loại thành 1 chuỗi, cách nhau bởi 2 dấu xuống dòng
                    tvCategoryInfo.text = categories.joinToString(separator = "\n\n") {
                        "🏷️ [ID: ${it.categoryId}] ${it.name}\n📝 Mô tả: ${it.description}"
                    }
                } else {
                    tvCategoryInfo.text = "Chưa có thể loại nào"
                }

                // Xử lý hiển thị danh sách Sách
                if (books.isNotEmpty()) {
                    // Nối các sách thành 1 chuỗi
                    tvBookInfo.text = books.joinToString(separator = "\n\n") {
                        "📚 [ID: ${it.bookId}] ${it.title} (CateID: ${it.categoryId})\n✍️ Tác giả: ${it.author}\n📦 Số lượng: ${it.totalQuantity}"
                    }
                } else {
                    tvBookInfo.text = "Chưa có sách nào"
                }
            }
        }

        // Sự kiện nút Reset
        btnResetTest.setOnClickListener {
            val prefs = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("IS_FIRST_LAUNCH", true).apply()

            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true)
                .build()
            findNavController().navigate(R.id.welcomeFragment, null, navOptions)
        }
    }
}