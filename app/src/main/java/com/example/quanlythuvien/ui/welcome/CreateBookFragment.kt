package com.example.quanlythuvien.ui.welcome

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.textfield.TextInputEditText

class CreateBookFragment : Fragment(R.layout.fragment_create_book) {

    private val viewModel: OnboardingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etBookName = view.findViewById<TextInputEditText>(R.id.etBookName)
        val etBookAuthor = view.findViewById<TextInputEditText>(R.id.etBookAuthor)
        val etBookQuantity = view.findViewById<TextInputEditText>(R.id.etBookQuantity)
        val etBookCode= view.findViewById<TextInputEditText>(R.id.etBookCode)
        val btnFinishSetup = view.findViewById<Button>(R.id.btnFinishSetup)

        btnFinishSetup.setOnClickListener {
            val title = etBookName.text.toString().trim()
            val author = etBookAuthor.text.toString().trim()
            val quantity = etBookQuantity.text.toString().toIntOrNull() ?: 1
            val isbnCode = etBookCode.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên sách", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi ViewModel lưu sách vào Room DB
            viewModel.saveBook(title, author, quantity,isbnCode) {
                // 1. Cập nhật cờ là đã hoàn tất Onboarding
                val prefs = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("IS_FIRST_LAUNCH", false).apply()

                // 2. Chuyển sang Dashboard và xóa sạch lịch sử để không back lại được
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()

                findNavController().navigate(R.id.dashboardFragment, null, navOptions)
            }
        }
    }
}