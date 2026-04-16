package com.example.quanlythuvien.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.CategoryResponse

class CategoryAdapter(

    private var categoryList: List<CategoryResponse>,

    private val onEditClick: (CategoryResponse) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemCategoryName)
        val ivEdit: ImageView = view.findViewById(R.id.ivEditCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]

        //Lấy tên từ object ra để gán vào TextView
        holder.tvName.text = category.name

        holder.ivEdit.setOnClickListener {
            // Ném nguyên cục category (có cả ID lẫn Tên) cho Fragment xử lý
            onEditClick(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size

    //CẬP NHẬT GIAO DIỆN (Fragment sẽ gọi hàm này)
    fun updateData(newList: List<CategoryResponse>) {
        categoryList = newList
        notifyDataSetChanged() // Quét F5 báo cho Android vẽ lại danh sách mới!
    }
}