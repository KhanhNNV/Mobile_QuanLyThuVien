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


    private lateinit var btnFinishSetup: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        btnFinishSetup = view.findViewById(R.id.btnFinishSetup)
    }

    private fun setupListeners() {
        btnFinishSetup.setOnClickListener {
            navigateToDashboard()
        }
    }


    // Xử lý điều hướng và xóa lịch sử (tránh ấn nút Back quay lại trang setup)
    private fun navigateToDashboard() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()

        findNavController().navigate(R.id.dashboardFragment, null, navOptions)
    }
}