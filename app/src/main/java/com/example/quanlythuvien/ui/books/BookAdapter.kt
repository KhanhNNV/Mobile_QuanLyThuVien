package com.example.quanlythuvien.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book // Nhớ import đúng đường dẫn Entity của bạn

class BookAdapter(private val bookList: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvBookTitle)
        val tvAuthor: TextView = view.findViewById(R.id.tvBookAuthor)
        val tvIsbn: TextView = view.findViewById(R.id.tvBookIsbn)
        val tvQuantity: TextView = view.findViewById(R.id.tvBookQuantity)
        val tvCategory: TextView = view.findViewById(R.id.tvBookCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // Thay R.layout.item_book bằng tên file XML item của bạn nếu nó khác
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = bookList[position]

        // Gán dữ liệu (Lưu ý: Thay đổi tên thuộc tính như title, author... cho khớp với Entity Book của bạn)
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvIsbn.text = "ISBN: ${book.isbnCode}"

        // Cập nhật số lượng
        holder.tvQuantity.text = "Còn ${book.availableQuantity} bản"

        // Cập nhật danh mục
        holder.tvCategory.text = book.categoryId.toString() // Hoặc book.categoryName tuỳ cách bạn thiết kế
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}