package com.example.quanlythuvien.ui.borrow_pay.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R
import com.example.quanlythuvien.ui.borrow_pay.data.LoanItemData

class BorrowPayAdapter(
    private val onItemClick: (LoanItemData) -> Unit
) : ListAdapter<LoanItemData, BorrowPayAdapter.BorrowPayViewHolder>(BorrowPayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowPayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrow_pay, parent, false)
        return BorrowPayViewHolder(view)
    }

    override fun onBindViewHolder(holder: BorrowPayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BorrowPayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        private val tvLoanStatus: TextView = itemView.findViewById(R.id.tvLoanStatus)
        private val tvBorrowDate: TextView = itemView.findViewById(R.id.tvBorrowDate)

        fun bind(item: LoanItemData) {
            tvName.text = item.readerName
            tvLoanId.text = "${item.loanId}"

            // Nếu Backend trả về chuỗi ISO (vd: 2026-04-14T00:00:00),
            // bạn có thể format lại chuỗi item.borrowDate ở ViewModel cho đẹp trước khi đưa vào đây.
            // Tạm thời hiển thị trực tiếp:
            tvBorrowDate.text = item.borrowDate
            Log.d("ĐỨC", "Dữ liệu borrowDate nhận được: ${item.borrowDate}")

            val context = itemView.context

            // --- BƯỚC QUAN TRỌNG: Dùng trực tiếp overallStatus từ Backend ---
            when (item.overallStatus.uppercase()) {
                "ACTIVE" -> {
                    tvLoanStatus.text = "Đang mượn"
                    updateStatusStyle(context, R.color.text_status_info, R.drawable.bg_status_info)
                }
                "COMPLETED" -> {
                    tvLoanStatus.text = "Hoàn tất"
                    updateStatusStyle(context, R.color.text_status_success, R.drawable.bg_status_success)
                }
                "OVERDUE" -> {
                    tvLoanStatus.text = "Quá hạn"
                    updateStatusStyle(context, R.color.text_status_error, R.drawable.bg_status_error)
                }
                "VIOLATED" -> {
                    tvLoanStatus.text = "Vi phạm"
                    updateStatusStyle(context, R.color.text_status_error, R.drawable.bg_status_error) // Có thể đổi màu đỏ đậm hơn nếu muốn
                }
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        // Hàm đổi màu giao diện (Giữ nguyên của bạn)
        private fun updateStatusStyle(context: Context, textColorRes: Int, bgDrawableRes: Int) {
            tvLoanStatus.setTextColor(ContextCompat.getColor(context, textColorRes))
            tvLoanStatus.setBackgroundResource(bgDrawableRes)
        }
    }

    class BorrowPayDiffCallback : DiffUtil.ItemCallback<LoanItemData>() {
        override fun areItemsTheSame(oldItem: LoanItemData, newItem: LoanItemData) = oldItem.loanId == newItem.loanId
        override fun areContentsTheSame(oldItem: LoanItemData, newItem: LoanItemData) = oldItem == newItem
    }
}