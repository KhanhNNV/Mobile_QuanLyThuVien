package com.example.quanlythuvien.ui.welcome

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

class CreateBookFragment : Fragment(R.layout.fragment_create_book) {

    private val viewModel: OnboardingViewModel by activityViewModels()

    private lateinit var etBookName: EditText
    private lateinit var etBookAuthor: EditText
    private lateinit var etBookQuantity: EditText
    private lateinit var etBookCode: EditText
    private lateinit var btnFinishSetup: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        etBookName = view.findViewById(R.id.etBookName)
        etBookAuthor = view.findViewById(R.id.etBookAuthor)
        etBookQuantity = view.findViewById(R.id.etBookQuantity)
        etBookCode = view.findViewById(R.id.etBookCode)
        btnFinishSetup = view.findViewById(R.id.btnFinishSetup)
    }

    private fun setupListeners() {
        btnFinishSetup.setOnClickListener {
            handleSaveBook()
        }
    }

    private fun handleSaveBook() {
        val title = etBookName.text.toString().trim()
        val author = etBookAuthor.text.toString().trim()
        val quantity = etBookQuantity.text.toString().toIntOrNull() ?: 1
        val isbnCode = etBookCode.text.toString().trim()

        if (!isValidInput(title)) {
            showError(getString(R.string.err_bookEmpty))
            return
        }

        saveDataAndNavigate(title, author, quantity, isbnCode)
    }

    private fun isValidInput(title: String): Boolean {
        return title.isNotEmpty()
    }

    private fun saveDataAndNavigate(title: String, author: String, quantity: Int, isbnCode: String) {
        viewModel.saveBook(title, author, quantity, isbnCode) {
            markOnboardingCompleted()
            navigateToDashboard()
        }
    }

    // Xử lý lưu cờ SharedPreferences
    private fun markOnboardingCompleted() {
        val prefs = requireActivity().getSharedPreferences("LibraryAppPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("IS_FIRST_LAUNCH", false)
        }
    }

    // Xử lý điều hướng và xóa lịch sử (tránh ấn nút Back quay lại trang setup)
    private fun navigateToDashboard() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        findNavController().navigate(R.id.dashboardFragment, null, navOptions)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}