package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.BookDropDownItem // Thêm import này

class SelectedBookAdapter(
    private val onRemoveClick: (Long) -> Unit // Trả về ID của bản sao sách
) : RecyclerView.Adapter<SelectedBookAdapter.ViewHolder>() {

    private var items = listOf<BookDropDownItem>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInfo: TextView = view.findViewById(R.id.tvSelectedBookInfo)
        val tvAuthor: TextView = view.findViewById(R.id.tvSelectedBookAuthor)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemoveSelectedBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_book, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Gọi hàm toString() đã override trong BookDropDownItem để hiển thị (Mã sách - Tên sách)
        holder.tvInfo.text = item.toString()

        holder.tvAuthor.text = item.bookCopy.author

        holder.btnRemove.setOnClickListener {
            // Truy xuất ID từ object bookCopy bên trong
            onRemoveClick(item.bookCopy.copyId)
        }
    }

    override fun getItemCount(): Int = items.size

    // Thay đổi tham số truyền vào
    fun submitList(newList: List<BookDropDownItem>) {
        items = newList
        notifyDataSetChanged()
    }
}