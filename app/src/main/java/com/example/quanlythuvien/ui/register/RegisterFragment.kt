package com.example.quanlythuvien.ui.register

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtLibrary: EditText
    private lateinit var edtAddress: EditText
    private lateinit var cbStudentDiscount: CheckBox
    private lateinit var btnRegister: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        handleButtonRegisterEvent()
    }

    private fun initViews(view: View) {
        edtUsername = view.findViewById(R.id.edtUsername)
        edtPassword = view.findViewById(R.id.edtPassword)
        edtLibrary = view.findViewById(R.id.edtLibrary)
        edtAddress = view.findViewById(R.id.edtAddress)
        cbStudentDiscount = view.findViewById(R.id.cbStudentDiscount)
        btnRegister = view.findViewById(R.id.btnRegister)
    }

    private fun handleButtonRegisterEvent() {
        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val libraryName = edtLibrary.text.toString().trim()
            val address = edtAddress.text.toString().trim()



            findNavController().navigate(R.id.createCategoryFragment)
        }
    }

}