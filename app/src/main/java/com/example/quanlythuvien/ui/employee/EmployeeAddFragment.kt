package com.example.quanlythuvien.ui.employee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.quanlythuvien.R
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EmployeeAddFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_employee_add, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ánh xạ View
        val edtFullName = view.findViewById<TextInputEditText>(R.id.edtEmpFullName)
        val edtUsername = view.findViewById<TextInputEditText>(R.id.edtEmpUsername)
        val edtPassword = view.findViewById<TextInputEditText>(R.id.edtEmpPassword)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEmp)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveEmp)

        // 2. Nút Hủy -> Quay về trang trước
        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        btnSave.setOnClickListener {
            val fullName = edtFullName.text.toString().trim()
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            // Reset lại trạng thái lỗi trước khi check
            edtFullName.error = null
            edtUsername.error = null
            edtPassword.error = null

            // BẮT LỖI TỪNG Ô
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

            Toast.makeText(requireContext(), "Tạo nhân viên $fullName thành công!", Toast.LENGTH_SHORT).show()

            // Quay về danh sách nhân viên
            findNavController().popBackStack()
        }
    }
}