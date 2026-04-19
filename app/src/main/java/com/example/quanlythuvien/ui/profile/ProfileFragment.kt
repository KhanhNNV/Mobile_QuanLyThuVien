package com.example.quanlythuvien.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.remote.UserApiService
import com.example.quanlythuvien.data.repository.UserRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import com.example.quanlythuvien.utils.setupHeaderWithBack
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: ProfileViewModel

    // Views
    private lateinit var edtUsername: TextInputEditText
    private lateinit var edtLibraryName: TextInputEditText
    private lateinit var edtLibraryAddress: TextInputEditText
    private lateinit var edtCreatedAt: TextInputEditText
    private lateinit var edtUpdatedAt: TextInputEditText
    private lateinit var edtFullname: TextInputEditText
    private lateinit var edtNewPassword: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHeaderWithBack(view,"Thông tin cá nhân")

        initViews(view)
        setupViewModel()
        observeViewModel()
        setupListeners()

        // Gọi API tải dữ liệu
        viewModel.loadMyProfile()
    }

    private fun initViews(view: View) {
        edtUsername = view.findViewById(R.id.edtUsername)
        edtLibraryName = view.findViewById(R.id.edtLibraryName)
        edtLibraryAddress = view.findViewById(R.id.edtLibraryAddress)
        edtCreatedAt = view.findViewById(R.id.edtCreatedAt)
        edtUpdatedAt = view.findViewById(R.id.edtUpdatedAt)
        edtFullname = view.findViewById(R.id.edtFullname)
        edtNewPassword = view.findViewById(R.id.edtNewPassword)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)


    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(UserApiService::class.java)
        val repository = UserRepository(apiService)
        val factory = GenericViewModelFactory { ProfileViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupListeners() {
        btnSaveChanges.setOnClickListener {
            val fullname = edtFullname.text.toString().trim()
            val newPassword = edtNewPassword.text.toString().trim()

            if (fullname.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateMyProfile(fullname, newPassword)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collectLatest { state ->
                    when (state) {
                        is ProfileState.Idle -> {}
                        is ProfileState.Loading -> {
                            btnSaveChanges.isEnabled = false
                            btnSaveChanges.text = "Đang xử lý..."
                        }
                        is ProfileState.LoadSuccess -> {
                            btnSaveChanges.isEnabled = true
                            btnSaveChanges.text = "Lưu Thay Đổi"

                            // Đổ dữ liệu lên UI
                            edtUsername.setText(state.user.username)
                            edtFullname.setText(state.user.fullname)
                            edtCreatedAt.setText(formatDate(state.user.createdAt))
                            edtUpdatedAt.setText(formatDate(state.user.updateAt))
                            edtLibraryName.setText(state.user.libraryName)
                            edtLibraryAddress.setText(state.user.address)
                        }
                        is ProfileState.UpdateSuccess -> {
                            btnSaveChanges.isEnabled = true
                            btnSaveChanges.text = "Lưu Thay Đổi"
                            edtNewPassword.text?.clear() // Xóa ô nhập pass sau khi cập nhật thành công

                            // Cập nhật lại ngày update
                            edtUpdatedAt.setText(formatDate(state.user.updateAt))

                            Toast.makeText(requireContext(), "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show()
                        }
                        is ProfileState.Error -> {
                            btnSaveChanges.isEnabled = true
                            btnSaveChanges.text = "Lưu Thay Đổi"
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // Hàm format String Date từ ISO (VD: 2024-05-18T10:30:00) sang chuẩn VN (18/05/2024)
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return "Không có dữ liệu"
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString // Nếu lỗi parse thì hiện nguyên bản
        }
    }
}