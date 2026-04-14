package com.example.quanlythuvien.ui.register

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.MainActivity
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.RegisterRequest
import com.example.quanlythuvien.data.remote.AuthApiService
import com.example.quanlythuvien.data.repository.AuthRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.JwtUtils
import com.example.quanlythuvien.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtLibrary: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnRegister: Button
    private lateinit var viewModel: RegisterViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        observeViewModel()
        handleButtonRegisterEvent()
    }

    private fun initViews(view: View) {
        edtUsername = view.findViewById(R.id.edtUsername)
        edtPassword = view.findViewById(R.id.edtPassword)
        edtLibrary = view.findViewById(R.id.edtLibrary)
        edtAddress = view.findViewById(R.id.edtAddress)
        btnRegister = view.findViewById(R.id.btnRegister)
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(AuthApiService::class.java)
         val repository = AuthRepository(apiService)
         val factory = GenericViewModelFactory{ RegisterViewModel(repository) }
         viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.Idle -> {
                        btnRegister.isEnabled = true
                        btnRegister.text = getString(R.string.btnRegister)
                    }
                    is RegisterState.Loading -> {
                        btnRegister.isEnabled = false
                        btnRegister.text = "Đang xử lý..."
                    }
                    is RegisterState.Success -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()

                        val tokenManager = TokenManager(requireContext())
                        tokenManager.saveTokens(state.accessToken, state.refreshToken)

                        val role = JwtUtils.getRoleFromToken(state.accessToken)

                        if (role.contains("ADMIN", ignoreCase = true)) {
                            findNavController().navigate(R.id.createCategoryFragment)
                        } else {
                            Toast.makeText(requireContext(), "Bạn là staff!! thật vô lý :) ", Toast.LENGTH_LONG).show()
                        }
                    }
                    is RegisterState.Error -> {
                        btnRegister.isEnabled = true
                        btnRegister.text = getString(R.string.btnRegister)
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handleButtonRegisterEvent() {
        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val libraryName = edtLibrary.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            if (username.isEmpty() || password.isEmpty() || libraryName.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                username = username,
                password = password,
                fullName = username, // Lấy username làm fullName mặc định
                libraryName = libraryName,
                address = address
            )

            // Gọi ViewModel để xử lý
            viewModel.registerAndLogin(request)
        }
    }

}