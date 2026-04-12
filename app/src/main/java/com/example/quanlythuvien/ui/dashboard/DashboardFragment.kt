package com.example.quanlythuvien.ui.dashboard

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.media.session.MediaSession
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.ui.welcome.category.CreateCategoryViewModel
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupCustomHeader
import com.example.quanlythuvien.viewmodel.SharedFilterLoanViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val sharedViewModel: SharedFilterLoanViewModel by activityViewModels()

    // Khai báo các Views
    private lateinit var cvTotalBooks: MaterialCardView
    private lateinit var cvTotalBorrowing: MaterialCardView
    private lateinit var cvTotalReader: MaterialCardView
    private lateinit var cvTotalDelayed: MaterialCardView
    private lateinit var tvTotalBookQuantity: TextView
    private lateinit var viewModel: DashboardViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Header
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi")).format(Date())
        setupCustomHeader(
            view = view,
            title = "Trang chủ",
            subtitle = currentDate
        )


        initViews(view)
        setupViewModel()
        observeViewModel()
        handleEvents()
        loadCountAllBooks()


    }

    private fun initViews(view: View) {
        cvTotalBooks = view.findViewById(R.id.cvTotalBooks)
        cvTotalBorrowing = view.findViewById(R.id.cvTotalBorrowing)
        cvTotalReader=view.findViewById(R.id.cvTotalReader)
        cvTotalDelayed=view.findViewById(R.id.cvTotalDelayed)
        tvTotalBookQuantity=view.findViewById(R.id.tvTotalBookQuantity)
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(BookApiService::class.java)
        val repository = BookRepository(apiService)

        val factory = GenericViewModelFactory { DashboardViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Lắng nghe State
                viewModel.bookCountState.collectLatest { state ->
                    when (state) {
                        is DashboardBookCountState.Idle -> {
                            Log.d("DASHBOARD","Tổng sách + ${tvTotalBookQuantity.text}")
                        }
                        is DashboardBookCountState.Loading -> {
                            // Đang gọi API
                            tvTotalBookQuantity.text = "..."
                            Log.d("DASHBOARD","Tổng sách + ${tvTotalBookQuantity.text}")
                        }
                        is DashboardBookCountState.Success -> {
                            tvTotalBookQuantity.text = state.totalBooks.toString()
                            Log.d("DASHBOARD","Tổng sách + ${tvTotalBookQuantity.text}")
                        }
                        is DashboardBookCountState.Error -> {
                            tvTotalBookQuantity.text = "0"
                            Log.d("DASHBOARD","Tổng sách lỗi + ${tvTotalBookQuantity.text}")
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }

    private fun handleEvents() {


        cvTotalBooks.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_bookList)
        }

        cvTotalReader.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_readerList)
        }

        // Sử dụng SharedViewModel để truyền tín hiệu thay vì dùng Bundle.
        // Tránh lỗi kẹt bộ lọc do app:restoreState="true" tự động khôi phục UI cũ và ghi đè Bundle mới.
        // Lưu ý: Đích đến (BorrowPay) phải gọi clearFilter() ngay sau khi nhận được lệnh.
        cvTotalBorrowing.setOnClickListener {
            sharedViewModel.setFilter("BORROWING") // Cập nhật ViewModel
            findNavController().navigate(R.id.action_dashboard_to_borrowPay)
        }

        cvTotalDelayed.setOnClickListener {
            sharedViewModel.setFilter("OVERDUE") // Cập nhật ViewModel
            findNavController().navigate(R.id.action_dashboard_to_borrowPay)
        }

    }

    private fun loadCountAllBooks(){
        val libraryId = TokenManager(requireContext()).getLibraryId()
        Log.d("DASHBOARD","Đang gọi ${libraryId}")
        if (libraryId != null && libraryId != -1L) {
            viewModel.loadTotalBooks(libraryId)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không lấy được ID thư viện", Toast.LENGTH_SHORT).show()
        }
    }
}