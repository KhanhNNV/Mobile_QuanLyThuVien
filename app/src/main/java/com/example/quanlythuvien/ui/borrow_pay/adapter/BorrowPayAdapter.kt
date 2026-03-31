package com.example.quanlythuvien.ui.borrow_pay.adapter

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
import android.content.res.ColorStateList

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
        // Ánh xạ các View từ XML bạn đã cung cấp
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        private val tvLoanStatus: TextView = itemView.findViewById(R.id.tvLoanStatus)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        fun bind(item: LoanItemData) {
            tvName.text = item.readerName
            tvLoanId.text = "${item.loanId}"
            tvDueDate.text = item.dueDate
            val context = itemView.context
            // Xử lý màu sắc cho trạng thái để dễ phân biệt
            if (item.overallStatus == "BORROWING") {
                tvLoanStatus.text = "Đang mượn"

                val textColor = ContextCompat.getColor(context, R.color.text_status_info)
                val bgColor = ContextCompat.getColor(context, R.color.status_info)

                tvLoanStatus.setTextColor(textColor)
                tvLoanStatus.backgroundTintList = ColorStateList.valueOf(bgColor)
            } else {
                tvLoanStatus.text = "Đã trả"

                val textColor = ContextCompat.getColor(context, R.color.text_status_success)
                val bgColor = ContextCompat.getColor(context, R.color.status_success)

                tvLoanStatus.setTextColor(textColor)
                tvLoanStatus.backgroundTintList = ColorStateList.valueOf(bgColor)
            }
            // Sự kiện click để mở Dialog (Nơi này sẽ hiện đầy đủ Tên sách, Tác giả...)
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // DiffUtil giúp RecyclerView cập nhật danh sách cực nhanh và mượt
    class BorrowPayDiffCallback : DiffUtil.ItemCallback<LoanItemData>() {
        override fun areItemsTheSame(oldItem: LoanItemData, newItem: LoanItemData): Boolean {
            return oldItem.loanId == newItem.loanId
        }

        override fun areContentsTheSame(oldItem: LoanItemData, newItem: LoanItemData): Boolean {
            return oldItem == newItem
        }
    }
}