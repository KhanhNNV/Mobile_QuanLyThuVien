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
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R


class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var btnLogin: Button
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        hanldeButtonLoginEvent()

    }

    private fun initViews(view: View){
        btnLogin=view.findViewById(R.id.btnLogin)
        edtEmail=view.findViewById(R.id.edtEmail)
        edtPassword=view.findViewById(R.id.edtPassword)
    }

    private fun hanldeButtonLoginEvent(){


        btnLogin.setOnClickListener {
            val email= edtEmail.text.toString().trim()
            val password=edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email == "admin" && password == "123") {
                saveLoginState(email, "ADMIN")

                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.dashboardFragment)
            } else {
                Toast.makeText(requireContext(), "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLoginState(email: String, role: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userEmail", email)
            putString("userRole", role)
            apply()
        }
    }


}