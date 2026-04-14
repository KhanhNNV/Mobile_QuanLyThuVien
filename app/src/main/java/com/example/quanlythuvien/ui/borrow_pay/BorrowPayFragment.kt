package com.example.quanlythuvien.ui.borrow_pay

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.LoanApiService
import com.example.quanlythuvien.data.repository.LoanRepository
import com.example.quanlythuvien.ui.borrow_pay.adapter.BorrowPayAdapter
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupCustomHeader
import com.example.quanlythuvien.viewmodel.SharedFilterLoanViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BorrowPayFragment : Fragment() {

    // --- VIEW MODELS ---
    private val sharedViewModel: SharedFilterLoanViewModel by activityViewModels()
    private lateinit var viewModel: BorrowPayViewModel // Thêm ViewModel gọi API

    // --- UI COMPONENTS ---
    private lateinit var fasAddLoan: FloatingActionButton
    private lateinit var autoSearch: AutoCompleteTextView
    private lateinit var btnToggleFilter: ImageView
    private lateinit var layoutFilterContainer: ConstraintLayout
    private lateinit var rgStatus: RadioGroup
    private lateinit var rbOption1: RadioButton
    private lateinit var rbOption2: RadioButton
    private lateinit var rbOption3: RadioButton
    private lateinit var edtFromDate: EditText
    private lateinit var edtToDate: EditText
    private lateinit var btnResetFilter: Button
    private lateinit var btnConfirmFilter: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar // Nên thêm 1 ProgressBar vào file XML để hiện khi tải dữ liệu

    // --- ADAPTER ---
    private lateinit var adapter: BorrowPayAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_borrow_pay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCustomHeader(view, "Mượn/Trả", "*Quản lý danh sách phiếu mượn")
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        setupSharedFilterObserver()

        // Gọi API nạp dữ liệu lần đầu tiên (Tất cả phiếu mượn)
        viewModel.fetchLoans()
    }

    // Mỗi khi màn hình này hiện lại (từ màn Detail quay về), tự động gọi API cập nhật dữ liệu mới nhất
    override fun onResume() {
        super.onResume()
        applyFilter()
    }

    private fun initViews(view: View) {
        fasAddLoan = view.findViewById(R.id.fasAddLoan)
        autoSearch = view.findViewById(R.id.autoSearch)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        layoutFilterContainer = view.findViewById(R.id.layoutFilterContainer)
        rgStatus = view.findViewById(R.id.rgStatus)
        rbOption1 = view.findViewById(R.id.rbOption1)
        rbOption2 = view.findViewById(R.id.rbOption2)
        rbOption3 = view.findViewById(R.id.rbOption3)
        edtFromDate = view.findViewById(R.id.edtFromDate)
        edtToDate = view.findViewById(R.id.edtToDate)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnConfirmFilter = view.findViewById(R.id.btnConfirmFilter)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Gợi ý: Hãy thêm thẻ <ProgressBar android:id="@+id/progressBar" ... /> vào file fragment_borrow_pay.xml
        // progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupViewModel() {
        // Khởi tạo Retrofit, Repository và ViewModel giống cách làm ở LoginFragment
        val apiService = RetrofitClient.getInstance(requireContext()).create(LoanApiService::class.java)
        val repository = LoanRepository(apiService)
        val factory = GenericViewModelFactory { BorrowPayViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[BorrowPayViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = BorrowPayAdapter { clickedItem ->
            // BƯỚC QUAN TRỌNG: Chỉ truyền ID sang màn hình Detail, không truyền cả Object nữa
            val bundle = Bundle().apply {
                putLong("EXTRA_LOAN_ID", clickedItem.loanId)
            }
            findNavController().navigate(R.id.action_borrowPay_to_loanDetail, bundle)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupListeners() {
        fasAddLoan.setOnClickListener { findNavController().navigate(R.id.loanAddFragment) }

        // Gọi API tìm kiếm mỗi khi người dùng dừng gõ phím
        autoSearch.addTextChangedListener { applyFilter() }

        btnToggleFilter.setOnClickListener {
            if (layoutFilterContainer.visibility == View.GONE) {
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            } else {
                layoutFilterContainer.visibility = View.GONE
                btnToggleFilter.backgroundTintList = null
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
            }
        }

        edtFromDate.setOnClickListener { showDatePickerForFilter(edtFromDate) }
        edtToDate.setOnClickListener { showDatePickerForFilter(edtToDate) }

        btnConfirmFilter.setOnClickListener { applyFilter() }

        btnResetFilter.setOnClickListener {
            edtFromDate.text.clear()
            edtToDate.text.clear()
            rgStatus.clearCheck()
            autoSearch.text.clear()
            applyFilter() // Tải lại toàn bộ dữ liệu
        }
    }

    // --- QUAN SÁT DỮ LIỆU TỪ API TRẢ VỀ ---
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loanListState.collectLatest { state ->
                when (state) {
                    is LoanListState.Idle -> { /* Không làm gì */ }
                    is LoanListState.Loading -> {
                        // progressBar.visibility = View.VISIBLE
                    }
                    is LoanListState.Success -> {
                        // progressBar.visibility = View.GONE
                        adapter.submitList(state.loans)
                        if (state.loans.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy phiếu phù hợp!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is LoanListState.Error -> {
                        // progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // --- HÀM GỌI API BỘ LỌC (Thay thế cho logic lọc nội bộ cũ) ---
    private fun applyFilter() {
        // 1. Lấy trạng thái
        val status = when (rgStatus.checkedRadioButtonId) {
            R.id.rbOption1 -> "OVERDUE"
            R.id.rbOption2 -> "BORROWING"
            R.id.rbOption3 -> "RETURNED"
            else -> null
        }

        // 2. Lấy ngày và chuyển đổi từ "dd/MM/yyyy" sang chuẩn "yyyy-MM-dd'T'00:00:00" cho Backend
        val fromDateIso = convertDateToIso(edtFromDate.text.toString().trim())
        val toDateIso = convertDateToIso(edtToDate.text.toString().trim())

        // 3. Lấy từ khóa tìm kiếm
        val search = autoSearch.text.toString().trim().takeIf { it.isNotEmpty() }

        // 4. Ra lệnh cho ViewModel gọi API
        viewModel.fetchLoans(status, fromDateIso, toDateIso, search)
    }

    // Hàm chuyển đổi định dạng ngày cho khớp với Backend Spring Boot
    private fun convertDateToIso(dateStr: String): String? {
        if (dateStr.isEmpty()) return null
        return try {
            val sdfIn = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfOut = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = sdfIn.parse(dateStr)
            if (date != null) sdfOut.format(date) else null
        } catch (e: Exception) {
            null
        }
    }


    //Hàm hiển thị DatePicker
    private fun showDatePickerForFilter(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            editText.setText(selectedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupSharedFilterObserver() {
        sharedViewModel.filterType.observe(viewLifecycleOwner) { type ->
            if (type != null) {
                layoutFilterContainer.visibility = View.VISIBLE
                btnToggleFilter.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.btn_primary)
                btnToggleFilter.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
                when (type) {
                    "OVERDUE" -> rgStatus.check(R.id.rbOption1)
                    "BORROWING" -> rgStatus.check(R.id.rbOption2)
                    "RETURNED" -> rgStatus.check(R.id.rbOption3)
                }
                applyFilter() // Lọc ngay lập tức
                sharedViewModel.clearFilter()
            }
        }
    }
}