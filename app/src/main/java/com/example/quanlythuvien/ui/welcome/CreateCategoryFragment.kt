package com.example.quanlythuvien.ui.welcome

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R
import com.google.android.material.textfield.TextInputEditText

class CreateCategoryFragment : Fragment(R.layout.fragment_create_category) {

    // Dùng ViewModel chung cho toàn bộ Activity để chia sẻ dữ liệu
    private val viewModel: OnboardingViewModel by activityViewModels()

    private lateinit var etCategoryName: EditText
    private lateinit var etCategoryDesc: EditText
    private lateinit var btnNextToBook: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        etCategoryName = view.findViewById(R.id.etCategoryName)
        etCategoryDesc = view.findViewById(R.id.etCategoryDesc)
        btnNextToBook = view.findViewById(R.id.btnNextToBook)
    }

    private fun setupListeners() {
        btnNextToBook.setOnClickListener {
            handleSaveCategory()
        }
    }

    private fun handleSaveCategory() {
        val name = etCategoryName.text.toString().trim()
        val desc = etCategoryDesc.text.toString().trim()

        if (!isValidInput(name)) {
            showError(getString(R.string.err_cateEmpty))
            return
        }

        saveDataAndNavigate(name, desc)
    }

    private fun isValidInput(name: String): Boolean {
        return name.isNotEmpty()
    }

    private fun saveDataAndNavigate(name: String, desc: String) {
        viewModel.saveCategory(name, desc) {
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.createBookFragment)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}