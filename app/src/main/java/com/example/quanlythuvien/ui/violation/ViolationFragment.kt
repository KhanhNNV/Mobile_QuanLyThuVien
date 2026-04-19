package com.example.quanlythuvien.ui.violation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.response.ViolationResponse
import com.example.quanlythuvien.data.remote.ViolationApiService
import com.example.quanlythuvien.data.repository.ViolationRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupHeaderWithBack
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ViolationFragment : Fragment(R.layout.fragment_violation) {

    // Views
    private lateinit var etSearch: EditText
    private lateinit var btnToggleFilter: ImageView
    private lateinit var llFilterContainer: LinearLayout
    private lateinit var rgStatus: RadioGroup
    private lateinit var edtFromDate: EditText
    private lateinit var edtToDate: EditText
    private lateinit var btnResetFilter: Button
    private lateinit var btnApplyFilter: Button

    private lateinit var rvViolations: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var btnRetryLoadViolations: Button

    // Adapter & ViewModel
    private lateinit var violationAdapter: ViolationAdapter
    private lateinit var viewModel: ViolationViewModel

    // Filter variables
    private var currentSearch: String? = null
    private var currentStatus: String? = null
    private var currentStartDate: String? = null
    private var currentEndDate: String? = null

    // Date Formatters (API expects ISO 8601, UI shows dd/MM/yyyy)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val uiDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var isAdmin: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý vi phạm")

        val tokenManager = TokenManager(requireContext())
        val role = tokenManager.getRole()
        isAdmin = (role == "ADMIN")

        initViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        handleEvents()

        // Gọi lấy dữ liệu lần đầu tiên (tải lại từ đầu)
        fetchViolations()
    }

    private fun initViews(view: View) {
        etSearch = view.findViewById(R.id.etSearch)
        btnToggleFilter = view.findViewById(R.id.btnToggleFilter)
        llFilterContainer = view.findViewById(R.id.llFilterContainer)
        rgStatus = view.findViewById(R.id.rgStatus)
        edtFromDate = view.findViewById(R.id.edtFromDate)
        edtToDate = view.findViewById(R.id.edtToDate)
        btnResetFilter = view.findViewById(R.id.btnResetFilter)
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter)

        rvViolations = view.findViewById(R.id.recyclerViewViolations)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        btnRetryLoadViolations = view.findViewById(R.id.btnRetryLoadViolations)

        llFilterContainer.visibility = View.GONE
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(ViolationApiService::class.java)
        val repository = ViolationRepository(apiService)

        val factory = GenericViewModelFactory {
            ViolationViewModel(repository)
        }
        viewModel = ViewModelProvider(this, factory)[ViolationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        rvViolations.layoutManager = layoutManager
        violationAdapter = ViolationAdapter(
            violationList = mutableListOf(),
            isAdmin=isAdmin,
            onEditClick = { violation -> showEditDialog(violation) },
            onDeleteClick = { violation -> showDeleteConfirmDialog(violation) },
            onViewLoanClick = { loanId ->
                val bundle = Bundle().apply {
                    putLong("loanId", loanId)
                }
                findNavController().navigate(R.id.action_violation_to_loanDetail, bundle)
            }
        )
        rvViolations.adapter = violationAdapter

        // --- BỘ LẮNG NGHE CUỘN XUỐNG DƯỚI CÙNG ---
        rvViolations.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // dy > 0 nghĩa là đang cuộn xuống
                if (dy > 0) {
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    // Kiểm tra nếu không bận load và chưa phải trang cuối
                    if (!viewModel.isLoading && !viewModel.isLastPage) {
                        // Nếu vị trí hiện tại + số item đang hiển thị >= tổng số item -> Đã đến cuối danh sách
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            // Gọi tải trang tiếp theo (isRefresh = false)
                            viewModel.fetchViolations(isRefresh = false)
                        }
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    when (state) {
                        is ViolationState.Loading -> {
                            // Cần xử lý UI Loading ở đây nếu muốn (ví dụ: ProgressBar)
                            // Hiện tại đang để trống để tránh giật UI khi load mượt.
                        }
                        is ViolationState.SuccessList -> {
                            val list = state.violations
                            if (list.isEmpty()) {
                                rvViolations.visibility = View.GONE
                                layoutEmptyState.visibility = View.VISIBLE
                                tvEmptyState.text = "Không có vi phạm phù hợp"
                                btnRetryLoadViolations.visibility = View.GONE
                            } else {
                                rvViolations.visibility = View.VISIBLE
                                layoutEmptyState.visibility = View.GONE
                                violationAdapter.updateData(list)
                            }
                        }
                        is ViolationState.SuccessAction -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            fetchViolations() // Sau khi Update/Delete thành công -> Reload lại từ đầu
                        }
                        is ViolationState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            // Chỉ hiển thị Empty State báo lỗi nếu danh sách hiện tại đang trống
                            if (violationAdapter.itemCount == 0) {
                                rvViolations.visibility = View.GONE
                                layoutEmptyState.visibility = View.VISIBLE
                                tvEmptyState.text = state.message
                                btnRetryLoadViolations.visibility = View.VISIBLE
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun handleEvents() {
        // Mở/Đóng Bộ lọc
        btnToggleFilter.setOnClickListener {
            llFilterContainer.visibility = if (llFilterContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Search bằng bàn phím
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentSearch = etSearch.text.toString().trim().ifEmpty { null }
                // Gọi load lại từ đầu với text tìm kiếm
                fetchViolations()
                true
            } else {
                false
            }
        }

        // Chọn ngày
        edtFromDate.setOnClickListener {
            showDatePickerDialog { calendar ->
                currentStartDate = apiDateFormat.format(calendar.time)
                edtFromDate.setText(uiDateFormat.format(calendar.time))
            }
        }

        edtToDate.setOnClickListener {
            showDatePickerDialog { calendar ->
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                currentEndDate = apiDateFormat.format(calendar.time)
                edtToDate.setText(uiDateFormat.format(calendar.time))
            }
        }

        // Apply Filter
        btnApplyFilter.setOnClickListener {
            currentStatus = when (rgStatus.checkedRadioButtonId) {
                R.id.rbActive -> "ACTIVE"
                R.id.rbResolved -> "RESOLVED"
                else -> null
            }

            // Đóng bộ lọc và gọi load lại từ đầu
            llFilterContainer.visibility = View.GONE
            fetchViolations()
        }

        // Reset Filter
        btnResetFilter.setOnClickListener {
            rgStatus.check(R.id.rbAll)
            edtFromDate.text.clear()
            edtToDate.text.clear()

            currentSearch = null
            currentStatus = null
            currentStartDate = null
            currentEndDate = null
            etSearch.text.clear()

            // Tải lại danh sách gốc
            fetchViolations()
        }

        // Retry on error
        btnRetryLoadViolations.setOnClickListener {
            fetchViolations()
        }
    }

    // Hàm gọi lấy dữ liệu mặc định (Refresh từ đầu với các filter hiện tại)
    private fun fetchViolations() {
        viewModel.fetchViolations(
            isRefresh = true,
            search = currentSearch,
            status = currentStatus,
            startDate = currentStartDate,
            endDate = currentEndDate
        )
    }

    private fun showDatePickerDialog(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, dayOfMonth, 0, 0, 0)
                onDateSelected(selectedCal)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showEditDialog(violation: ViolationResponse) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_edit_violation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val edtReason = dialog.findViewById<EditText>(R.id.edtReason)
        val spinnerStatus = dialog.findViewById<Spinner>(R.id.spinnerStatus)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSavePolicy)

        val statuses = listOf("ACTIVE", "RESOLVED")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses)
        spinnerStatus?.adapter = adapter

        // Điền dữ liệu cũ
        edtReason?.setText(violation.reason)
        val statusIndex = statuses.indexOf(violation.status)
        if (statusIndex >= 0) {
            spinnerStatus?.setSelection(statusIndex)
        }

        btnCancel?.setOnClickListener { dialog.dismiss() }

        btnSave?.setOnClickListener {
            val newReason = edtReason?.text.toString().trim()
            val newStatus = spinnerStatus?.selectedItem.toString()

            if (newReason.isEmpty()) {
                Toast.makeText(requireContext(), "Lý do không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateViolation(violation.violationId, newReason, newStatus)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmDialog(violation: ViolationResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa vi phạm")
            .setMessage("Bạn có chắc chắn muốn xóa vi phạm #${violation.violationId} không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                viewModel.deleteViolation(violation.violationId)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}