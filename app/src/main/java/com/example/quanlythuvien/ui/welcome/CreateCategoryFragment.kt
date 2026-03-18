package com.example.quanlythuvien.ui.welcome

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.textfield.TextInputEditText

class CreateCategoryFragment : Fragment(R.layout.fragment_create_category) {

    // Dùng ViewModel chung cho toàn bộ Activity để chia sẻ dữ liệu
    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etCategoryName = view.findViewById<TextInputEditText>(R.id.etCategoryName)
        val etCategoryDesc = view.findViewById<TextInputEditText>(R.id.etCategoryDesc)
        val btnNextToBook = view.findViewById<Button>(R.id.btnNextToBook)

        btnNextToBook.setOnClickListener {
            val name = etCategoryName.text.toString().trim()
            val desc = etCategoryDesc.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên thể loại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi ViewModel lưu vào Room DB
            viewModel.saveCategory(name, desc) {
                // Lambda callback: Khi lưu xong thì chuyển trang
                findNavController().navigate(R.id.createBookFragment)
            }
        }
    }
}