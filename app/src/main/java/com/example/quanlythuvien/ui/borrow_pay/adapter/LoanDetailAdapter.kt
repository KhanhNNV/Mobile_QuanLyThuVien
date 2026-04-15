package com.example.quanlythuvien.ui.borrow_pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.LoanDetailItemData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LoanDetailAdapter(
    private val isAdmin: Boolean,
    private val onMenuActionClick: (LoanDetailItemData, String) -> Unit
) : ListAdapter<LoanDetailItemData, LoanDetailAdapter.BookViewHolder>(BookDiffCallback()) {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCatetory)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDetailStatus)
        private val ibtSet: ImageButton = itemView.findViewById(R.id.ibtSet)
        private val tvNgayTraTitle: TextView = itemView.findViewById(R.id.tvNgayTraTitle)
        private val tvReturnDate: TextView = itemView.findViewById(R.id.tvReturnDate)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDetailDueDate)
        private val ivWarningOverdue: ImageView = itemView.findViewById(R.id.ivWarningOverdueDetail)

        fun bind(item: LoanDetailItemData) {
            val context = itemView.context

            // 1. Đổ dữ liệu văn bản
            tvTitle.text = item.title
            tvAuthor.text = item.author
            tvCategory.text = item.categoryName
            tvDueDate.text = item.dueDate

            val currentStatus = item.status

            // 2. Map Color và Text tương ứng với trạng thái
            val (statusText, textColorRes, bgDrawableRes) = when (currentStatus) {
                "RETURNED_NORMAL", "RETURNED", "COMPLETED" -> Triple("Đã trả", R.color.text_status_success, R.drawable.bg_status_success)
                "BORROWING" -> Triple("Đang mượn", R.color.text_status_info, R.drawable.bg_status_info)
                "LATE", "OVERDUE" -> Triple("Trễ hạn", R.color.text_status_error, R.drawable.bg_status_error)
                "LOST" -> Triple("Bị mất", R.color.text_status_error, R.drawable.bg_status_error)
                "DAMAGED" -> Triple("Hư hỏng", R.color.text_status_error, R.drawable.bg_status_error)
                else -> Triple("Không xác định", R.color.text_status_error, R.drawable.bg_status_error)
            }

            tvStatus.text = statusText
            tvStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            tvStatus.setBackgroundResource(bgDrawableRes)

            // 3. Xử lý ẩn hiện Ngày Trả
            val isReturnedState = currentStatus in listOf("RETURNED_NORMAL", "RETURNED", "COMPLETED", "LATE", "DAMAGED")
            if (isReturnedState && !item.returnDate.isNullOrEmpty()) {
                tvNgayTraTitle.visibility = View.VISIBLE
                tvReturnDate.visibility = View.VISIBLE
                tvReturnDate.text = item.returnDate
            } else {
                tvNgayTraTitle.visibility = View.GONE
                tvReturnDate.visibility = View.GONE
            }

            // 4. Xử lý Cảnh báo Trễ hạn (Đã Fix)
            when (currentStatus) {
                "LATE", "OVERDUE" -> {
                    // Trạng thái từ server đã báo trễ hạn
                    ivWarningOverdue.visibility = View.VISIBLE
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                }
                "BORROWING" -> {
                    // Trạng thái đang mượn, tự tính toán xem có lố ngày hôm nay không
                    try {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val dueDate = sdf.parse(item.dueDate)
                        val today = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                        }.time

                        if (dueDate != null && dueDate.before(today)) {
                            ivWarningOverdue.visibility = View.VISIBLE
                            tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_status_error))
                        } else {
                            ivWarningOverdue.visibility = View.GONE
                            tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        }
                    } catch (e: Exception) {
                        ivWarningOverdue.visibility = View.GONE
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    }
                }
                else -> {
                    // Các trạng thái khác (đã trả, mất...) không cần hiện cảnh báo
                    ivWarningOverdue.visibility = View.GONE
                    tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }

            // 5. Phân quyền menu
            ibtSet.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menu.add(0, 1, 0, "Sửa")
                if (isAdmin) {
                    popup.menu.add(0, 2, 0, "Xóa")
                }
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        1 -> onMenuActionClick(item, "EDIT")
                        2 -> onMenuActionClick(item, "DELETE")
                    }
                    true
                }
                popup.show()
            }

            itemView.setOnClickListener { onMenuActionClick(item, "EDIT") }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan_detail, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookDiffCallback : DiffUtil.ItemCallback<LoanDetailItemData>() {
        override fun areItemsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData) =
            oldItem.title == newItem.title && oldItem.bookId == newItem.bookId

        override fun areContentsTheSame(oldItem: LoanDetailItemData, newItem: LoanDetailItemData) =
            oldItem == newItem
    }
}