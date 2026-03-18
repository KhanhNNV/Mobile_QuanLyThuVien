package com.example.quanlythuvien.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book

class BookAdapter : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    // 1. ViewHolder: Nơi chứa và ánh xạ các View từ file XML
    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvBookQuantity)

        // Hàm này dùng để đổ dữ liệu của 1 cuốn sách vào các TextView
        fun bind(book: Book) {
            tvTitle.text = book.title
            tvAuthor.text = "Tác giả: ${book.author}"
            tvQuantity.text = "Sẵn sàng mượn: ${book.availableQuantity}/${book.totalQuantity}"
        }
    }

    // 2. Tạo ra giao diện cho một dòng (Bơm item_book.xml vào)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    // 3. Gắn dữ liệu vào giao diện tương ứng với vị trí (position)
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = getItem(position)
        holder.bind(currentBook)
    }

    // 4. DiffUtil: Công cụ giúp RecyclerView biết được dữ liệu nào mới, dữ liệu nào cũ để cập nhật
    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        // Kiểm tra xem 2 cuốn sách có phải là 1 không (dựa vào Khóa chính - ID)
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.bookId == newItem.bookId
        }

        // Kiểm tra xem nội dung của cuốn sách có bị thay đổi không
        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}