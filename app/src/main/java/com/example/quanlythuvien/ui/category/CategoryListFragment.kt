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

        val fabAddCategory = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddCategory)
        fabAddCategory.setOnClickListener {
            findNavController().navigate(R.id.categoryAddFragment)
        }

        val mockData = listOf(
            "Công nghệ thông tin",
            "Kinh tế - Tài chính",
            "Văn học - Tiểu thuyết",
            "Lịch sử - Địa lý"
        )

        // 2. Tìm cái RecyclerView trong file fragment_category_list.xml
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewCategories)

        // 3. Nói cho Android biết là hãy xếp danh sách này theo chiều dọc (từ trên xuống)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())

        // 4. Lắp Adapter vào cái RecyclerView
        recyclerView.adapter = CategoryAdapter(mockData) { selectedName ->
            findNavController().navigate(R.id.categoryEditFragment)
        }

    }
}