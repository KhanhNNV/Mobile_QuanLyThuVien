package com.example.quanlythuvien.ui.borrow_pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.quanlythuvien.R

class BorrowPayAdapter(
    private val onItemClick: (BorrowPayItem) -> Unit
) : ListAdapter<BorrowPayItem, BorrowPayAdapter.BorrowPayViewHolder>(BorrowPayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowPayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrow_pay, parent, false) // Tên file XML của bạn
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

        fun bind(item: BorrowPayItem) {
            tvName.text = item.readerName
            tvLoanId.text = "Mã phiếu: #${item.loanId}"
            tvDueDate.text = "Hạn trả: ${item.dueDate}"
            val context = itemView.context
            // Xử lý màu sắc cho trạng thái để dễ phân biệt
            if (item.overallStatus == "BORROWING") {
                tvLoanStatus.text = "Đang mượn"
                // Thường mượn thì để màu Xanh dương hoặc Cam, Trễ hạn mới để Đỏ
                tvLoanStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
            } else {
                // Mặc định là RETURNED
                tvLoanStatus.text = "Đã trả"
                tvLoanStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
            }
            // Sự kiện click để mở Dialog (Nơi này sẽ hiện đầy đủ Tên sách, Tác giả...)
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // DiffUtil giúp RecyclerView cập nhật danh sách cực nhanh và mượt
    class BorrowPayDiffCallback : DiffUtil.ItemCallback<BorrowPayItem>() {
        override fun areItemsTheSame(oldItem: BorrowPayItem, newItem: BorrowPayItem): Boolean {
            return oldItem.loanId == newItem.loanId
        }

        override fun areContentsTheSame(oldItem: BorrowPayItem, newItem: BorrowPayItem): Boolean {
            return oldItem == newItem
        }
    }
}