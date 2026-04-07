package com.example.quanlythuvien.ui.borrow_pay

import android.content.res.ColorStateList
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
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData

class LoanAdapter(
    private val onStatusChange: (LoanDetailItemData, LoanDetailStatus) -> Unit
) : ListAdapter<LoanDetailItemData, LoanAdapter.BookViewHolder>(BookDiffCallback()) {

    // 1. LỚP KHUÔN (VIEW HOLDER)
    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCatetory)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDetailStatus)
        private val tvReturnDate: TextView = itemView.findViewById(R.id.tvReturnDate)
        private val ibtSet: ImageButton = itemView.findViewById(R.id.ibtSet)

        // Hàm bơm dữ liệu
        fun bind(item: LoanDetailItemData) {
            tvTitle.text = item.title
            tvAuthor.text = item.author
            tvCategory.text = item.categoryName

            // Lấy Enum từ dữ liệu String
            val currentStatus = LoanDetailStatus.fromValue(item.status)

            // Hiển thị chữ cho trạng thái
            tvStatus.text = when (currentStatus) {
                LoanDetailStatus.BORROWING -> "Đang mượn"
                LoanDetailStatus.RETURNED -> "Đã trả"
                LoanDetailStatus.LOST -> "Bị mất"
                else -> "Không xác định"
            }

            // Đổi màu nền cho Tag trạng thái
            val context = itemView.context

            val (text, textColorRes, bgColorRes) = when (currentStatus) {
                LoanDetailStatus.BORROWING -> Triple(
                    "Đang mượn",
                    R.color.text_status_info,
                    R.color.status_info
                )
                LoanDetailStatus.RETURNED -> Triple(
                    "Đã trả",
                    R.color.text_status_success,
                    R.color.status_success
                )
                LoanDetailStatus.LOST -> Triple(
                    "Bị mất",
                    R.color.text_status_error,
                    R.color.status_error
                )
                else -> Triple(
                    "Không xác định",
                    R.color.text_secondary,
                    R.color.bg_card
                )
            }

            tvStatus.text = text
            tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            tvStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, bgColorRes))

            // Hiển thị ngày trả (nếu có)
            if (!item.returnDate.isNullOrEmpty()) {
                tvReturnDate.visibility = View.VISIBLE
                tvReturnDate.text = item.returnDate
            } else {
                tvReturnDate.visibility = View.GONE
            }

            // Xử lý PopupMenu cho nút 3 chấm
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
                    // Gọi điện báo cáo ra bên ngoài Activity/Fragment
                    onStatusChange(item, newEnumStatus)
                    true
                }
                popup.show()
            }
        }
    }

    // 2. TẠO KHUÔN
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_detail, parent, false)
        return BookViewHolder(view)
    }

    // 3. GẮN DỮ LIỆU VÀO KHUÔN
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 4. CÔNG CỤ SO SÁNH (DIFF UTIL)
    class BookDiffCallback : DiffUtil.ItemCallback<LoanDetailItemData>() {
        override fun areItemsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData): Boolean {
            return oldItem.title == newItem.title // Lý tưởng nhất là so sánh bằng ID nếu có
        }

        override fun areContentsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData): Boolean {
            return oldItem == newItem
        }
    }
}