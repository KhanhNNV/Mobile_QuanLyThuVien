package com.example.quanlythuvien.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.BookResponse

class BookAdapter : ListAdapter<BookResponse, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    var onItemClick: ((BookResponse) -> Unit)? = null

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
        val book = getItem(position)

        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvIsbn.text = "ISBN: ${book.isbn}"
        holder.tvQuantity.text = "Còn ${book.availableCopies ?: "--"} bản"
        holder.tvCategory.text = "Danh mục: ${book.categoryName ?: "Chưa cập nhật"}"

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book)
        }
    }

    fun removeById(bookId: Long): Int {
        val mutable = currentList.toMutableList()
        val index = mutable.indexOfFirst { it.bookId == bookId }
        if (index >= 0) {
            mutable.removeAt(index)
            submitList(mutable)
        }
        return index
    }

    fun setItems(items: List<BookResponse>) {
        submitList(items)
    }
}

private class BookDiffCallback : DiffUtil.ItemCallback<BookResponse>() {
    override fun areItemsTheSame(oldItem: BookResponse, newItem: BookResponse): Boolean {
        return oldItem.bookId == newItem.bookId
    }

    override fun areContentsTheSame(oldItem: BookResponse, newItem: BookResponse): Boolean {
        return oldItem == newItem
    }
}