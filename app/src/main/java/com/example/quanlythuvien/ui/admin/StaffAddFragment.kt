package com.example.quanlythuvien.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.UserRequest
import com.example.quanlythuvien.data.remote.UserApiService
import com.example.quanlythuvien.data.repository.UserRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StaffAddFragment : Fragment(R.layout.fragment_employee_add) {

    private lateinit var edtFullName: TextInputEditText
    private lateinit var edtUsername: TextInputEditText
    private lateinit var edtPassword: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    private lateinit var viewModel: StaffAddViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()
    }

    private fun initViews(view: View) {
        edtFullName = view.findViewById(R.id.edtEmpFullName)
        edtUsername = view.findViewById(R.id.edtEmpUsername)
        edtPassword = view.findViewById(R.id.edtEmpPassword)
        btnCancel = view.findViewById(R.id.btnCancelEmp)
        btnSave = view.findViewById(R.id.btnSaveEmp)
    }

    private fun setupViewModel() {
        val retrofit = RetrofitClient.getInstance(requireContext())
        val apiService = retrofit.create(UserApiService::class.java)
        val repository = UserRepository(apiService)

        val factory = GenericViewModelFactory { StaffAddViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[StaffAddViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addState.collectLatest { state ->
                when (state) {
                    is StaffAddState.Idle -> {
                        btnSave.isEnabled = true
                        btnSave.text = "LƯU"
                    }
                    is StaffAddState.Loading -> {
                        btnSave.isEnabled = false
                        btnSave.text = "Đang xử lý..."
                    }
                    is StaffAddState.Success -> {
                        btnSave.isEnabled = true
                        btnSave.text = "LƯU"
                        Toast.makeText(requireContext(), "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is StaffAddState.Error -> {
                        btnSave.isEnabled = true
                        btnSave.text = "LƯU"
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSave.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Reset lỗi
            edtFullName.error = null
            edtUsername.error = null
            edtPassword.error = null

            // Validate
            if (fullName.isEmpty()) {
                edtFullName.error = "Vui lòng nhập Họ và tên!"
                edtFullName.requestFocus()
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                edtUsername.error = "Vui lòng nhập Tên đăng nhập!"
                edtUsername.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                edtPassword.error = "Vui lòng nhập Mật khẩu!"
                edtPassword.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 6) {
                edtPassword.error = "Mật khẩu phải từ 6 ký tự trở lên!"
                edtPassword.requestFocus()
                return@setOnClickListener
            }

            // Gọi API
            val request = UserRequest(
                username = username,
                password = password,
                fullname = fullName,
                role = "STAFF"
            )
            viewModel.addStaff(request)
        }
    }
}