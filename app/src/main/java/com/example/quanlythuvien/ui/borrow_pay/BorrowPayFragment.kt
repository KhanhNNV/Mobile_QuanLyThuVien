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
    private lateinit var viewModel: BorrowPayViewModel

    // --- UI COMPONENTS ---
    private lateinit var fasAddLoan: FloatingActionButton
    private lateinit var autoSearch: AutoCompleteTextView
    private lateinit var btnToggleFilter: ImageView
    private lateinit var layoutFilterContainer: ConstraintLayout

    // Đã xóa rgStatus, khai báo đủ 4 nút Radio
    private lateinit var rbOption1: RadioButton // Overdue
    private lateinit var rbOption2: RadioButton // Active
    private lateinit var rbOption3: RadioButton // Complete
    private lateinit var rbOption4: RadioButton // Violated

    private lateinit var edtFromDate: EditText
    private lateinit var edtToDate: EditText
    private lateinit var btnResetFilter: Button
    private lateinit var btnConfirmFilter: Button
    private lateinit var recyclerView: RecyclerView

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

        viewModel.fetchLoans()
    }

    override fun onResume() {
        super.onResume()
        applyFilter()
    }

    private fun initViews(view: View) {
        fasAddLoan = view.findViewById(R.id.fasAddLoan)
        autoSearch = view.findViewById(R.id.autoSearch)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        layoutFilterContainer = view.findViewById(R.id.layoutFilterContainer)

        // Khởi tạo 4 nút
        rbOption1 = view.findViewById(R.id.rbOption1)
        rbOption2 = view.findViewById(R.id.rbOption2)
        rbOption3 = view.findViewById(R.id.rbOption3)
        rbOption4 = view.findViewById(R.id.rbOption4)

        edtFromDate = view.findViewById(R.id.edtFromDate)
        edtToDate = view.findViewById(R.id.edtToDate)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnConfirmFilter = view.findViewById(R.id.btnConfirmFilter)
        recyclerView = view.findViewById(R.id.recyclerView)
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(LoanApiService::class.java)
        val repository = LoanRepository(apiService)
        val factory = GenericViewModelFactory { BorrowPayViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[BorrowPayViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = BorrowPayAdapter { clickedItem ->
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

        // --- BƯỚC QUAN TRỌNG: Logic thủ công cho 4 RadioButton thay vì RadioGroup ---
        val radioButtons = listOf(rbOption1, rbOption2, rbOption3, rbOption4)
        for (rb in radioButtons) {
            rb.setOnClickListener { clickedView ->
                // Khi 1 nút được bấm, bỏ tick tất cả các nút khác
                for (otherRb in radioButtons) {
                    if (otherRb.id != clickedView.id) {
                        otherRb.isChecked = false
                    }
                }
            }
        }

        btnConfirmFilter.setOnClickListener { applyFilter() }

        btnResetFilter.setOnClickListener {
            edtFromDate.text.clear()
            edtToDate.text.clear()
            autoSearch.text.clear()

            // Bỏ chọn thủ công 4 nút
            rbOption1.isChecked = false
            rbOption2.isChecked = false
            rbOption3.isChecked = false
            rbOption4.isChecked = false

            applyFilter()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loanListState.collectLatest { state ->
                when (state) {
                    is LoanListState.Idle -> { }
                    is LoanListState.Loading -> { }
                    is LoanListState.Success -> {
                        adapter.submitList(state.loans)
                        if (state.loans.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy phiếu phù hợp!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is LoanListState.Error -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun applyFilter() {
        // 1. Lấy trạng thái thủ công và gửi ĐÚNG CHUẨN ENUM BACKEND MỚI
        val status = when {
            rbOption1.isChecked -> "OVERDUE"
            rbOption2.isChecked -> "ACTIVE"
            rbOption3.isChecked -> "COMPLETED"
            rbOption4.isChecked -> "VIOLATED"
            else -> null
        }

        // 2. Format ngày
        val fromDateIso = convertDateToIso(edtFromDate.text.toString().trim())
        val toDateIso = convertDateToIso(edtToDate.text.toString().trim())

        // 3. Search
        val search = autoSearch.text.toString().trim().takeIf { it.isNotEmpty() }

        // 4. Gọi API
        viewModel.fetchLoans(status, fromDateIso, toDateIso, search)
    }

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

                // Reset tất cả nút trước khi check nút mới
                rbOption1.isChecked = false
                rbOption2.isChecked = false
                rbOption3.isChecked = false
                rbOption4.isChecked = false

                // Khớp chính xác với các Enum mới
                when (type) {
                    "OVERDUE" -> rbOption1.isChecked = true
                    "ACTIVE" -> rbOption2.isChecked = true
                    "COMPLETED" -> rbOption3.isChecked = true
                    "VIOLATED" -> rbOption4.isChecked = true
                }

                applyFilter()
                sharedViewModel.clearFilter()
            }
        }
    }
}