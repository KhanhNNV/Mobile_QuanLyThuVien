package com.example.quanlythuvien.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R

class CategoryAdapter(
    private val categoryList: List<String>,
    private val onEditClick: (String) -> Unit // Hàm này để báo  có người bấm cây bút
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // Ánh xạ các thành phần trong file item_category.xml
    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemCategoryName)
        val ivEdit: ImageView = view.findViewById(R.id.ivEditCategory)
    }

    // Nạp cái layout item_category.xml vào
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    // Đổ dữ liệu vào từng dòng và gài sự kiện
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoryName = categoryList[position]
        holder.tvName.text = categoryName

        // Bắt sự kiện người dùng chọt ngón tay vào cái Cây Bút
        holder.ivEdit.setOnClickListener {
            onEditClick(categoryName)
        }
    }

    // Báo cho máy biết có bao nhiêu dòng cần vẽ
    override fun getItemCount(): Int = categoryList.size
}