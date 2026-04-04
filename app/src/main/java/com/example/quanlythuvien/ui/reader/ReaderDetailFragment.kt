package com.example.quanlythuvien.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.utils.setupCustomHeader
import com.google.android.material.tabs.TabLayout
import java.security.KeyStore

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {
    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<MockReaderBook> = listOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCustomHeader(
            view = view,
            title = "Thông tin độc giả",
            subtitle = "Chi tiết",
            showBack = true,
            showEdit = true,
            onEditClick = {
                Toast.makeText(requireContext(), "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
            }
        )

        //Lấy thông tin người dùng từ bundle
        var readerName = arguments?.getString("readerName")
        var readerPhone = arguments?.getString("readerPhone")
        var readerType = arguments?.getString("readerType")

        //Gắn thông tin người dùng vào giao diện  View
        view.findViewById<TextView>(R.id.tvReaderName)?.text = readerName
        view.findViewById<TextView>(R.id.tvReaderInfo)?.text = readerPhone
        view.findViewById<TextView>(R.id.tvReaderStatus)?.text = readerType
        view.findViewById<TextView>(R.id.tvAvatar)?.text = readerName?.firstOrNull()?.uppercase()

        //Khởi tọa data MOCK cho book
        setupMockData()

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)
        bookAdapter = ReaderDetailAdapter()
        rvBooks?.layoutManager = LinearLayoutManager(requireContext())
        rvBooks?.adapter = bookAdapter


        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterBooks(isReturned = false)
                    1 -> filterBooks(isReturned = true)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        view.post {
            filterBooks(isReturned = false)
        }
    }

    private fun setupMockData() {
        allDataMockReaderBook = listOf(
            MockReaderBook("Lập trình Java căn bản", "Trần Văn B", "978-111", "01/10/2025", "15/10/2025", isOverdue = true, isReturned = false),
            MockReaderBook("Kotlin Coroutines", "JetBrains", "978-222", "10/10/2025", "24/10/2025", isOverdue = false, isReturned = false),
            MockReaderBook("Cấu trúc dữ liệu & Giải thuật", "Nguyễn C", "978-333", "01/09/2025", "15/09/2025", isOverdue = false, isReturned = true),
            MockReaderBook("Clean Code", "Robert C. Martin", "978-444", "15/08/2025", "30/08/2025", isOverdue = false, isReturned = true)
        )
    }

    private fun filterBooks(isReturned: Boolean) {
        val filteredList = allDataMockReaderBook.filter { it.isReturned == isReturned }
        bookAdapter.submitList(filteredList)
    }
}


