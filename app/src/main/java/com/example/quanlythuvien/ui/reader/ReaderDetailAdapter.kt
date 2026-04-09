package com.example.quanlythuvien.ui.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R


// Data class để chứa dữ liệu giả
data class MockReaderBook(
    val title: String,
    val author: String,
    val isbn: String,
    val borrowDate: String,
    val dueDate: String,
    val isOverdue: Boolean,
    val isReturned: Boolean // true: Đã trả, false: Đang mượn
)


class ReaderDetailAdapter(
    private val onBookClick: (MockReaderBook) -> Unit
) : RecyclerView.Adapter<ReaderDetailAdapter.BooKViewHolder>() {
    private var bookList: List<MockReaderBook> = listOf()

    fun submitList(bookList: List<MockReaderBook>) {
        this.bookList = bookList
        notifyDataSetChanged()
    }



    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): BooKViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_reader_book_detail, p0, false)
        return BooKViewHolder(view)
    }

    override fun onBindViewHolder(
        p0: BooKViewHolder,
        p1: Int
    ) {
        val book = this.bookList[p1]
        p0.bind(book)

        // Bắt sự kiện click vào Card/View của từng sách
        p0.itemView.setOnClickListener {
            onBookClick(book)
        }
    }

    override fun getItemCount(): Int {
        return bookList.size;
    }

    class BooKViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val tvBookIsbn: TextView = itemView.findViewById(R.id.tvBookIsbn)
        private val tvBookBorrowDate: TextView = itemView.findViewById(R.id.tvBorrowDate)
        private val tvBookDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        fun bind(book: MockReaderBook) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvBookIsbn.text = book.isbn
            tvBookBorrowDate.text = "Ngày mượn: ${book.borrowDate}"
            if (book.isOverdue) {
                tvBookDueDate.text = "Hạn trả: ${book.dueDate} (Quá hạn)"
                tvBookDueDate.setTextColor(itemView.context.getColor(R.color.red))
            } else {
                tvBookDueDate.text = "Hạn trả: ${book.dueDate}"
            }
            if (book.isReturned) {
                tvBookDueDate.text = "Đã trả: ${book.dueDate}"
                tvBookDueDate.setTextColor(itemView.context.getColor(R.color.green))
            }

        }
    }
}