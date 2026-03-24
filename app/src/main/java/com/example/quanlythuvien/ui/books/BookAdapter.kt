package com.example.quanlythuvien.ui.books

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.Book

class BookAdapter(
    private var categoryNames: Map<Int, String> = emptyMap()
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    fun updateCategories(names: Map<Int, String>) {
        categoryNames = names
        notifyDataSetChanged()
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val tvIsbn: TextView = itemView.findViewById(R.id.tvBookIsbn)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvBookQuantity)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvBookCategory)
        private val context: Context get() = itemView.context

        fun bind(book: Book) {
            tvTitle.text = book.title
            tvAuthor.text = book.author
            tvIsbn.text = "ISBN: ${book.isbnCode}"

            // Quantity chip
            when {
                book.availableQuantity <= 0 -> {
                    tvQuantity.text = "Hết kho"
                    tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.status_out))
                    tvQuantity.background = ContextCompat.getDrawable(context, R.drawable.bg_chip_red)
                }
                book.availableQuantity <= 2 -> {
                    tvQuantity.text = "Còn ${book.availableQuantity} bản"
                    tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.status_low))
                    tvQuantity.background = ContextCompat.getDrawable(context, R.drawable.bg_chip_orange)
                }
                else -> {
                    tvQuantity.text = "Còn ${book.availableQuantity} bản"
                    tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.status_available))
                    tvQuantity.background = ContextCompat.getDrawable(context, R.drawable.bg_chip_green)
                }
            }

            // Category chip
            val catName = categoryNames[book.categoryId] ?: "Danh mục"
            tvCategory.text = catName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.bookId == newItem.bookId
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}