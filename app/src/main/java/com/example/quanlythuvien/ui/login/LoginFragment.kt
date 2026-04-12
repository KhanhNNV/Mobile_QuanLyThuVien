package com.example.quanlythuvien.ui.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.MainActivity
import com.example.quanlythuvien.R
import com.example.quanlythuvien.core.api.RetrofitClient
import com.example.quanlythuvien.data.model.request.LoginRequest
import com.example.quanlythuvien.data.remote.AuthApiService
import com.example.quanlythuvien.data.repository.AuthRepository
import com.example.quanlythuvien.utils.GenericViewModelFactory
import com.example.quanlythuvien.utils.TokenManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var btnLogin: Button
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var viewModel: LoginViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModel()
        observeViewModel()
        hanldeButtonLoginEvent()

    }

    private fun initViews(view: View){
        btnLogin=view.findViewById(R.id.btnLogin)
        edtUsername=view.findViewById(R.id.edtUsername)
        edtPassword=view.findViewById(R.id.edtPassword)
    }
    private fun setupViewModel() {
        val apiService = RetrofitClient.getInstance(requireContext()).create(AuthApiService::class.java)
        val repository = AuthRepository(apiService)
        val factory = GenericViewModelFactory { LoginViewModel(repository) }
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginState.Idle -> {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Đăng nhập"
                    }
                    is LoginState.Loading -> {
                        btnLogin.isEnabled = false
                        btnLogin.text = "Đang kiểm tra..."
                    }
                    is LoginState.Success -> {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Đăng nhập"
                        Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        // Lưu Token bằng TokenManager
                        val tokenManager = TokenManager(requireContext())
                        tokenManager.saveTokens(state.accessToken, state.refreshToken)


                        // 3. Điều hướng dựa theo Role trả về từ Server
                        val mainActivity = requireActivity() as MainActivity

                        // Giả sử backend trả về "ADMIN" hoặc "ROLE_ADMIN"
                        if (state.role.contains("ADMIN", ignoreCase = true)) {
                            mainActivity.updateBottomNavigationMenu("ADMIN")
                            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                        } else {
                            mainActivity.updateBottomNavigationMenu("STAFF")
                            findNavController().navigate(R.id.action_loginFragment_to_staffDashboardFragment)
                        }
                    }
                    is LoginState.Error -> {
                        btnLogin.isEnabled = true
                        btnLogin.text = "Đăng nhập"
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun hanldeButtonLoginEvent(){


        btnLogin.setOnClickListener {
            val username= edtUsername.text.toString().trim()
            val password=edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request = LoginRequest(username, password)
            viewModel.login(request)
        }
    }



}