package com.example.quanlythuvien.ui.dashboard

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.BookApiService
import com.example.quanlythuvien.data.remote.LoanApiService
import com.example.quanlythuvien.data.remote.LoanDetailApiService
import com.example.quanlythuvien.data.remote.ReaderApiService
import com.example.quanlythuvien.data.repository.BookRepository
import com.example.quanlythuvien.data.repository.LoanDetailRepository
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.data.repository.ReaderRepository
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
    private lateinit var tvTotalLoanBorrowing: TextView
    private lateinit var tvTotalLoanDelayed: TextView
    private lateinit var tvTotalReader: TextView
    private lateinit var viewModel: DashboardViewModel
    private lateinit var rvAlerts: RecyclerView


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
        tvTotalBookQuantity = view.findViewById(R.id.tvTotalBookQuantity)
        tvTotalLoanBorrowing = view.findViewById(R.id.tvTotalLoanBorrowing)
        tvTotalLoanDelayed = view.findViewById(R.id.tvTotalLoanDelayed)
        tvTotalReader = view.findViewById(R.id.tvTotalReader)

        rvAlerts = view.findViewById(R.id.rvAlerts)
        rvAlerts.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        // Khởi tạo 3 API Service
        val bookApi = retrofit.create(BookApiService::class.java)
        val loanApi = retrofit.create(LoanApiService::class.java)
        val readerApi = retrofit.create(ReaderApiService::class.java)
        val loanDetailApi= retrofit.create(LoanDetailApiService::class.java)

        // Khởi tạo 3 Repository
        val bookRepo = BookRepository(bookApi)
        val loanRepo = LoanRepository(loanApi)
        val readerRepo = ReaderRepository(readerApi)
        val loanDetailRepo= LoanDetailRepository(loanDetailApi)

        val factory = GenericViewModelFactory {
            DashboardViewModel(bookRepo, loanRepo, readerRepo,loanDetailRepo)
        }
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]

    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe Sách
                launch {
                    viewModel.bookCountState.collectLatest { state ->
                        when (state) {
                            is CountState.Loading -> tvTotalBookQuantity.text = "..."
                            is CountState.Success -> tvTotalBookQuantity.text = state.count.toString()
                            is CountState.Error -> tvTotalBookQuantity.text = "0"
                            else -> {}
                        }
                    }
                }

                // Observe Đang mượn
                launch {
                    viewModel.borrowingLoanState.collectLatest { state ->
                        when (state) {
                            is CountState.Loading -> tvTotalLoanBorrowing.text = "..."
                            is CountState.Success -> tvTotalLoanBorrowing.text = state.count.toString()
                            is CountState.Error -> tvTotalLoanBorrowing.text = "0"
                            else -> {}
                        }
                    }
                }

                // Observe Trễ hạn
                launch {
                    viewModel.overdueLoanState.collectLatest { state ->
                        when (state) {
                            is CountState.Loading -> tvTotalLoanDelayed.text = "..."
                            is CountState.Success -> tvTotalLoanDelayed.text = state.count.toString()
                            is CountState.Error -> tvTotalLoanDelayed.text = "0"
                            else -> {}
                        }
                    }
                }

                // Observe Độc giả
                launch {
                    viewModel.readerCountState.collectLatest { state ->
                        when (state) {
                            is CountState.Loading -> tvTotalReader.text = "..."
                            is CountState.Success -> tvTotalReader.text = state.count.toString()
                            is CountState.Error -> tvTotalReader.text = "0"
                            else -> {}
                        }
                    }
                }

                // Observe Cảnh báo
                launch {
                    viewModel.alertState.collectLatest { state ->
                        when (state) {
                            is AlertState.Loading -> {
                                rvAlerts.adapter = AlertAdapter(listOf("Đang tải..."))
                            }
                            is AlertState.Success -> {
                                rvAlerts.adapter = AlertAdapter(state.alerts)
                            }
                            is AlertState.Error -> {
                                rvAlerts.adapter = AlertAdapter(listOf(state.message))
                            }
                            else -> {}
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
        if (libraryId != null && libraryId != -1L) {
            viewModel.loadTotalBooks(libraryId)
            viewModel.loadBorrowingLoans(libraryId)
            viewModel.loadOverdueLoans(libraryId)
            viewModel.loadTotalReaders(libraryId)
            viewModel.loadAlerts(libraryId)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không lấy được ID thư viện", Toast.LENGTH_SHORT).show()
        }
    }
}