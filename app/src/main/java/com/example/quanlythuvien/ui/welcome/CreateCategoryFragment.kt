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

    private lateinit var btnNextToBook: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        btnNextToBook = view.findViewById(R.id.btnNextToBook)
    }

    private fun setupListeners() {
        btnNextToBook.setOnClickListener {
            navigateToNextScreen()
        }
    }


    private fun navigateToNextScreen() {
        findNavController().navigate(R.id.createBookFragment)
    }

}