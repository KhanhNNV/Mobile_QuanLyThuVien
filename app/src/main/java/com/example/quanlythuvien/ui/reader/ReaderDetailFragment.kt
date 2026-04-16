package com.example.quanlythuvien.ui.reader

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.ReaderRepository
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.viewmodel.LoanSharedViewModel
import com.google.android.material.tabs.TabLayout

class ReaderDetailFragment : Fragment(R.layout.fragment_reader_detail) {

    private lateinit var bookAdapter: ReaderDetailAdapter
    private var allDataMockReaderBook: List<MockReaderBook> = emptyList()

    private val loanSharedViewModel: LoanSharedViewModel by activityViewModels()

    private val detailViewModel: ReaderDetailViewModel by activityViewModels {
        GenericViewModelFactory {
            val app = requireContext().applicationContext as Application
            val apiService = RetrofitClient.getInstance(app).create(ReaderApiService::class.java)
            ReaderDetailViewModel(ReaderRepository(apiService))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val readerId = arguments?.getInt("readerId", -1)?.toLong() ?: -1L
        val readerName = arguments?.getString("readerName").orEmpty()
        val readerPhone = arguments?.getString("readerPhone").orEmpty()
        val readerType = arguments?.getString("readerType").orEmpty()

        bindHeader(view, readerName, readerPhone, readerType, readerId)
        setupRecycler(view, readerName)
        setupTabs(view)
        observeViewModel(view)
    }

    private fun bindHeader(
        view: View,
        readerName: String,
        readerPhone: String,
        readerType: String,
        readerId: Long
    ) {
        view.findViewById<TextView>(R.id.tvReaderName).text = readerName
        view.findViewById<TextView>(R.id.tvReaderInfo).text = readerPhone
        view.findViewById<TextView>(R.id.tvReaderStatus).text = readerType
        view.findViewById<TextView>(R.id.tvAvatar).text = readerName.firstOrNull()?.uppercase() ?: ""
        view.findViewById<TextView>(R.id.tvHeaderTitle).text = "Chi tiết độc giả"

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<View>(R.id.ivMoreOption).setOnClickListener { anchor ->
            showOptionMenu(
                anchorView = anchor,
                readerId = readerId,
                readerName = readerName,
                readerPhone = readerPhone,
                readerType = readerType,
                role = getCurrentRole()
            )
        }
    }

    private fun setupRecycler(view: View, readerName: String) {
        setupMockData()

        val rvBooks = view.findViewById<RecyclerView>(R.id.rvReaderBooks)
        bookAdapter = ReaderDetailAdapter { selectedBook ->
            val mockLoan = LoanItemData(
                loanId = 12345,
                readerName = readerName.ifBlank { "Độc giả" },
                borrowDate = selectedBook.borrowDate,
                overallStatus = if (selectedBook.isReturned) "RETURNED" else "BORROWING",
                borrowedBooks = mutableListOf()
            )
            loanSharedViewModel.selectedLoanToView.value = mockLoan
            findNavController().navigate(R.id.loanFragment)
        }

        rvBooks.layoutManager = LinearLayoutManager(requireContext())
        rvBooks.adapter = bookAdapter
    }

    private fun setupTabs(view: View) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterBooks(0) // đang mượn
                    1 -> filterBooks(1) // đã trả
                    2 -> filterBooks(2) // quá hạn
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        view.post { filterBooks(0) }
    }

    private fun showOptionMenu(
        anchorView: View,
        readerId: Long,
        readerName: String,
        readerPhone: String,
        readerType: String,
        role: String
    ) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_reader_options, popup.menu)

        // STAFF: ẩn xóa
        if (role == "STAFF") {
            popup.menu.findItem(R.id.menuDeleteReader)?.isVisible = false
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuEditReader -> {
                    val bundle = Bundle().apply {
                        putString("readerName", readerName)
                        putString("readerPhone", readerPhone)
                        putString("readerType", readerType)
                    }
                    findNavController().navigate(R.id.actionReaderDetailToReaderAdd, bundle)
                    true
                }

                R.id.menuDeleteReader -> {
                    showDeleteConfirmationDialog(readerId)
                    true
                }

                else -> false
            }
        }

        popup.setForceShowIcon(true)
        popup.show()
    }

    private fun showDeleteConfirmationDialog(readerId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa Độc Giả")
            .setMessage("Bạn có chắc chắn muốn xóa độc giả này?")
            .setPositiveButton("Có") { dialog, _ ->
                if (readerId <= 0L) {
                    Toast.makeText(requireContext(), "Không tìm thấy mã độc giả", Toast.LENGTH_SHORT).show()
                } else {
                    detailViewModel.deleteReader(readerId)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun observeViewModel(rootView: View) {
        detailViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            rootView.findViewById<View>(R.id.ivMoreOption).isEnabled = !loading
        }

        detailViewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        detailViewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Đã xóa độc giả", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun getCurrentRole(): String {
        val rawRole = TokenManager(requireContext()).getRole().orEmpty()
        return when {
            rawRole.contains("ADMIN", ignoreCase = true) -> "ADMIN"
            rawRole.contains("STAFF", ignoreCase = true) -> "STAFF"
            else -> ""
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

    private fun filterBooks(status: Int) {
        val filtered = when (status) {
            0 -> allDataMockReaderBook.filter { !it.isReturned && !it.isOverdue }
            1 -> allDataMockReaderBook.filter { it.isReturned }
            2 -> allDataMockReaderBook.filter { !it.isReturned && it.isOverdue }
            else -> allDataMockReaderBook
        }
        bookAdapter.submitList(filtered)
    }
}