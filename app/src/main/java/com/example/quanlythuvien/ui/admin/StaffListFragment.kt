package com.example.quanlythuvien.ui.admin

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
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
import com.example.quanlythuvien.data.model.response.UserResponse
import com.example.quanlythuvien.data.remote.UserApiService
import com.example.quanlythuvien.data.repository.UserRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StaffListFragment : Fragment(R.layout.fragment_manager_staff_of_admin) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var staffAdapter: StaffAdapter
    private lateinit var viewModel: StaffViewModel
    private lateinit var autoSearch: AutoCompleteTextView
    private lateinit var spinnerFilter: Spinner
    private lateinit var fabAdd: FloatingActionButton

    private var currentDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view, "Quản lý nhân viên")

        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupSearchAndFilter()
        observeViewModel()
        handleEvents()

        viewModel.fetchUsers()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewStaff)
        autoSearch = view.findViewById(R.id.autoSearchStaff)
        spinnerFilter = view.findViewById(R.id.spnStatusFilter)
        fabAdd = view.findViewById(R.id.fasAddStaff)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(UserApiService::class.java)
        val repository = UserRepository(apiService)

        val factory = GenericViewModelFactory {
            StaffViewModel(repository)
        }
        viewModel = ViewModelProvider(this, factory)[StaffViewModel::class.java]
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        staffAdapter = StaffAdapter(
            staffList = mutableListOf(),
            onEditClick = { staff ->
                showEditStaffDialog(staff)
            },
            onDeleteClick = { staff ->
                showDeleteConfirmDialog(staff)
            }
        )
        recyclerView.adapter = staffAdapter
    }

    private fun setupSearchAndFilter() {
        // Setup Filter Spinner
        val filters = StaffFilter.values().map { it.displayName }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filters
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = StaffFilter.values()[position]
                viewModel.setFilter(selectedFilter)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Setup Search
        autoSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchUsers(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.filteredUsers.collectLatest { users ->
                        staffAdapter.updateData(users)
                    }
                }

                launch {
                    viewModel.state.collectLatest { state ->
                        when (state) {
                            is StaffState.Loading -> {
                                // Show loading
                            }
                            is StaffState.SuccessList -> {
                                // Data updated via filteredUsers flow
                            }
                            is StaffState.SuccessAction -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                currentDialog?.dismiss()
                            }
                            is StaffState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                                // Enable save button in dialog if error
                                currentDialog?.findViewById<Button>(R.id.btnSave)?.isEnabled = true
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun handleEvents() {
        fabAdd.setOnClickListener {
            showAddStaffDialog()
        }
    }

    private fun showAddStaffDialog() {
        // Hiển thị dialog thêm nhân viên (nếu có)
        Toast.makeText(requireContext(), "Chức năng thêm nhân viên đang phát triển", Toast.LENGTH_SHORT).show()
    }

    private fun showEditStaffDialog(staffToEdit: UserResponse) {
        val dialog = Dialog(requireContext())
        currentDialog = dialog
        dialog.setContentView(R.layout.dialog_edit_staff)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val edtFullname = dialog.findViewById<EditText>(R.id.edtEditFullname)
        val edtUsername = dialog.findViewById<EditText>(R.id.edtEditUsername)
        val edtPassword = dialog.findViewById<EditText>(R.id.edtEditPassword)
        val swActive = dialog.findViewById<SwitchCompat>(R.id.swEditActive)
        val spinnerRole = dialog.findViewById<Spinner>(R.id.spinnerRole)
        val tvRoleLabel = dialog.findViewById<TextView>(R.id.tvRoleLabel)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        // Setup dialog title
        tvDialogTitle?.text = "Chỉnh sửa nhân viên"

        // Setup Role Spinner
        val roles = arrayOf("STAFF", "ADMIN")
        val roleDisplayNames = roles.map { role ->
            when (role) {
                "ADMIN" -> "Quản trị viên"
                "STAFF" -> "Nhân viên"
                else -> role
            }
        }
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roleDisplayNames
        )
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole?.adapter = roleAdapter

        // Set current data
        edtFullname?.setText(staffToEdit.fullname)
        edtUsername?.setText(staffToEdit.username)
        edtUsername?.isEnabled = false // Không cho sửa username
        swActive?.isChecked = staffToEdit.isActive

        // Set role
        val roleIndex = if (staffToEdit.role == "ADMIN") 1 else 0
        spinnerRole?.setSelection(roleIndex)

        // Ẩn password field khi edit (optional)
        edtPassword?.visibility = View.GONE
        dialog.findViewById<TextView>(R.id.tvPasswordLabel)?.visibility = View.GONE

        btnCancel?.setOnClickListener {
            dialog.dismiss()
        }

        btnSave?.setOnClickListener {
            val fullname = edtFullname?.text.toString().trim()
            if (fullname.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRoleIndex = spinnerRole?.selectedItemPosition ?: 0
            val newRole = roles[selectedRoleIndex]

            // Disable save button during update
            btnSave.isEnabled = false
            btnSave.text = "Đang xử lý..."

            viewModel.updateUser(
                id = staffToEdit.userId,
                fullname = fullname,
                role = newRole,
                isActive = swActive?.isChecked ?: true
            )
        }

        dialog.setOnDismissListener {
            currentDialog = null
        }

        dialog.show()
    }

    private fun showDeleteConfirmDialog(staff: UserResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa nhân viên")
            .setMessage("Bạn có chắc chắn muốn xóa nhân viên ${staff.fullname} không?\n\nLưu ý: Hành động này không thể hoàn tác.")
            .setPositiveButton("Xóa") { dialog, _ ->
                viewModel.deleteUser(staff.userId)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialog?.dismiss()
        currentDialog = null
    }
}