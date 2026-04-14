package com.example.quanlythuvien.ui.books

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data2.entity.Book // Nhớ import đúng đường dẫn Entity của bạn

class BookAdapter(private val bookList: MutableList<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    var onItemClick: ((Book) -> Unit)? = null

    private fun categoryLabel(categoryId: Int): String = when (categoryId) {
        1 -> "CNTT"
        2 -> "Tâm lý"
        3 -> "Tiểu thuyết"
        4 -> "Lịch sử"
        else -> "Khác"
    }

    private fun quantityLabel(book: Book): String {
        val available = book.availableQuantity.coerceAtLeast(0)
        val total = book.totalQuantity.coerceAtLeast(0)
        val lost = book.lostQuantity.coerceAtLeast(0)

        val base = "Có sẵn $available / $total"
        return if (lost > 0) "$base • Mất $lost" else base
    }

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

        // Gán dữ liệu
        holder.tvTitle.text = book.title
        holder.tvAuthor.text = book.author
        holder.tvIsbn.text = "ISBN: ${book.isbnCode}"
        holder.tvQuantity.text = quantityLabel(book)
        holder.tvCategory.text = categoryLabel(book.categoryId)

        // Bắt sự kiện khi người dùng bấm vào 1 dòng (itemView)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(book) // Truyền cuốn sách đang được bấm ra ngoài
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    fun removeById(bookId: Long): Int {
        val index = bookList.indexOfFirst { it.bookId == bookId }
        if (index >= 0) {
            bookList.removeAt(index)
            notifyItemRemoved(index)
        }
        return index
    }

    fun setItems(items: List<Book>) {
        bookList.clear()
        bookList.addAll(items)
        notifyDataSetChanged()
    }
}