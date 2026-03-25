package com.example.quanlythuvien.ui.borrow_pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.data.entity.enums.LoanDetailStatus
import java.text.SimpleDateFormat
import java.util.*

class BorrowedBookDetailAdapter(
    private val onStatusChange: (BorrowedBookDetail, LoanDetailStatus) -> Unit
) : ListAdapter<BorrowedBookDetail, BorrowedBookDetailAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_detail_of_dialog, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCatetory) // Theo ID trong XML của bạn
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDetailStatus)
        private val tvReturnDate: TextView = itemView.findViewById(R.id.tvReturnDate)
        private val ibtSet: ImageButton = itemView.findViewById(R.id.ibtSet) // Nút 3 chấm của bạn

        fun bind(item: BorrowedBookDetail) {
            tvTitle.text = item.title
            tvAuthor.text = item.author
            tvCategory.text = item.categoryName

            val currentStatus = LoanDetailStatus.fromValue(item.status)

            // Hiển thị trạng thái
            tvStatus.text = when (currentStatus) {
                LoanDetailStatus.BORROWING -> "Đang mượn"
                LoanDetailStatus.RETURNED -> "Đã trả"
                LoanDetailStatus.LOST -> "Bị mất"
            }

            // Đổi màu nền cho Tag trạng thái
            val context = itemView.context
            val colorRes = when (currentStatus) {
                LoanDetailStatus.RETURNED -> android.R.color.holo_green_dark
                LoanDetailStatus.LOST -> android.R.color.holo_red_dark
                else -> android.R.color.holo_blue_dark
            }
            tvStatus.backgroundTintList = ContextCompat.getColorStateList(context, colorRes)

            // Hiển thị ngày trả (nếu có)
            if (!item.returnDate.isNullOrEmpty()) {
                tvReturnDate.visibility = View.VISIBLE
                tvReturnDate.text = "Ngày trả: ${item.returnDate}"
            } else {
                tvReturnDate.visibility = View.GONE
            }

            // Xử lý PopupMenu cho nút ibtSet
            ibtSet.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menu.add(0, 1, 0, "Đánh dấu: Đã trả")
                popup.menu.add(0, 2, 0, "Đánh dấu: Bị mất")
                popup.menu.add(0, 3, 0, "Đang mượn")

                popup.setOnMenuItemClickListener { menuItem ->
                    val newEnumStatus = when (menuItem.itemId) {
                        1 -> LoanDetailStatus.RETURNED
                        2 -> LoanDetailStatus.LOST
                        else -> LoanDetailStatus.BORROWING
                    }
                    onStatusChange(item, newEnumStatus)
                    true
                }
                popup.show()
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<BorrowedBookDetail>() {
        override fun areItemsTheSame(oldItem: BorrowedBookDetail, newItem: BorrowedBookDetail) =
            oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: BorrowedBookDetail, newItem: BorrowedBookDetail) =
            oldItem == newItem
    }
}