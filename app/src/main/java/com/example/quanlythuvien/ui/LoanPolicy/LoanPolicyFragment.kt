package com.example.quanlythuvien.ui.LoanPolicy

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.example.quanlythuvien.data.model.response.CategoryResponse
import com.example.quanlythuvien.data.model.response.LoanPolicyResponse
import com.example.quanlythuvien.data.remote.CategoryApiService
import com.example.quanlythuvien.data.remote.LibraryApiService
import com.example.quanlythuvien.data.remote.LoanPolicyApiService
import com.example.quanlythuvien.data.repository.CategoryRepository
import com.example.quanlythuvien.data.repository.LibraryRepository
import com.example.quanlythuvien.data.repository.LoanPolicyRepository
import com.example.quanlythuvien.ui.LoanPolicy.LoanPolicyAdapter
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoanPolicyFragment : Fragment(R.layout.fragment_loan_policy) {
    private lateinit var btnBack: ImageButton
    private lateinit var fabAddPolicy: FloatingActionButton
    private lateinit var rvPolicies: RecyclerView
    private lateinit var policyAdapter: LoanPolicyAdapter

    private lateinit var edtMaxLoansQuota: EditText
    private lateinit var btnSaveLoansQuota: ImageButton

    private lateinit var edtMaxBooksQuota: EditText
    private lateinit var btnSaveBooksQuota: ImageButton

    private lateinit var viewModel: LoanPolicyViewModel
    private var categoryList: List<CategoryResponse> = emptyList()
    private var isAdmin: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý chính sách mượn trả")

        val tokenManager = TokenManager(requireContext())
        // Thêm kiểm tra ROLE_ADMIN phòng trường hợp backend trả về có prefix ROLE_
        isAdmin = tokenManager.getRole() == "ADMIN"

        initViews(view)
        setupViewModel()
        setupRecyclerView()
        observeViewModel()
        handleEvents()

        viewModel.fetchPolicies()
        viewModel.fetchCategories()


        viewModel.fetchLibraryQuota()
    }

    private fun initViews(view: View){
        btnBack = view.findViewById(R.id.btnBack)
        fabAddPolicy = view.findViewById(R.id.fabAddPolicy)
        rvPolicies = view.findViewById(R.id.rvPolicies)
        edtMaxLoansQuota = view.findViewById(R.id.edtMaxLoansQuota)
        btnSaveLoansQuota = view.findViewById(R.id.btnSaveLoansQuota)
        edtMaxBooksQuota = view.findViewById(R.id.edtMaxBooksQuota)
        btnSaveBooksQuota = view.findViewById(R.id.btnSaveBooksQuota)

        // Phân quyền cho phần nhập hạn ngạch
        if (!isAdmin) {
            edtMaxLoansQuota.isEnabled = false
            btnSaveLoansQuota.visibility = View.GONE
            edtMaxBooksQuota.isEnabled = false
            btnSaveBooksQuota.visibility = View.GONE
        }
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())

        // Khởi tạo các API Service
        val apiService = retrofit.create(LoanPolicyApiService::class.java)
        val categoryApi = retrofit.create(CategoryApiService::class.java)
        val libraryApi = retrofit.create(LibraryApiService::class.java)

        // Khởi tạo các Repository
        val repository = LoanPolicyRepository(apiService)
        val categoryRepo = CategoryRepository(categoryApi)
        val libraryRepo = LibraryRepository(libraryApi)

        val factory = GenericViewModelFactory {
            LoanPolicyViewModel(repository, categoryRepo, libraryRepo)
        }
        viewModel = ViewModelProvider(this, factory)[LoanPolicyViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is PolicyState.Loading -> { /* Có thể hiển thị progress bar ở đây */ }
                            is PolicyState.SuccessList -> {
                                policyAdapter.updateData(state.policies)
                            }
                            is PolicyState.SuccessAction -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            is PolicyState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.categories.collectLatest { list ->
                        categoryList = list
                    }
                }

                launch {
                    viewModel.libraryQuota.collectLatest { quota ->
                        if (!edtMaxLoansQuota.hasFocus()) {
                            edtMaxLoansQuota.setText(quota.toString())
                        }
                    }
                }
                launch {
                    viewModel.libraryBooksQuota.collectLatest { quota ->
                        if (!edtMaxBooksQuota.hasFocus()) {
                            edtMaxBooksQuota.setText(quota.toString())
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        rvPolicies.layoutManager = LinearLayoutManager(requireContext())
        policyAdapter = LoanPolicyAdapter(
            policyList = mutableListOf<LoanPolicyResponse>(),
            isAdmin = isAdmin,
            onEditClick = { selectedPolicy -> showPolicyDialog(selectedPolicy) },
            onDeleteClick = { selectedPolicy, _ -> showDeleteConfirmDialog(selectedPolicy) }
        )
        rvPolicies.adapter = policyAdapter
    }

    private fun handleEvents(){
        btnBack.setOnClickListener { findNavController().popBackStack() }

        if (isAdmin) {
            // Cho phép Admin thêm chính sách
            fabAddPolicy.visibility = View.VISIBLE
            fabAddPolicy.setOnClickListener { showPolicyDialog() }

            // Lắng nghe sự kiện click nút tick
            btnSaveLoansQuota.setOnClickListener {
                saveQuota()
            }

            // Lắng nghe sự kiện bấm nút Hoàn tất (Done) trên bàn phím ảo
            edtMaxLoansQuota.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveQuota()
                    true
                } else {
                    false
                }
            }
            btnSaveBooksQuota.setOnClickListener { saveBooksQuota() }
            edtMaxBooksQuota.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveBooksQuota()
                    true
                } else false
            }
        } else {
            // Ẩn nút thêm chính sách đối với Staff
            fabAddPolicy.visibility = View.GONE
        }
    }

    private fun saveQuota() {
        val quotaStr = edtMaxLoansQuota.text.toString().trim()
        if (quotaStr.isNotEmpty()) {
            val newQuota = quotaStr.toIntOrNull() ?: 0
            viewModel.updateLibraryQuota(newQuota)

            edtMaxLoansQuota.clearFocus()

        }
    }

    private fun saveBooksQuota() {
        val quotaStr = edtMaxBooksQuota.text.toString().trim()
        if (quotaStr.isNotEmpty()) {
            val newQuota = quotaStr.toIntOrNull() ?: 0
            viewModel.updateLibraryBooksQuota(newQuota)
            edtMaxBooksQuota.clearFocus()
        }
    }

    private fun showPolicyDialog(policyToEdit: LoanPolicyResponse? = null) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loan_policy)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val edtMaxDays = dialog.findViewById<EditText>(R.id.edtMaxDays)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSavePolicy = dialog.findViewById<Button>(R.id.btnSavePolicy)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinnerCategory)

        val categoryNames = mutableListOf("Tất cả thể loại")
        categoryNames.addAll(categoryList.map { it.name })

        // Tạo Adapter cho Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames)
        spinnerCategory?.adapter = adapter

        if (policyToEdit != null) {
            tvDialogTitle?.text = "Sửa Chính Sách"
            edtMaxDays?.setText(policyToEdit.maxBorrowDays.toString())

            if (policyToEdit.categoryId != null) {
                // Tìm vị trí của category trong list (Cộng thêm 1 vì vị trí 0 là "Tất cả thể loại")
                val index = categoryList.indexOfFirst { it.categoryId == policyToEdit.categoryId }
                if (index != -1) {
                    spinnerCategory?.setSelection(index + 1)
                }
            } else {
                spinnerCategory?.setSelection(0) // Chọn "Tất cả thể loại"
            }
        } else {
            tvDialogTitle?.text = "Thêm Chính Sách"
        }

        btnCancel?.setOnClickListener { dialog.dismiss() }

        btnSavePolicy?.setOnClickListener {
            val maxDaysStr = edtMaxDays?.text.toString().trim()
            if (maxDaysStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số ngày mượn tối đa!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lấy ID thể loại được chọn từ Spinner
            val selectedPosition = spinnerCategory?.selectedItemPosition ?: 0
            val categoryIdToSave: Long? = if (selectedPosition == 0) {
                null // Nếu chọn "Tất cả thể loại" thì id = null
            } else {
                // Trừ 1 để lấy đúng index trong list gốc (vì mục 0 là "Tất cả")
                categoryList[selectedPosition - 1].categoryId
            }

            viewModel.savePolicy(
                policyId = policyToEdit?.policyId,
                categoryId = categoryIdToSave,
                maxDays = maxDaysStr.toInt()
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDeleteConfirmDialog(policy: LoanPolicyResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa chính sách")
            .setMessage("Bạn có chắc chắn muốn xóa chính sách này không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                viewModel.deletePolicy(policy.policyId)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}