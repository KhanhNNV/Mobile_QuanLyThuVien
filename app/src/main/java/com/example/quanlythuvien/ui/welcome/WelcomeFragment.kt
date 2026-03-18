package com.example.quanlythuvien.ui.welcome

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStartSetup = view.findViewById<Button>(R.id.btnStartSetup)

        btnStartSetup.setOnClickListener {
            // Chuyển sang màn hình Tạo Thể Loại
            findNavController().navigate(R.id.createCategoryFragment)
        }
    }
}