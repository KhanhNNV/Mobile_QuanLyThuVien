package com.example.quanlythuvien.ui.category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.quanlythuvien.R

class CategoryListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
        val tvHeaderSubtitle = view.findViewById<TextView>(R.id.tvHeaderSubtitle)

        tvHeaderTitle?.text = "Thể loại"
        tvHeaderSubtitle?.text = "Quản lý thể loại sách"

        val btnAddCategory = view.findViewById<Button>(R.id.btnAddCategory)
        btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.categoryAddFragment)
        }

    }
}