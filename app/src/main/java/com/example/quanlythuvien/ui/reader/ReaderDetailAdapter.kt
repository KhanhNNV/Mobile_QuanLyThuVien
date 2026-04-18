package com.example.quanlythuvien.ui.reader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.model.response.LoanDetailResponse

class ReaderDetailAdapter(
    private var list: List<LoanDetailResponse>,
    private val onBookClick: (LoanDetailResponse) -> Unit
) : RecyclerView.Adapter<ReaderDetailAdapter.BooKViewHolder>() {
    private var bookList: List<LoanDetailResponse> = listOf()

    fun submitList(bookList: List<LoanDetailResponse>?) {
        this.bookList = bookList ?: listOf()
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
//        private val tvBookAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
//        private val tvBookIsbn: TextView = itemView.findViewById(R.id.tvBookIsbn)
        private val tvBookBorrowDate: TextView = itemView.findViewById(R.id.tvBorrowDate)
        private val tvBookDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        fun bind(book: LoanDetailResponse) {
            tvBookTitle.text = book.bookTitle
 //           tvBookAuthor.text = book.author ?: "Không có tác giả"
            tvBookBorrowDate.text = "Ngày mượn: ${book.returnDate}" // Cần format nếu là String ISO
            tvBookDueDate.text = "Hạn trả: ${book.dueDate}"
//            tvBookIsbn.text = "ISBN: ${book.isbn ?: "---"}"

            // Highlight màu đỏ nếu quá hạn
            if (book.status.trim().equals("OVERDUE", ignoreCase = true)) {
                tvBookDueDate.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            } else {
                tvBookDueDate.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }

            // Highlight màu đỏ nếu quá hạn
            if (book.status.trim().equals("RETURNED", ignoreCase = true)) {
                tvBookDueDate.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
            }
        }
    }
}