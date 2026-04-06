package com.example.quanlythuvien.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.quanlythuvien.R
import androidx.navigation.fragment.findNavController

class CategoryAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp giao diện XML của màn hình Thêm Thể Loại
        return inflater.inflate(R.layout.fragment_category_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnCancelCategory = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelCategory)

        btnCancelCategory.setOnClickListener {

            findNavController().popBackStack()
        }
    }
}